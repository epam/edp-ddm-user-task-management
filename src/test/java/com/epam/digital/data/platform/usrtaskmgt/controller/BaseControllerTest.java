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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;

import com.epam.digital.data.platform.bpms.client.exception.InternalServerErrorException;
import com.epam.digital.data.platform.bpms.client.exception.TaskNotFoundException;
import com.epam.digital.data.platform.starter.errorhandling.BaseRestExceptionHandler;
import com.epam.digital.data.platform.starter.errorhandling.dto.SystemErrorDto;
import com.epam.digital.data.platform.starter.localization.MessageResolver;
import com.epam.digital.data.platform.usrtaskmgt.controller.config.CustomMockMvcConfigurer;
import com.epam.digital.data.platform.usrtaskmgt.i18n.UserTaskManagementMessage;
import com.epam.digital.data.platform.usrtaskmgt.exception.UserTaskAlreadyAssignedException;
import com.epam.digital.data.platform.usrtaskmgt.exception.UserTaskNotExistsOrCompletedException;
import com.epam.digital.data.platform.usrtaskmgt.model.request.Pageable;
import com.epam.digital.data.platform.usrtaskmgt.model.response.CountResponse;
import com.epam.digital.data.platform.usrtaskmgt.model.response.HistoryUserTaskResponse;
import com.epam.digital.data.platform.usrtaskmgt.model.response.SignableDataUserTaskResponse;
import com.epam.digital.data.platform.usrtaskmgt.model.response.UserTaskResponse;
import com.epam.digital.data.platform.usrtaskmgt.service.HistoryUserTaskManagementService;
import com.epam.digital.data.platform.usrtaskmgt.service.UserTaskManagementService;
import com.google.common.collect.ImmutableMap;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
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
  private UserTaskManagementService userTaskManagementService;
  @Mock
  private HistoryUserTaskManagementService historyUserTaskManagementService;
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
    lenient().when(userTaskManagementService.countTasks(any())).thenReturn(new CountResponse(22L));
  }

  public void mockGetById() {
    var taskById = new SignableDataUserTaskResponse("testId", "taskDefinitionKey", "testTaskName",
        "testAssignee", LocalDateTime.of(LocalDate.of(2020, 12, 12), LocalTime.of(13, 3, 22)),
        "testDesc", "testProcessInstanceId", "testProcessDefinitionId", "testProcess",
        "testFormKey", true, true,
        ImmutableMap.of("fullName", "FullName"), Set.of(), ImmutableMap.of("var1", 123123));

    lenient().when(userTaskManagementService.getTaskById(eq("testId"), any())).thenReturn(taskById);
  }

  public void mockGetTasks() {
    var task1 = UserTaskResponse.builder()
        .id("testId")
        .taskDefinitionKey("taskDefinitionKey")
        .name("testTaskName")
        .assignee("testAssignee")
        .created(LocalDateTime.of(2020, 12, 12, 13, 3, 22))
        .description("testDesc")
        .processDefinitionName("testProcessDefinitionName")
        .processInstanceId("testProcessInstanceId")
        .processDefinitionId("testProcessDefinitionId")
        .formKey("testFormKey")
        .suspended(false)
        .build();
    var task2 = UserTaskResponse.builder()
        .id("testId2")
        .taskDefinitionKey("taskDefinitionKey")
        .name("testTaskName2")
        .assignee("testAssignee2")
        .created(LocalDateTime.of(2020, 12, 12, 13, 3, 22))
        .description("testDesc2")
        .processDefinitionName("testProcessDefinitionName2")
        .processInstanceId("testProcessInstanceId2")
        .processDefinitionId("testProcessDefinitionId2")
        .formKey("testFormKey2")
        .suspended(true)
        .build();

    lenient().when(userTaskManagementService.getTasks(eq(null), eq(Pageable.builder().build()), any()))
        .thenReturn(List.of(task1, task2));
  }

  public void mockGetTasksByProcessInstanceId() {
    var task = UserTaskResponse.builder()
        .id("testId")
        .taskDefinitionKey("taskDefinitionKey")
        .name("testTaskName")
        .assignee("testAssignee")
        .created(LocalDateTime.of(2020, 12, 12, 13, 3, 22))
        .description("testDesc")
        .processDefinitionName("testProcessDefinitionName")
        .processInstanceId("testProcessInstanceId")
        .processDefinitionId("testProcessDefinitionId")
        .formKey("testFormKey")
        .suspended(false)
        .build();
    lenient().when(
            userTaskManagementService.getTasks(eq("testProcessInstanceId"), eq(Pageable.builder().build()), any()))
        .thenReturn(List.of(task));
  }

  public void mockGetHistoryTasks() {
    var task1 = HistoryUserTaskResponse.builder()
        .id("testId")
        .name("testTaskName")
        .assignee("testAssignee")
        .startTime(LocalDateTime.of(2020, 12, 12, 13, 3, 22))
        .endTime(LocalDateTime.of(2020, 12, 12, 13, 3, 22))
        .description("testDesc")
        .processDefinitionName("testProcessDefinitionName")
        .processInstanceId("testProcessInstanceId")
        .processDefinitionId("testProcessDefinitionId")
        .build();
    var task2 = HistoryUserTaskResponse.builder()
        .id("testId2")
        .name("testTaskName2")
        .assignee("testAssignee2")
        .startTime(LocalDateTime.of(2020, 12, 12, 13, 3, 22))
        .endTime(LocalDateTime.of(2020, 12, 12, 13, 3, 22))
        .description("testDesc2")
        .processDefinitionName("testProcessDefinitionName2")
        .processInstanceId("testProcessInstanceId2")
        .processDefinitionId("testProcessDefinitionId2")
        .build();
    lenient().when(historyUserTaskManagementService.getHistoryTasks(eq(Pageable.builder().build()), any()))
        .thenReturn(List.of(task1, task2));
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
        .when(userTaskManagementService)
        .claimTaskById(eq("testId404"), any());

    lenient().when(messageResolver.getMessage(
        UserTaskManagementMessage.USER_TASK_ALREADY_ASSIGNED, "userTask"))
        .thenReturn("409 localizedMessage");

    lenient().doThrow(new UserTaskAlreadyAssignedException("userTask", "409 message"))
        .when(userTaskManagementService)
        .claimTaskById(eq("testId409"), any());

    lenient().doThrow(
            new InternalServerErrorException(
                SystemErrorDto.builder()
                    .traceId("traceId")
                    .code("code500")
                    .message("500 message")
                    .localizedMessage("500 localizedMessage")
                    .build()))
        .when(userTaskManagementService)
        .claimTaskById(eq("testId500"), any());
  }
}
