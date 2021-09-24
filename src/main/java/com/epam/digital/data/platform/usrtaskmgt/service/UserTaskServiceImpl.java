package com.epam.digital.data.platform.usrtaskmgt.service;

import com.epam.digital.data.platform.bpms.api.dto.ClaimTaskDto;
import com.epam.digital.data.platform.bpms.api.dto.PaginationQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.SortingDto;
import com.epam.digital.data.platform.bpms.api.dto.TaskCountQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.TaskQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.UserTaskDto;
import com.epam.digital.data.platform.bpms.client.CamundaTaskRestClient;
import com.epam.digital.data.platform.bpms.client.ExtendedUserTaskRestClient;
import com.epam.digital.data.platform.bpms.client.TaskPropertyRestClient;
import com.epam.digital.data.platform.bpms.client.exception.TaskNotFoundException;
import com.epam.digital.data.platform.dso.api.dto.Subject;
import com.epam.digital.data.platform.dso.api.dto.VerificationRequestDto;
import com.epam.digital.data.platform.dso.api.dto.VerifySubjectRequestDto;
import com.epam.digital.data.platform.dso.client.DigitalSignatureRestClient;
import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.epam.digital.data.platform.integration.ceph.exception.CephCommunicationException;
import com.epam.digital.data.platform.integration.ceph.service.FormDataCephService;
import com.epam.digital.data.platform.starter.errorhandling.exception.ValidationException;
import com.epam.digital.data.platform.starter.validation.service.FormValidationService;
import com.epam.digital.data.platform.usrtaskmgt.exception.SignatureValidationException;
import com.epam.digital.data.platform.usrtaskmgt.exception.UserTaskAlreadyAssignedException;
import com.epam.digital.data.platform.usrtaskmgt.exception.UserTaskAuthorizationException;
import com.epam.digital.data.platform.usrtaskmgt.exception.UserTaskNotExistsException;
import com.epam.digital.data.platform.usrtaskmgt.exception.UserTaskNotExistsOrCompletedException;
import com.epam.digital.data.platform.usrtaskmgt.mapper.UserTaskDtoMapper;
import com.epam.digital.data.platform.usrtaskmgt.model.Pageable;
import com.epam.digital.data.platform.usrtaskmgt.model.SignableUserTaskDto;
import com.epam.digital.data.platform.usrtaskmgt.util.AuthUtil;
import com.epam.digital.data.platform.usrtaskmgt.util.CephKeyProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.task.CompleteTaskDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserTaskServiceImpl implements UserTaskService {

  private static final String USER_TASK_AUTHORIZATION_ERROR_MSG = "The user with username %s does not have permission on resource Task with id %s";
  private static final String SIGN_PROPERTY = "eSign";
  private static final String FORM_VARIABLES_PROPERTY = "formVariables";
  private static final String FORM_VARIABLES_REGEX = "\\s*,\\s*";

  private final CamundaTaskRestClient camundaTaskRestClient;
  private final ExtendedUserTaskRestClient extendedUserTaskRestClient;
  private final TaskPropertyRestClient taskPropertyRestClient;
  private final DigitalSignatureRestClient digitalSignatureRestClient;

  private final FormDataCephService cephService;

  private final UserTaskDtoMapper userTaskDtoMapper;
  private final ObjectMapper objectMapper;
  private final CephKeyProvider cephKeyProvider;
  private final FormValidationService formValidationService;

  @Override
  public List<UserTaskDto> getTasks(String processInstanceId, Pageable page) {
    log.info("Getting unfinished user tasks for process instance {}. Parameters: {}",
        processInstanceId, page);
    var tasks = getTaskDtoList(processInstanceId, page);
    log.trace("Found user tasks - {}", tasks);

    log.info("Found {} user tasks. Task ids - {}", tasks.size(),
        tasks.stream().map(UserTaskDto::getId).collect(Collectors.joining(", ")));

    return tasks;
  }

  @Override
  public SignableUserTaskDto getTaskById(String taskId) {
    log.info("Getting unfinished user task by id {}", taskId);

    var taskById = getUserTaskById(taskId);
    log.trace("Task was found in bpms");
    verifyAssignee(taskById);

    var userTaskDto = userTaskDtoMapper.toSignableUserTaskDto(taskById);

    var data = getFormData(taskById.getProcessInstanceId(), taskById.getTaskDefinitionKey());
    userTaskDto.setData(data);
    log.trace("Form data pre-population is found. {}", data);

    var taskProperties = getTaskProperties(taskId);
    userTaskDto.setESign(Boolean.parseBoolean(taskProperties.get(SIGN_PROPERTY)));
    userTaskDto.setFormVariables(getTaskFormVariables(taskProperties, taskId));
    log.trace("Found user task - {}", userTaskDto);

    log.info("Unfinished user task by id {} is found", taskId);
    return userTaskDto;
  }

  @Override
  public CountResultDto countTasks() {
    log.info("Getting unfinished user task count");
    var unassignedCountTaskQuery = TaskCountQueryDto.builder()
        .assignee(AuthUtil.getCurrentUsername())
        .unassigned(true)
        .build();
    var taskCountQueryDto = TaskCountQueryDto.builder()
        .orQueries(List.of(unassignedCountTaskQuery))
        .build();
    var result = camundaTaskRestClient.getTaskCountByParams(taskCountQueryDto);
    log.info("Getting unfinished user task count finished - {}", result.getCount());
    return result;
  }

  @Override
  public void completeTaskById(String taskId, FormDataDto formData) {
    log.info("Completing user task with id {}", taskId);
    completeTask(taskId, formData, (id, data) -> {
    });
    log.info("Task {} is completed", taskId);
  }

  @Override
  public void signOfficerForm(String taskId, FormDataDto formData) {
    log.info("Completing signable officer task with id {}", taskId);
    completeTask(taskId, formData, (id, data) -> verifyOfficerFormData(data));
    log.info("Signable officer task {} is completed", taskId);
  }

  @Override
  public void signCitizenForm(String taskId, FormDataDto formData) {
    log.info("Completing signable citizen task with id {}", taskId);
    completeTask(taskId, formData, this::verifyCitizenFormData);
    log.info("Signable citizen task {} is completed", taskId);
  }

  @Override
  public void claimTaskById(String taskId) {
    log.info("Claiming task with id {}", taskId);
    var currentUserName = AuthUtil.getCurrentUsername();
    log.trace("Claiming task with id {} to {}", taskId, currentUserName);

    var taskDto = getUserTaskByIdForClaim(taskId);
    var assignee = taskDto.getAssignee();
    if (!StringUtils.isEmpty(assignee) && !assignee.equals(currentUserName)) {
      throw new UserTaskAlreadyAssignedException(taskDto.getName(), "Task already assigned");
    }

    var claimTaskDto = ClaimTaskDto.builder().userId(currentUserName).build();
    camundaTaskRestClient.claimTaskById(taskId, claimTaskDto);
    log.info("Task {} was claimed", taskId);
  }

  private List<UserTaskDto> getTaskDtoList(String processInstanceId, Pageable page) {
    log.debug("Getting assigned to current user or unassigned user tasks of process instance {}. "
        + "Paging and sorting params - {}", processInstanceId, page);
    var unassignedTaskQuery = TaskQueryDto.builder()
        .unassigned(true)
        .assignee(AuthUtil.getCurrentUsername())
        .build();
    var sortingDto = SortingDto.builder()
        .sortBy(page.getSortBy())
        .sortOrder(page.getSortOrder())
        .build();
    var taskQueryDto = TaskQueryDto.builder()
        .processInstanceId(processInstanceId)
        .orQueries(List.of(unassignedTaskQuery))
        .sorting(List.of(sortingDto))
        .build();
    var paginationQueryDto = PaginationQueryDto.builder()
        .firstResult(page.getFirstResult())
        .maxResults(page.getMaxResults())
        .build();

    return extendedUserTaskRestClient.getTasksByParams(taskQueryDto, paginationQueryDto);
  }

  private void verifyOfficerFormData(FormDataDto formData) {
    log.debug("Verifying officer signed form data. {}", formData);
    var data = serializeFormData(formData.getData());
    var signature = formData.getSignature();
    var verifyResponseDto = digitalSignatureRestClient.verifyOfficer(
        new VerificationRequestDto(signature, data));
    if (!verifyResponseDto.isValid()) {
      log.error("Officer task form data hasn't passed the signature verification");
      throw new SignatureValidationException(verifyResponseDto.getError());
    }
    log.debug("Officer signed form data verified.");
  }

  private void verifyCitizenFormData(String taskId, FormDataDto formData) {
    log.debug("Verifying citizen signed form data. {}", formData);
    var data = serializeFormData(formData.getData());
    var signature = formData.getSignature();
    var taskProperties = taskPropertyRestClient.getTaskProperty(taskId);
    var allowedSubjects = Arrays.stream(Subject.values())
        .filter(subject -> Boolean.parseBoolean(taskProperties.get(subject.name())))
        .collect(Collectors.toList());
    log.trace("Found subjects - {}", allowedSubjects);

    if (allowedSubjects.isEmpty()) {
      allowedSubjects = Collections.singletonList(Subject.INDIVIDUAL);
    }

    var verifyResponseDto = digitalSignatureRestClient.verifyCitizen(
        new VerifySubjectRequestDto(allowedSubjects, signature, data));
    if (!verifyResponseDto.isValid()) {
      log.error("Citizen task form data hasn't passed the signature verification");
      throw new SignatureValidationException(verifyResponseDto.getError());
    }
    log.debug("Citizen signed form data verified.");
  }

  private TaskDto getUserTaskByIdForClaim(String taskId) {
    try {
      return camundaTaskRestClient.getTaskById(taskId);
    } catch (TaskNotFoundException ex) {
      throw new UserTaskNotExistsOrCompletedException(ex);
    }
  }

  private void completeTask(String taskId, FormDataDto formData,
      BiConsumer<String, FormDataDto> verifyingConsumer) {
    log.debug("Completing user task {} with form data {}", taskId, formData);

    var taskDto = getUserTaskById(taskId);
    log.trace("User task {} was found", taskId);

    validateFormData(taskDto.getFormKey(), formData);
    log.trace("Form data {} has passed the validation", formData);

    verifyingConsumer.accept(taskId, formData);
    log.trace("Form data has passed the signature verification if there was any");

    verifyAssignee(taskDto);
    log.trace("Verified if user task assignee is current user.");

    saveFormData(taskDto, formData);
    log.trace("Form data is saved to ceph");

    camundaTaskRestClient.completeTaskById(taskId, new CompleteTaskDto());
    log.debug("User task {} successfully completed", taskId);
  }

  private Map<String, Object> getTaskFormVariables(Map<String, String> taskProperties,
      String taskId) {
    var formVariablesProperties = taskProperties.get(FORM_VARIABLES_PROPERTY);
    if (Objects.isNull(formVariablesProperties)) {
      return null;
    }

    var formVariableNames = List.of(formVariablesProperties.split(FORM_VARIABLES_REGEX));
    var allTaskVariables = camundaTaskRestClient.getTaskVariables(taskId);

    return allTaskVariables.entrySet().stream()
        .filter(entry -> formVariableNames.contains(entry.getKey()))
        .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getValue()));
  }

  private Map<String, String> getTaskProperties(String taskId) {
    return taskPropertyRestClient.getTaskProperty(taskId);
  }

  private void saveFormData(TaskDto taskDto, FormDataDto fromData) {
    var processInstanceId = taskDto.getProcessInstanceId();
    var taskDefinitionKey = taskDto.getTaskDefinitionKey();

    var formDataCephKey = cephKeyProvider.generateKey(taskDefinitionKey, processInstanceId);

    putFormDataToCeph(formDataCephKey, fromData);
  }

  private void putFormDataToCeph(String secureSysVarRefTaskFormData, FormDataDto formData) {
    try {
      cephService.putFormData(secureSysVarRefTaskFormData, formData);
    } catch (CephCommunicationException ex) {
      log.warn("Couldn't put form data to ceph", ex);
      throw ex;
    }
  }

  private Map<String, Object> getFormData(String processInstanceId, String taskDefinitionKey) {
    var cephKey = cephKeyProvider.generateKey(taskDefinitionKey, processInstanceId);
    return getFormDataFromCeph(cephKey).map(FormDataDto::getData).orElse(null);
  }

  private Optional<FormDataDto> getFormDataFromCeph(String formDataCephKey) {
    try {
      return cephService.getFormData(formDataCephKey);
    } catch (CephCommunicationException ex) {
      log.warn("Couldn't get form data from ceph", ex);
      return Optional.empty();
    }
  }

  private <T> String serializeFormData(T formData) {
    try {
      return objectMapper.writeValueAsString(formData);
    } catch (JsonProcessingException e) {
      e.clearLocation();
      throw new IllegalStateException("Couldn't serialize form data", e);
    }
  }

  private TaskDto getUserTaskById(String taskId) {
    try {
      return camundaTaskRestClient.getTaskById(taskId);
    } catch (TaskNotFoundException ex) {
      throw new UserTaskNotExistsException(taskId, ex);
    }
  }

  private void verifyAssignee(TaskDto taskDto) {
    var assignee = taskDto.getAssignee();
    var currentUserName = AuthUtil.getCurrentUsername();
    if (Objects.isNull(assignee) || !assignee.equals(currentUserName)) {
      throw new UserTaskAuthorizationException(
          String.format(USER_TASK_AUTHORIZATION_ERROR_MSG, currentUserName, taskDto.getId()),
          taskDto.getId());
    }
  }

  private void validateFormData(String formId, FormDataDto formDataDto) {
    var formValidationResponseDto = formValidationService.validateForm(formId, formDataDto);
    if (!formValidationResponseDto.isValid()) {
      throw new ValidationException(formValidationResponseDto.getError());
    }
  }
}
