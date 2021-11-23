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

import com.epam.digital.data.platform.bpms.api.dto.UserTaskDto;
import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.epam.digital.data.platform.usrtaskmgt.exception.SignatureValidationException;
import com.epam.digital.data.platform.usrtaskmgt.exception.UserTaskAlreadyAssignedException;
import com.epam.digital.data.platform.usrtaskmgt.exception.UserTaskNotExistsOrCompletedException;
import com.epam.digital.data.platform.usrtaskmgt.model.Pageable;
import com.epam.digital.data.platform.usrtaskmgt.model.SignableDataUserTaskDto;
import java.util.List;
import org.camunda.bpm.engine.rest.dto.CountResultDto;

/**
 * The UserTaskService class represents a service for {@link UserTaskDto} entity and
 * contains methods for working with a user tasks.
 */
public interface UserTaskService {

  /**
   * Method for getting list of user task entities.
   *
   * @param processInstanceId process instance identifier
   * @param page              specifies the index of the first result, the maximum number of results
   *                          and result sorting criteria and order
   * @return the list of user tasks
   */
  List<UserTaskDto> getTasks(String processInstanceId, Pageable page);

  /**
   * Method for getting user task entity that can be signed by id.
   *
   * @param taskId task identifier
   * @return the user task entity that can be signed
   */
  SignableDataUserTaskDto getTaskById(String taskId);

  /**
   * Method for getting the number of tasks.
   *
   * @return the number of tasks
   */
  CountResultDto countTasks();

  /**
   * Method should complete user task by id. Before completion, {@link FormDataDto} entity must be
   * saved to the ceph.
   *
   * @param taskId      task identifier
   * @param formDataDto data to save to the ceph
   */
  void completeTaskById(String taskId, FormDataDto formDataDto);

  /**
   * Method should verify {@link FormDataDto} entity and complete task by id. Before completion,
   * form data must be saved to the ceph. Performed by a user with the role of an officer.
   *
   * @param taskId      task identifier
   * @param formDataDto data to verify
   * @throws SignatureValidationException if the form data is invalid
   */
  void signOfficerForm(String taskId, FormDataDto formDataDto);

  /**
   * Method should verify {@link FormDataDto} entity and complete task by id. Before completion,
   * form data must be saved to the ceph. Performed by a user with the role of an citizen.
   *
   * @param taskId      task identifier
   * @param formDataDto data to verify
   * @throws SignatureValidationException if the form data is invalid
   */
  void signCitizenForm(String taskId, FormDataDto formDataDto);

  /**
   * Method should claim task by id.
   *
   * @param taskId task identifier
   * @throws UserTaskNotExistsOrCompletedException if the task not found
   * @throws UserTaskAlreadyAssignedException      if the task already assigned to other user
   */
  void claimTaskById(String taskId);
}
