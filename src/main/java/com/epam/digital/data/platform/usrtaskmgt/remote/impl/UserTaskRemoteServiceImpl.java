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

package com.epam.digital.data.platform.usrtaskmgt.remote.impl;

import com.epam.digital.data.platform.bpms.api.dto.ClaimTaskDto;
import com.epam.digital.data.platform.bpms.api.dto.PaginationQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.SortingDto;
import com.epam.digital.data.platform.bpms.api.dto.TaskCountQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.TaskQueryDto;
import com.epam.digital.data.platform.bpms.client.CamundaTaskRestClient;
import com.epam.digital.data.platform.bpms.client.ExtendedUserTaskRestClient;
import com.epam.digital.data.platform.usrtaskmgt.mapper.UserTaskDtoMapper;
import com.epam.digital.data.platform.usrtaskmgt.model.request.Pageable;
import com.epam.digital.data.platform.usrtaskmgt.model.response.CountResponse;
import com.epam.digital.data.platform.usrtaskmgt.model.response.SignableDataUserTaskResponse;
import com.epam.digital.data.platform.usrtaskmgt.model.response.UserTaskResponse;
import com.epam.digital.data.platform.usrtaskmgt.remote.UserTaskRemoteService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.rest.dto.task.CompleteTaskDto;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserTaskRemoteServiceImpl implements UserTaskRemoteService {

  private final CamundaTaskRestClient camundaTaskRestClient;
  private final ExtendedUserTaskRestClient extendedUserTaskRestClient;
  private final UserTaskDtoMapper userTaskDtoMapper;

  @Override
  @NonNull
  public List<UserTaskResponse> getUserTasks(@Nullable String processInstanceId,
      @NonNull String assignee, @NonNull Pageable page) {
    log.debug("Getting assigned to current user or unassigned user tasks of process instance {}. "
        + "Paging and sorting params - {}", processInstanceId, page);

    var unassignedTaskQuery = TaskQueryDto.builder()
        .unassigned(true)
        .assignee(assignee)
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

    var dtos = extendedUserTaskRestClient.getTasksByParams(taskQueryDto, paginationQueryDto);

    log.debug("{} user tasks were found", dtos.size());
    return userTaskDtoMapper.toUserTaskDtoList(dtos);
  }

  @Override
  @NonNull
  public CountResponse countUserTasks(@NonNull String assignee) {
    log.debug("Counting assigned to current user or unassigned user tasks");

    var unassignedCountTaskQuery = TaskCountQueryDto.builder()
        .assignee(assignee)
        .unassigned(true)
        .build();
    var taskCountQueryDto = TaskCountQueryDto.builder()
        .orQueries(List.of(unassignedCountTaskQuery))
        .build();
    var dto = camundaTaskRestClient.getTaskCountByParams(taskCountQueryDto);

    log.debug("Found {} user tasks", dto.getCount());
    return userTaskDtoMapper.toCountResponse(dto);
  }

  @Override
  @NonNull
  public SignableDataUserTaskResponse getUserTaskById(@NonNull String taskId) {
    log.debug("Selecting user task by id {}", taskId);

    var taskDto = extendedUserTaskRestClient.getUserTaskById(taskId);
    log.trace("User task {} was found - {}", taskId, taskDto);

    var userTask = userTaskDtoMapper.toSignableDataUserTaskDto(taskDto);

    log.trace("User task was mapped to user task dto {}", userTask);
    return userTask;
  }

  @Override
  public void assignUserTask(@NonNull String taskId, @NonNull String userName) {
    log.debug("Claiming task with id {} to {}", taskId, userName);

    var claimTaskDto = ClaimTaskDto.builder()
        .userId(userName)
        .build();
    camundaTaskRestClient.claimTaskById(taskId, claimTaskDto);

    log.debug("Task with id {} was claimed by {}", taskId, userName);
  }

  @Override
  public void completeTaskById(@NonNull String taskId) {
    log.debug("Completing task with id {}", taskId);
    camundaTaskRestClient.completeTaskById(taskId, new CompleteTaskDto());
    log.debug("Task with id {} was completed", taskId);
  }
}
