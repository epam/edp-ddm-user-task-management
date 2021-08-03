package com.epam.digital.data.platform.usrtaskmgt.service;

import com.epam.digital.data.platform.bpms.api.dto.ClaimTaskDto;
import com.epam.digital.data.platform.bpms.api.dto.ProcessDefinitionQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.TaskQueryDto;
import com.epam.digital.data.platform.bpms.client.CamundaTaskRestClient;
import com.epam.digital.data.platform.bpms.client.ProcessDefinitionRestClient;
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
import com.epam.digital.data.platform.usrtaskmgt.dto.SignableUserTaskDto;
import com.epam.digital.data.platform.usrtaskmgt.dto.UserTaskDto;
import com.epam.digital.data.platform.usrtaskmgt.exception.SignatureValidationException;
import com.epam.digital.data.platform.usrtaskmgt.exception.UserTaskAlreadyAssignedException;
import com.epam.digital.data.platform.usrtaskmgt.exception.UserTaskAuthorizationException;
import com.epam.digital.data.platform.usrtaskmgt.exception.UserTaskNotExistsException;
import com.epam.digital.data.platform.usrtaskmgt.exception.UserTaskNotExistsOrCompletedException;
import com.epam.digital.data.platform.usrtaskmgt.mapper.UserTaskDtoMapper;
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
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
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

  private final ProcessDefinitionRestClient processDefinitionRestClient;
  private final CamundaTaskRestClient camundaTaskRestClient;
  private final TaskPropertyRestClient taskPropertyRestClient;
  private final DigitalSignatureRestClient digitalSignatureRestClient;

  private final FormDataCephService cephService;

  private final UserTaskDtoMapper userTaskDtoMapper;
  private final ObjectMapper objectMapper;
  private final CephKeyProvider cephKeyProvider;
  private final FormValidationService formValidationService;

  @Override
  public List<UserTaskDto> getTasks(String processInstanceId) {
    var taskQueryDto = prepareDefaultSearchTaskQuery();
    taskQueryDto.setProcessInstanceId(processInstanceId);
    List<TaskDto> tasks = camundaTaskRestClient.getTasksByParams(taskQueryDto);
    return postProcess(userTaskDtoMapper.toUserTasks(tasks));
  }

  @Override
  public SignableUserTaskDto getTaskById(String taskId) {
    var taskById = getUserTaskById(taskId);
    verifyAssignee(taskById);
    return postProcess(taskById, userTaskDtoMapper.toSignableUserTaskDto(taskById));
  }

  @Override
  public CountResultDto countTasks() {
    return camundaTaskRestClient.getTaskCountByParams(prepareDefaultSearchTaskQuery());
  }

  @Override
  public void completeTaskById(String taskId, FormDataDto formData) {
    var taskDto = getUserTaskById(taskId);
    validateFormData(taskDto.getFormKey(), formData);
    completeTask(taskDto, formData);
  }

  @Override
  public void signOfficerForm(String taskId, FormDataDto formData) {
    var taskDto = getUserTaskById(taskId);
    validateFormData(taskDto.getFormKey(), formData);
    var data = serializeFormData(formData.getData());
    var signature = formData.getSignature();
    var verifyResponseDto = digitalSignatureRestClient.verifyOfficer(
        new VerificationRequestDto(signature, data));
    if (!verifyResponseDto.isValid()) {
      throw new SignatureValidationException(verifyResponseDto.getError());
    }
    completeTask(taskDto, formData);
  }

  @Override
  public void signCitizenForm(String taskId, FormDataDto formData) {
    var taskDto = getUserTaskById(taskId);
    validateFormData(taskDto.getFormKey(), formData);
    var data = serializeFormData(formData.getData());
    var signature = formData.getSignature();
    var taskProperties = taskPropertyRestClient.getTaskProperty(taskId);
    var allowedSubjects = Arrays.stream(Subject.values())
        .filter(subject -> String.valueOf(true).equals(taskProperties.get(subject.name())))
        .collect(Collectors.toList());

    if (allowedSubjects.isEmpty()) {
      allowedSubjects = Collections.singletonList(Subject.INDIVIDUAL);
    }

    var verifyResponseDto = digitalSignatureRestClient.verifyCitizen(
        new VerifySubjectRequestDto(allowedSubjects, signature, data));
    if (!verifyResponseDto.isValid()) {
      throw new SignatureValidationException(verifyResponseDto.getError());
    }
    completeTask(taskDto, formData);
  }

  @Override
  public void claimTaskById(String taskId) {
    var taskDto = getUserTaskByIdForClaim(taskId);
    claimTask(taskDto);
  }

  private TaskDto getUserTaskByIdForClaim(String taskId) {
    try {
      return camundaTaskRestClient.getTaskById(taskId);
    } catch (TaskNotFoundException ex) {
      throw new UserTaskNotExistsOrCompletedException(ex);
    }
  }

  private void claimTask(TaskDto taskDto) {
    var assignee = taskDto.getAssignee();
    var currentUserName = AuthUtil.getCurrentUsername();
    if (!StringUtils.isEmpty(assignee) && !assignee.equals(currentUserName)) {
      throw new UserTaskAlreadyAssignedException(taskDto.getName(), "Task already assigned");
    }
    camundaTaskRestClient.claimTaskById(
        taskDto.getId(), ClaimTaskDto.builder().userId(currentUserName).build());
  }

  private void completeTask(TaskDto taskDto, FormDataDto formData) {
    verifyAssignee(taskDto);
    saveFormData(taskDto, formData);
    camundaTaskRestClient.completeTaskById(taskDto.getId(), new CompleteTaskDto());
  }

  /**
   * Method provides a possibility to fill extract properties on the subclass level
   *
   * @param userTaskDto specified task.
   * @return filled user task.
   */
  private SignableUserTaskDto postProcess(TaskDto taskDto, SignableUserTaskDto userTaskDto) {
    fillFormData(taskDto, userTaskDto);
    var taskProperties = getTaskProperties(userTaskDto.getId());
    setSignTypeForTask(userTaskDto, taskProperties);
    setTaskFormVariables(userTaskDto, taskProperties);
    return userTaskDto;
  }

  private List<UserTaskDto> postProcess(List<UserTaskDto> userTaskDtos) {
    fillProcessDefinitionName(userTaskDtos);
    return userTaskDtos;
  }

  private void setSignTypeForTask(SignableUserTaskDto userTaskDto, Map<String, String> taskProperties) {
    var eSign = taskProperties.get(SIGN_PROPERTY);
    userTaskDto.setESign(Boolean.parseBoolean(eSign));
  }

  private void setTaskFormVariables(SignableUserTaskDto userTaskDto, Map<String, String> taskProperties) {
    var formVariablesProperties = taskProperties.get(FORM_VARIABLES_PROPERTY);

    if (Objects.nonNull(formVariablesProperties)) {
      var taskVariables = camundaTaskRestClient.getTaskVariables(userTaskDto.getId());
      var variables = List.of(formVariablesProperties.split(FORM_VARIABLES_REGEX));
      var formVariables = taskVariables.entrySet().stream()
          .filter(entry -> variables.contains(entry.getKey()))
          .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getValue()));
      userTaskDto.setFormVariables(formVariables);
    }
  }

  private Map<String, String> getTaskProperties(String taskId){
    return taskPropertyRestClient.getTaskProperty(taskId);
  }

  private void saveFormData(TaskDto taskDto, FormDataDto fromData) {
    var processInstanceId = taskDto.getProcessInstanceId();
    var taskDefinitionKey = taskDto.getTaskDefinitionKey();

    var secureSysVarRefTaskFormData = cephKeyProvider.generateKey(taskDefinitionKey, processInstanceId);

    putStringFormDataToCeph(secureSysVarRefTaskFormData, fromData);
  }

  private void fillFormData(TaskDto taskDto, SignableUserTaskDto userTaskDto) {
    var processInstanceId = taskDto.getProcessInstanceId();
    var taskDefinitionKey = taskDto.getTaskDefinitionKey();

    var secureSysVarRefTaskFormData = cephKeyProvider.generateKey(taskDefinitionKey, processInstanceId);

    var formData = getStringFormDataFromCeph(secureSysVarRefTaskFormData);
    if (formData.isEmpty()) {
      return;
    }

    userTaskDto.setData(formData.get().getData());
  }

  private void putStringFormDataToCeph(String secureSysVarRefTaskFormData, FormDataDto formData) {
    try {
      cephService.putFormData(secureSysVarRefTaskFormData, formData);
    } catch (CephCommunicationException ex) {
      log.warn("Couldn't put form data to ceph", ex);
      throw ex;
    }
  }

  private Optional<FormDataDto> getStringFormDataFromCeph(String secureSysVarRefTaskFormData) {
    try {
      return cephService.getFormData(secureSysVarRefTaskFormData);
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

  private void fillProcessDefinitionName(List<UserTaskDto> userTaskDtos) {
    List<String> processDefinitionIds = userTaskDtos.stream()
        .map(UserTaskDto::getProcessDefinitionId)
        .distinct().collect(Collectors.toList());
    var processDefinitionQueryDto = ProcessDefinitionQueryDto.builder()
        .processDefinitionIdIn(processDefinitionIds)
        .build();
    List<ProcessDefinitionDto> processDefinitions = processDefinitionRestClient
        .getProcessDefinitionsByParams(processDefinitionQueryDto);
    Map<String, ProcessDefinitionDto> processDefinitionIdAndDtoMap = processDefinitions.stream()
        .collect(Collectors.toMap(ProcessDefinitionDto::getId, Function.identity()));
    userTaskDtos.forEach(task -> {
      var processDefinitionDto = processDefinitionIdAndDtoMap
          .get(task.getProcessDefinitionId());
      if (Objects.nonNull(processDefinitionDto)) {
        task.setProcessDefinitionName(processDefinitionDto.getName());
      }
    });
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
    if (Objects.nonNull(assignee) && assignee.equals(currentUserName)) {
      return;
    }
    throw new UserTaskAuthorizationException(
        String.format(USER_TASK_AUTHORIZATION_ERROR_MSG, currentUserName, taskDto.getId()),
        taskDto.getId());
  }

  private TaskQueryDto prepareDefaultSearchTaskQuery() {
    return TaskQueryDto.builder()
        .orQueries(Collections.singletonList(
            TaskQueryDto.builder()
                .assignee(AuthUtil.getCurrentUsername())
                .unassigned(true)
                .build()))
        .build();
  }

  private void validateFormData(String formId, FormDataDto formDataDto) {
    var formValidationResponseDto = formValidationService.validateForm(formId, formDataDto);
    if (!formValidationResponseDto.isValid()) {
      throw new ValidationException(formValidationResponseDto.getError());
    }
  }
}
