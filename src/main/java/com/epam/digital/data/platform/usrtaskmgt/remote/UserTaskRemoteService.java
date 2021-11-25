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

package com.epam.digital.data.platform.usrtaskmgt.remote;

import com.epam.digital.data.platform.bpms.api.dto.UserTaskDto;
import com.epam.digital.data.platform.bpms.client.exception.TaskNotFoundException;
import com.epam.digital.data.platform.usrtaskmgt.model.request.Pageable;
import com.epam.digital.data.platform.usrtaskmgt.model.response.CountResponse;
import com.epam.digital.data.platform.usrtaskmgt.model.response.SignableDataUserTaskResponse;
import com.epam.digital.data.platform.usrtaskmgt.model.response.UserTaskResponse;
import java.util.List;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Service for {@link UserTaskDto} entity. Contains methods for accessing user tasks.
 */
public interface UserTaskRemoteService {

  /**
   * Getting assigned to current user or unassigned user tasks of specific process instance (if it's
   * present, else will be returned list of tasks for all process instances)
   *
   * @param processInstanceId id of current process instance
   * @param page              paging and sorting properties
   * @return the list of user tasks
   */
  @NonNull
  List<UserTaskResponse> getUserTasks(@Nullable String processInstanceId, @NonNull String assignee,
      @NonNull Pageable page);

  /**
   * Getting count of assigned to current user or unassigned user tasks
   *
   * @return the count of user tasks
   */
  @NonNull
  CountResponse countUserTasks(@NonNull String assignee);

  /**
   * Getting user task by id and verifying if current user should have access for the task (user has
   * access to task in camunda, but it's already assigned to another user)
   *
   * @param taskId id of current task
   * @return user task object
   *
   * @throws TaskNotFoundException if user task with this id is not exists
   */
  @NonNull
  SignableDataUserTaskResponse getUserTaskById(@NonNull String taskId);

  /**
   * Assigning user task to a user
   *
   * @param taskId   id of current task to claim
   * @param userName userName of the user that claims the task
   */
  void assignUserTask(@NonNull String taskId, @NonNull String userName);

  /**
   * Complete user task with specified id in Camunda
   *
   * @param taskId id of current task to complete
   */
  void completeTaskById(@NonNull String taskId);
}
