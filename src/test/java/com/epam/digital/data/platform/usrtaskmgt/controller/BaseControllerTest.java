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

package com.epam.digital.data.platform.usrtaskmgt.controller;

import static org.mockito.Mockito.lenient;

import com.epam.digital.data.platform.bpms.api.dto.HistoryUserTaskDto;
import com.epam.digital.data.platform.bpms.api.dto.UserTaskDto;
import com.epam.digital.data.platform.bpms.client.exception.InternalServerErrorException;
import com.epam.digital.data.platform.bpms.client.exception.TaskNotFoundException;
import com.epam.digital.data.platform.starter.errorhandling.BaseRestExceptionHandler;
import com.epam.digital.data.platform.starter.errorhandling.dto.SystemErrorDto;
import com.epam.digital.data.platform.starter.localization.MessageResolver;
import com.epam.digital.data.platform.usrtaskmgt.controller.config.CustomMockMvcConfigurer;
import com.epam.digital.data.platform.usrtaskmgt.enums.UserTaskManagementMessage;
import com.epam.digital.data.platform.usrtaskmgt.exception.UserTaskAlreadyAssignedException;
import com.epam.digital.data.platform.usrtaskmgt.exception.UserTaskNotExistsOrCompletedException;
import com.epam.digital.data.platform.usrtaskmgt.model.Pageable;
import com.epam.digital.data.platform.usrtaskmgt.model.SignableDataUserTaskDto;
import com.epam.digital.data.platform.usrtaskmgt.service.HistoryUserTaskService;
import com.epam.digital.data.platform.usrtaskmgt.service.UserTaskService;
import com.google.common.collect.ImmutableMap;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;
import org.assertj.core.util.Lists;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

@ExtendWith(MockitoExtension.class)
public abstract class BaseControllerTest {

  @InjectMocks
  private UserTaskController userTaskController;
  @InjectMocks
  private HistoryUserTaskController historyUserTaskController;

  @Mock
  private UserTaskService userTaskService;
  @Mock
  private HistoryUserTaskService historyUserTaskService;
  @Mock
  private MessageResolver messageResolver;

  @BeforeEach
  public void init() {
    MDC.put(BaseRestExceptionHandler.TRACE_ID_KEY, "traceId");

    RestAssuredMockMvc.standaloneSetup(
        userTaskController,
        historyUserTaskController,
        new CustomMockMvcConfigurer(messageResolver));
    mockCount();
    mockGetById();
    mockGetTasks();
    mockGetTasksByProcessInstanceId();
    mockGetHistoryTasks();
    mockClaimTaskById();
  }

  public void mockCount() {
    lenient().when(userTaskService.countTasks()).thenReturn(new CountResultDto(22L));
  }

  public void mockGetById() {
    var taskById = new SignableDataUserTaskDto("testId", "taskDefinitionKey", "testTaskName",
        "testAssignee", LocalDateTime.of(LocalDate.of(2020, 12, 12), LocalTime.of(13, 3, 22)),
        "testDesc", "testProcessInstanceId", "testProcessDefinitionId", "testProcess",
        "testFormKey", true, ImmutableMap.of("var1", 123123), true,
        ImmutableMap.of("fullName", "FullName"), Set.of());

    lenient().when(userTaskService.getTaskById("testId")).thenReturn(taskById);
  }

  public void mockGetTasks() {
    lenient().when(userTaskService.getTasks(null, Pageable.builder().build()))
        .thenReturn(Lists.newArrayList(
            new UserTaskDto("testId", "taskDefinitionKey", "testTaskName", "testAssignee",
                LocalDateTime.of(LocalDate.of(2020, 12, 12), LocalTime.of(13, 3, 22)), "testDesc",
                "testProcessDefinitionName", "testProcessInstanceId", "testProcessDefinitionId",
                "testFormKey", false),
            new UserTaskDto("testId2", "taskDefinitionKey", "testTaskName2", "testAssignee2",
                LocalDateTime.of(LocalDate.of(2020, 12, 12), LocalTime.of(13, 3, 22)), "testDesc2",
                "testProcessDefinitionName2", "testProcessInstanceId2", "testProcessDefinitionId2",
                "testFormKey2", true)));
  }

  public void mockGetTasksByProcessInstanceId() {
    lenient().when(userTaskService.getTasks("testProcessInstanceId", Pageable.builder().build()))
        .thenReturn(Lists.newArrayList(
            new UserTaskDto("testId", "taskDefinitionKey", "testTaskName", "testAssignee",
                LocalDateTime.of(LocalDate.of(2020, 12, 12), LocalTime.of(13, 3, 22)), "testDesc",
                "testProcessDefinitionName", "testProcessInstanceId", "testProcessDefinitionId",
                "testFormKey", false)));
  }

  public void mockGetHistoryTasks() {
    lenient().when(historyUserTaskService.getHistoryTasks(Pageable.builder().build()))
        .thenReturn(
            Lists.newArrayList(new HistoryUserTaskDto("testId", "testTaskName", "testAssignee",
                    LocalDateTime.of(LocalDate.of(2020, 12, 12), LocalTime.of(13, 3, 22)),
                    LocalDateTime.of(LocalDate.of(2020, 12, 12), LocalTime.of(13, 3, 22)),
                    "testDesc", "testProcessDefinitionName", "testProcessInstanceId",
                    "testProcessDefinitionId"),
                new HistoryUserTaskDto("testId2", "testTaskName2", "testAssignee2",
                    LocalDateTime.of(LocalDate.of(2020, 12, 12), LocalTime.of(13, 3, 22)),
                    LocalDateTime.of(LocalDate.of(2020, 12, 12), LocalTime.of(13, 3, 22)),
                    "testDesc2", "testProcessDefinitionName2", "testProcessInstanceId2",
                    "testProcessDefinitionId2")));
  }

  public void mockClaimTaskById() {
    lenient().when(
        messageResolver.getMessage(UserTaskManagementMessage.USER_TASK_NOT_EXISTS_OR_COMPLETED))
        .thenReturn("404 localizedMessage");

    lenient().doThrow(
        new UserTaskNotExistsOrCompletedException(
            new TaskNotFoundException(
                SystemErrorDto.builder()
                    .traceId("traceId")
                    .code("code404")
                    .message("404 message")
                    .localizedMessage("localized message")
                    .build())))
        .when(userTaskService)
        .claimTaskById("testId404");

    lenient().when(messageResolver.getMessage(
        UserTaskManagementMessage.USER_TASK_ALREADY_ASSIGNED, "userTask"))
        .thenReturn("409 localizedMessage");

    lenient().doThrow(new UserTaskAlreadyAssignedException("userTask", "409 message"))
        .when(userTaskService)
        .claimTaskById("testId409");

    lenient().doThrow(
        new InternalServerErrorException(
            SystemErrorDto.builder()
                .traceId("traceId")
                .code("code500")
                .message("500 message")
                .localizedMessage("500 localizedMessage")
                .build()))
        .when(userTaskService)
        .claimTaskById("testId500");
  }
}
