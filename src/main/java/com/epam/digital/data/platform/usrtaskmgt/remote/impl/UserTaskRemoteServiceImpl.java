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

import com.epam.digital.data.platform.bpms.api.dto.DdmClaimTaskQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmCompleteTaskDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmTaskCountQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmTaskQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.PaginationQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.SortingDto;
import com.epam.digital.data.platform.bpms.client.TaskRestClient;
import com.epam.digital.data.platform.usrtaskmgt.mapper.UserTaskDtoMapper;
import com.epam.digital.data.platform.usrtaskmgt.model.request.Pageable;
import com.epam.digital.data.platform.usrtaskmgt.model.response.CompletedTaskResponse;
import com.epam.digital.data.platform.usrtaskmgt.model.response.CountResponse;
import com.epam.digital.data.platform.usrtaskmgt.model.response.SignableDataUserTaskResponse;
import com.epam.digital.data.platform.usrtaskmgt.model.response.UserTaskLightweightResponse;
import com.epam.digital.data.platform.usrtaskmgt.model.response.UserTaskResponse;
import com.epam.digital.data.platform.usrtaskmgt.remote.UserTaskRemoteService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserTaskRemoteServiceImpl implements UserTaskRemoteService {

  private final TaskRestClient taskRestClient;
  private final UserTaskDtoMapper userTaskDtoMapper;

  @Override
  @NonNull
  public List<UserTaskResponse> getUserTasks(@Nullable String processInstanceId,
      @NonNull String assignee, @NonNull Pageable page) {
    log.debug("Getting assigned to current user or unassigned user tasks of process instance {}. "
        + "Paging and sorting params - {}", processInstanceId, page);

    var taskQueryDto = buildDdmTaskQueryDto(assignee, page);
    taskQueryDto.setProcessInstanceId(processInstanceId);
    var paginationQueryDto = PaginationQueryDto.builder()
        .firstResult(page.getFirstResult())
        .maxResults(page.getMaxResults())
        .build();

    var dtos = taskRestClient.getTasksByParams(taskQueryDto, paginationQueryDto);

    log.debug("{} user tasks were found", dtos.size());
    return userTaskDtoMapper.toUserTaskDtoList(dtos);
  }

  @Override
  @NonNull
  public List<UserTaskLightweightResponse> getLightweightUserTasks(String rootProcessInstanceId,
      String assignee, Pageable page) {
    log.debug("Getting assigned to current user or unassigned lightweight user tasks of "
        + "root process instance {}. Paging and sorting params - {}", rootProcessInstanceId, page);

    var taskQueryDto = buildDdmTaskQueryDto(assignee, page);
    taskQueryDto.setRootProcessInstanceId(rootProcessInstanceId);
    var paginationQueryDto = PaginationQueryDto.builder()
        .firstResult(page.getFirstResult())
        .maxResults(page.getMaxResults())
        .build();

    var dtos = taskRestClient.getLightweightTasksByParams(taskQueryDto, paginationQueryDto);

    log.debug("{} user tasks were found", dtos.size());
    return userTaskDtoMapper.toUserTaskLightweightResponse(dtos);
  }

  @Override
  @NonNull
  public CountResponse countUserTasks(@NonNull String assignee) {
    log.debug("Counting assigned to current user or unassigned user tasks");

    var unassignedCountTaskQuery = DdmTaskCountQueryDto.builder()
        .assignee(assignee)
        .unassigned(true)
        .build();
    var taskCountQueryDto = DdmTaskCountQueryDto.builder()
        .orQueries(List.of(unassignedCountTaskQuery))
        .build();
    var dto = taskRestClient.getTaskCountByParams(taskCountQueryDto);

    log.debug("Found {} user tasks", dto.getCount());
    return userTaskDtoMapper.toCountResponse(dto);
  }

  @Override
  @NonNull
  public SignableDataUserTaskResponse getUserTaskById(@NonNull String taskId) {
    log.debug("Selecting user task by id {}", taskId);

    var taskDto = taskRestClient.getTaskById(taskId);
    log.trace("User task {} was found - {}", taskId, taskDto);

    var userTask = userTaskDtoMapper.toSignableDataUserTaskDto(taskDto);

    log.debug("User task by id {} selected. {}", taskId, userTask);
    return userTask;
  }

  @Override
  public void assignUserTask(@NonNull String taskId, @NonNull String userName) {
    log.debug("Claiming task with id {} to {}", taskId, userName);

    var claimTaskDto = DdmClaimTaskQueryDto.builder()
        .userId(userName)
        .build();
    taskRestClient.claimTaskById(taskId, claimTaskDto);

    log.debug("Task with id {} was claimed by {}", taskId, userName);
  }

  @Override
  public CompletedTaskResponse completeTaskById(@NonNull String taskId) {
    log.debug("Completing task with id {}", taskId);

    var result = taskRestClient.completeTaskById(taskId, DdmCompleteTaskDto.builder().build());

    log.debug("Task with id {} was completed", taskId);
    return userTaskDtoMapper.toCompletedTaskResponse(result);
  }

  private DdmTaskQueryDto buildDdmTaskQueryDto(String assignee, Pageable page) {
    var unassignedTaskQuery = DdmTaskQueryDto.builder()
        .unassigned(true)
        .assignee(assignee)
        .build();
    var sortingDto = SortingDto.builder()
        .sortBy(page.getSortBy())
        .sortOrder(page.getSortOrder())
        .build();
    return DdmTaskQueryDto.builder()
        .orQueries(List.of(unassignedTaskQuery))
        .sorting(List.of(sortingDto))
        .build();
  }
}
