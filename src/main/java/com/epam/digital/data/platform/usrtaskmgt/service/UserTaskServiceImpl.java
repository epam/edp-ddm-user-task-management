/*
 * Copyright 2021 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.usrtaskmgt.service;

import com.epam.digital.data.platform.bpms.api.dto.ClaimTaskDto;
import com.epam.digital.data.platform.bpms.api.dto.PaginationQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.SortingDto;
import com.epam.digital.data.platform.bpms.api.dto.TaskCountQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.TaskQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.UserTaskDto;
import com.epam.digital.data.platform.bpms.client.CamundaTaskRestClient;
import com.epam.digital.data.platform.bpms.client.ExtendedUserTaskRestClient;
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
import com.epam.digital.data.platform.usrtaskmgt.model.SignableDataUserTaskDto;
import com.epam.digital.data.platform.usrtaskmgt.util.CephKeyProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.task.CompleteTaskDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserTaskServiceImpl implements UserTaskService {

  private static final String USER_TASK_AUTHORIZATION_ERROR_MSG = "The user with username %s does not have permission on resource Task with id %s";

  private final CamundaTaskRestClient camundaTaskRestClient;
  private final ExtendedUserTaskRestClient extendedUserTaskRestClient;
  private final DigitalSignatureRestClient digitalSignatureRestClient;

  private final FormDataCephService cephService;

  private final UserTaskDtoMapper userTaskDtoMapper;
  private final ObjectMapper objectMapper;
  private final CephKeyProvider cephKeyProvider;
  private final FormValidationService formValidationService;

  @Override
  public List<UserTaskDto> getTasks(String processInstanceId, Pageable page,
      Authentication authentication) {
    log.info("Getting unfinished user tasks for process instance {}. Parameters: {}",
        processInstanceId, page);
    var tasks = getTaskDtoList(processInstanceId, page, authentication);
    log.trace("Found user tasks - {}", tasks);
    log.info("Found {} user tasks", tasks.size());
    return tasks;
  }

  @Override
  public SignableDataUserTaskDto getTaskById(String taskId, Authentication authentication) {
    log.info("Getting unfinished user task by id {}", taskId);

    var userTaskDto = getUserTaskById(taskId);
    log.trace("Task was found in bpms");
    verifyAssignee(userTaskDto, authentication);

    var data = getFormData(userTaskDto.getProcessInstanceId(), userTaskDto.getTaskDefinitionKey());
    userTaskDto.setData(data);
    log.trace("Form data pre-population is found.");

    log.info("Unfinished user task by id {} is found", taskId);
    return userTaskDto;
  }

  @Override
  public CountResultDto countTasks(Authentication authentication) {
    log.info("Getting unfinished user task count");
    var unassignedCountTaskQuery = TaskCountQueryDto.builder()
        .assignee(authentication.getName())
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
  public void completeTaskById(String taskId, FormDataDto formData, Authentication authentication) {
    log.info("Completing user task with id {}", taskId);
    completeTask(authentication, taskId, formData, (userTaskDto, data) -> {
    });
    log.info("Task {} is completed", taskId);
  }

  @Override
  public void signOfficerForm(String taskId, FormDataDto formData, Authentication authentication) {
    log.info("Completing signable officer task with id {}", taskId);
    completeTask(authentication, taskId, formData, (taskDto, data) -> verifyOfficerFormData(data));
    log.info("Signable officer task {} is completed", taskId);
  }

  @Override
  public void signCitizenForm(String taskId, FormDataDto formData, Authentication authentication) {
    log.info("Completing signable citizen task with id {}", taskId);
    completeTask(authentication, taskId, formData, this::verifyCitizenFormData);
    log.info("Signable citizen task {} is completed", taskId);
  }

  @Override
  public void claimTaskById(String taskId, Authentication authentication) {
    log.info("Claiming task with id {}", taskId);
    var currentUserName = authentication.getName();
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

  private List<UserTaskDto> getTaskDtoList(String processInstanceId, Pageable page,
      Authentication authentication) {
    log.debug("Getting assigned to current user or unassigned user tasks of process instance {}. "
        + "Paging and sorting params - {}", processInstanceId, page);
    var unassignedTaskQuery = TaskQueryDto.builder()
        .unassigned(true)
        .assignee(authentication.getName())
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
    log.debug("Verifying officer signed form data.");
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

  private void verifyCitizenFormData(SignableDataUserTaskDto taskDto, FormDataDto formData) {
    log.debug("Verifying citizen signed form data.");
    var data = serializeFormData(formData.getData());
    var signature = formData.getSignature();
    var allowedSubjects = taskDto.getSignatureValidationPack();
    log.trace("Found subjects - {}", allowedSubjects);

    if (allowedSubjects.isEmpty()) {
      allowedSubjects = Set.of(Subject.INDIVIDUAL);
    }

    var verifyResponseDto = digitalSignatureRestClient.verifyCitizen(
        new VerifySubjectRequestDto(new ArrayList<>(allowedSubjects), signature, data));
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

  private void completeTask(Authentication authentication, String taskId, FormDataDto formData,
      BiConsumer<SignableDataUserTaskDto, FormDataDto> verifyingConsumer) {
    log.debug("Completing user task {}", taskId);

    var taskDto = getUserTaskById(taskId);
    log.trace("User task {} was found", taskId);

    validateFormData(taskDto.getFormKey(), formData);
    log.trace("Form data has passed the validation");

    verifyingConsumer.accept(taskDto, formData);
    log.trace("Form data has passed the signature verification if there was any");

    verifyAssignee(taskDto, authentication);
    log.trace("Verified if user task assignee is current user.");

    saveFormData(taskDto, formData, authentication);
    log.trace("Form data is saved to ceph");

    camundaTaskRestClient.completeTaskById(taskId, new CompleteTaskDto());
    log.debug("User task {} successfully completed", taskId);
  }

  private void saveFormData(SignableDataUserTaskDto taskDto, FormDataDto formData,
      Authentication authentication) {
    formData.setAccessToken((String) authentication.getCredentials());
    var processInstanceId = taskDto.getProcessInstanceId();
    var taskDefinitionKey = taskDto.getTaskDefinitionKey();

    var formDataCephKey = cephKeyProvider.generateKey(taskDefinitionKey, processInstanceId);

    putFormDataToCeph(formDataCephKey, formData);
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

  private SignableDataUserTaskDto getUserTaskById(String taskId) {
    try {
      var userTask = extendedUserTaskRestClient.getUserTaskById(taskId);
      return userTaskDtoMapper.toSignableDataUserTaskDto(userTask);
    } catch (TaskNotFoundException ex) {
      throw new UserTaskNotExistsException(taskId, ex);
    }
  }

  private void verifyAssignee(SignableDataUserTaskDto taskDto, Authentication authentication) {
    var assignee = taskDto.getAssignee();
    var currentUserName = authentication.getName();
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
