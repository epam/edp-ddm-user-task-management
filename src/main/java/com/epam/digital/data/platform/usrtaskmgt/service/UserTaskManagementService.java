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

import com.epam.digital.data.platform.bpms.client.exception.TaskNotFoundException;
import com.epam.digital.data.platform.dso.api.dto.Subject;
import com.epam.digital.data.platform.starter.errorhandling.exception.ValidationException;
import com.epam.digital.data.platform.starter.validation.service.FormValidationService;
import com.epam.digital.data.platform.storage.base.exception.RepositoryCommunicationException;
import com.epam.digital.data.platform.storage.form.dto.FormDataDto;
import com.epam.digital.data.platform.storage.form.service.FormDataStorageService;
import com.epam.digital.data.platform.usrtaskmgt.exception.UserTaskAlreadyAssignedException;
import com.epam.digital.data.platform.usrtaskmgt.exception.UserTaskAuthorizationException;
import com.epam.digital.data.platform.usrtaskmgt.exception.UserTaskNotExistsException;
import com.epam.digital.data.platform.usrtaskmgt.exception.UserTaskNotExistsOrCompletedException;
import com.epam.digital.data.platform.usrtaskmgt.model.request.Pageable;
import com.epam.digital.data.platform.usrtaskmgt.model.response.CompletedTaskResponse;
import com.epam.digital.data.platform.usrtaskmgt.model.response.CountResponse;
import com.epam.digital.data.platform.usrtaskmgt.model.response.SignableDataUserTaskResponse;
import com.epam.digital.data.platform.usrtaskmgt.model.response.UserTaskResponse;
import com.epam.digital.data.platform.usrtaskmgt.remote.DigitalSignatureRemoteService;
import com.epam.digital.data.platform.usrtaskmgt.remote.UserTaskRemoteService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Represents a service that contains methods for working with a user tasks.
 * <p>
 * Implements such business functions:
 * <li>{@link UserTaskManagementService#getTasks(String, Pageable, Authentication) get tasks for
 * process instance}</li>
 * <li>{@link UserTaskManagementService#countTasks(Authentication) count all user tasks}</li>
 * <li>{@link UserTaskManagementService#claimTaskById(String, Authentication) claim user task by
 * current user}</li>
 * <li>{@link UserTaskManagementService#getTaskById(String, Authentication) get one task by
 * id}</li>
 * <li>{@link UserTaskManagementService#completeTaskById(String, FormDataDto, Authentication)
 * complete non signable task}</li>
 * <li>{@link UserTaskManagementService#signOfficerForm(String, FormDataDto, Authentication)
 * complete signable task with officer signature validation}</li>
 * <li>{@link UserTaskManagementService#signCitizenForm(String, FormDataDto, Authentication)
 * complete signable task with citizen signature validation}</li>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserTaskManagementService {

  private static final LinkedHashMap<String, Object> EMPTY_FORM_DATA = new LinkedHashMap<>();

  private final UserTaskRemoteService userTaskRemoteService;
  private final DigitalSignatureRemoteService digitalSignatureRemoteService;
  private final FormDataStorageService formDataStorageService;
  private final FormValidationService formValidationService;

  /**
   * Getting list of user task entities of particular process instance (if process instance isn't
   * present then gets all user tasks)
   *
   * @param processInstanceId process instance identifier (nullable)
   * @param page              specifies the index of the first result, the maximum number of results
   *                          and result sorting criteria and order
   * @param authentication    authentication object of current authenticated user
   * @return the list of user tasks
   */
  @NonNull
  public List<UserTaskResponse> getTasks(@Nullable String processInstanceId, @NonNull Pageable page,
      @NonNull Authentication authentication) {
    log.info("Getting unfinished user tasks for process instance {}. Parameters: {}",
        processInstanceId, page);

    var tasks = userTaskRemoteService.getUserTasks(processInstanceId, authentication.getName(),
        page);
    log.trace("Found user tasks - {}", tasks);

    log.info("Found {} user tasks", tasks.size());
    return tasks;
  }

  /**
   * Getting the number of user tasks
   *
   * @param authentication authentication object of current authenticated user
   * @return dto with the count of tasks
   */
  @NonNull
  public CountResponse countTasks(@NonNull Authentication authentication) {
    log.info("Getting unfinished user task count");

    var result = userTaskRemoteService.countUserTasks(authentication.getName());

    log.info("Getting unfinished user task count finished - {}", result.getCount());
    return result;
  }

  /**
   * Claiming user task by id with current user
   *
   * @param taskId         task identifier
   * @param authentication authentication object of current authenticated user
   * @throws UserTaskNotExistsOrCompletedException if the task not found (it never existed or
   *                                               already completed)
   * @throws UserTaskAlreadyAssignedException      if the task already assigned to other user
   */
  public void claimTaskById(@NonNull String taskId, @NonNull Authentication authentication) {
    log.info("Claiming task with id {}", taskId);

    var userTask = getUserTaskOrThrowTaskNotExistsOrCompletedException(taskId);
    log.trace("User task {} was found. {}", taskId, userTask);

    var currentUserName = authentication.getName();
    throwExceptionIfUserTaskIsAssignedToDifferentUser(userTask, currentUserName);
    log.trace("Verified that user task {} is not assigned to different user", taskId);

    userTaskRemoteService.assignUserTask(taskId, currentUserName);
    log.info("Task {} was claimed", taskId);
  }

  /**
   * Getting user task entity by id with form data pre-population if task's assigned to current
   * user
   *
   * @param taskId         task identifier
   * @param authentication authentication object of current authenticated user
   * @return the user task entity with form data pre-population
   * @throws UserTaskNotExistsException     if user task wasn't found
   * @throws UserTaskAuthorizationException if task is assigned to other user
   */
  @NonNull
  public SignableDataUserTaskResponse getTaskById(@NonNull String taskId,
      @NonNull Authentication authentication) {
    log.info("Getting unfinished user task by id {}", taskId);

    var userTaskDto = getUserTaskOrThrowTaskNotExistsException(taskId);
    log.trace("Task was found in bpms {}", userTaskDto);

    throwExceptionIfUserTaskIsNotAssignedToCurrentUser(userTaskDto, authentication.getName());

    var taskDefinitionKey = userTaskDto.getTaskDefinitionKey();
    var processInstanceId = userTaskDto.getProcessInstanceId();
    var data = getFormDataFromCeph(taskDefinitionKey, processInstanceId);
    log.trace("Form data pre-population is found");

    userTaskDto.setData(data.map(FormDataDto::getData).orElse(EMPTY_FORM_DATA));
    log.info("Unfinished user task by id {} is found", taskId);
    return userTaskDto;
  }

  /**
   * Completing user task by id with no signature verification
   *
   * @param taskId         task identifier
   * @param formData       data to save to the ceph
   * @param authentication authentication object of current authenticated user
   * @return {@link CompletedTaskResponse}
   *
   * @see UserTaskManagementService#completeTask(String, FormDataDto, Authentication,
   * SignatureVerifier) Task completion method itself
   */
  public CompletedTaskResponse completeTaskById(@NonNull String taskId,
      @NonNull FormDataDto formData, @NonNull Authentication authentication) {
    log.info("Completing user task with id {}", taskId);

    var result = completeTask(taskId, formData, authentication,
        SignatureVerifier.NO_SIGNATURE_VERIFICATION);

    log.info("Task {} is completed", taskId);
    return result;
  }

  /**
   * Completing user task by id with officer signature verification
   *
   * @param taskId         task identifier
   * @param formData       data to save to the ceph
   * @param authentication authentication object of current authenticated user
   * @return {@link CompletedTaskResponse}
   *
   * @see UserTaskManagementService#completeTask(String, FormDataDto, Authentication,
   * SignatureVerifier) Task completion method itself
   */
  public CompletedTaskResponse signOfficerForm(@NonNull String taskId,
      @NonNull FormDataDto formData,
      @NonNull Authentication authentication) {
    log.info("Completing signable officer task with id {}", taskId);

    var result = completeTask(taskId, formData, authentication,
        (taskDto, data) -> digitalSignatureRemoteService.verifyOfficerFormData(data));

    log.info("Signable officer task {} is completed", taskId);
    return result;
  }

  /**
   * Completing user task by id with citizen signature verification
   *
   * @param taskId         task identifier
   * @param formData       data to save to the ceph
   * @param authentication authentication object of current authenticated user
   * @return {@link CompletedTaskResponse}
   *
   * @see UserTaskManagementService#completeTask(String, FormDataDto, Authentication,
   * SignatureVerifier) Task completion method itself
   */
  public CompletedTaskResponse signCitizenForm(@NonNull String taskId,
      @NonNull FormDataDto formData,
      @NonNull Authentication authentication) {
    log.info("Completing signable citizen task with id {}", taskId);

    var result = completeTask(taskId, formData, authentication,
        digitalSignatureRemoteService::verifyCitizenFormData);

    log.info("Signable citizen task {} is completed", taskId);
    return result;
  }

  /**
   * Base method that completes user task completion.
   * <p>
   * Performs:
   * <ol>
   * <li>Finds user task by id</li>
   * <li>Verifies if user task is assigned to current user</li>
   * <li>Validates user task form data</li>
   * <li>Verifies signature by input signature verifier</li>
   * <li>Saves form data to form data storage</li>
   * <li>Completes the user task in Camunda</li>
   * </ol>
   *
   * @param taskId            task identifier
   * @param formData          data to save to the ceph
   * @param authentication    authentication object of current authenticated user
   * @param signatureVerifier object that performs signature verification
   * @throws UserTaskNotExistsException     if user task wasn't found
   * @throws UserTaskAuthorizationException if task is assigned to other user
   * @throws ValidationException            if form data hasn't passed the validation
   * @return {@link CompletedTaskResponse}
   */
  private CompletedTaskResponse completeTask(String taskId, FormDataDto formData,
      Authentication authentication, SignatureVerifier signatureVerifier) {
    log.debug("Completing user task {}", taskId);

    var taskDto = getUserTaskOrThrowTaskNotExistsException(taskId);
    log.trace("User task {} was found", taskId);

    var currentUserName = authentication.getName();
    throwExceptionIfUserTaskIsNotAssignedToCurrentUser(taskDto, currentUserName);
    log.trace("Verified that user task {} is assigned to {}", taskDto.getId(), currentUserName);

    validateFormData(taskDto.getFormKey(), formData);
    log.trace("Form data has passed the validation");

    signatureVerifier.verify(taskDto.getSignatureValidationPack(), formData);
    log.trace("Form data has passed the signature verification if there was any");

    formData.setAccessToken((String) authentication.getCredentials());
    formDataStorageService.putFormData(taskDto.getTaskDefinitionKey(),
        taskDto.getProcessInstanceId(),
        formData);
    log.trace("Form data is saved");

    var result = userTaskRemoteService.completeTaskById(taskId);

    log.debug("User task {} successfully completed", taskId);
    return result;
  }

  private void throwExceptionIfUserTaskIsNotAssignedToCurrentUser(
      SignableDataUserTaskResponse taskDto,
      String currentUserName) {
    var assignee = taskDto.getAssignee();

    log.debug("Checking if user task {} assignee {} is equal to current user {}",
        taskDto.getId(), assignee, currentUserName);
    if (Objects.isNull(assignee) || !assignee.equals(currentUserName)) {
      var message = String.format(
          "The user with username %s does not have permission on resource Task with id %s",
          currentUserName, taskDto.getId());
      throw new UserTaskAuthorizationException(message, taskDto.getId());
    }
  }

  private SignableDataUserTaskResponse getUserTaskOrThrowTaskNotExistsOrCompletedException(
      String taskId) {
    try {
      return userTaskRemoteService.getUserTaskById(taskId);
    } catch (TaskNotFoundException ex) {
      throw new UserTaskNotExistsOrCompletedException(ex);
    }
  }

  private SignableDataUserTaskResponse getUserTaskOrThrowTaskNotExistsException(String taskId) {
    try {
      return userTaskRemoteService.getUserTaskById(taskId);
    } catch (TaskNotFoundException ex) {
      throw new UserTaskNotExistsException(taskId, ex);
    }
  }

  private void throwExceptionIfUserTaskIsAssignedToDifferentUser(
      SignableDataUserTaskResponse userTask, String currentUserName) {
    var taskAssignee = userTask.getAssignee();

    log.debug("Checking if user task {} assignee {} is empty or not equals to current user {}",
        userTask.getId(), taskAssignee, currentUserName);
    if (!StringUtils.isEmpty(taskAssignee) && !taskAssignee.equals(currentUserName)) {
      throw new UserTaskAlreadyAssignedException(userTask.getName(), "Task already assigned");
    }
  }

  private void validateFormData(String formId, FormDataDto formDataDto) {
    var formValidationResponseDto = formValidationService.validateForm(formId, formDataDto);
    if (!formValidationResponseDto.isValid()) {
      throw new ValidationException(formValidationResponseDto.getError());
    }
  }

  private Optional<FormDataDto> getFormDataFromCeph(String taskDefinitionKey,
      String processInstanceId) {
    try {
      return formDataStorageService.getFormData(taskDefinitionKey, processInstanceId);
    } catch (RepositoryCommunicationException ex) {
      log.warn("Couldn't get form data by task definition {} and process instance id {} from ceph",
          taskDefinitionKey, processInstanceId, ex);
      return Optional.empty();
    }
  }

  @FunctionalInterface
  private interface SignatureVerifier {

    SignatureVerifier NO_SIGNATURE_VERIFICATION = (subjects, formData) -> {
    };

    void verify(Set<Subject> allowedSubjects, FormDataDto formData);
  }
}
