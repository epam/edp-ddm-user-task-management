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

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.digital.data.platform.bpms.api.dto.DdmTaskDto;
import com.epam.digital.data.platform.starter.errorhandling.dto.SystemErrorDto;
import com.epam.digital.data.platform.starter.errorhandling.dto.ValidationErrorDto;
import com.epam.digital.data.platform.storage.form.dto.FormDataDto;
import com.epam.digital.data.platform.usrtaskmgt.BaseIT;
import com.epam.digital.data.platform.usrtaskmgt.model.StubRequest;
import com.epam.digital.data.platform.usrtaskmgt.model.response.CompletedTaskResponse;
import com.epam.digital.data.platform.usrtaskmgt.model.response.CompletedTaskResponse.VariableValueResponse;
import com.epam.digital.data.platform.usrtaskmgt.model.response.CountResponse;
import com.epam.digital.data.platform.usrtaskmgt.model.response.SignableDataUserTaskResponse;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

class UserTaskControllerIT extends BaseIT {

  private static final String TASK_ID = "testTaskId";

  @Test
  void shouldCountTasks() {
    final var testTaskCount = 11L;

    mockBpmsGetTaskCount(200, String.format("{\"count\":%d}", testTaskCount));

    var request = get("/api/task/count")
        .accept(MediaType.APPLICATION_JSON_VALUE);
    var result = performForObjectAsOfficer(request, CountResponse.class);

    assertThat(result).isNotNull()
        .extracting(CountResponse::getCount).isEqualTo(testTaskCount);
  }

  @Test
  void shouldCountTasks_badRequest() throws Exception {
    mockBpmsGetTaskCount(400, "{\"message\":\"Bad request\"}");

    var request = get("/api/task/count")
        .accept(MediaType.APPLICATION_JSON_VALUE);
    performWithTokenOfficerRole(request).andExpect(status().isBadRequest());
  }

  @Test
  void shouldCountTasks_unauthorized() throws Exception {
    mockBpmsGetTaskCount(401, "{\"message\":\"Unauthorized\"}");

    var request = get("/api/task/count")
        .accept(MediaType.APPLICATION_JSON_VALUE);
    performWithTokenOfficerRole(request).andExpect(status().isUnauthorized());
  }

  @Test
  void shouldGetTasks() {
    mockBpmsRequest(StubRequest.builder()
        .path("/api/extended/task")
        .method(HttpMethod.POST)
        .status(200)
        .responseBody(fileContent("/json/getTasksResponse.json"))
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .build());

    var request = get("/api/task")
        .accept(MediaType.APPLICATION_JSON_VALUE);
    var userTaskDtos = Arrays.asList(performForObjectAsOfficer(request, DdmTaskDto[].class));

    assertThat(userTaskDtos).hasSize(2);
    assertThat(userTaskDtos.get(0))
        .hasFieldOrPropertyWithValue("id", "task1")
        .hasFieldOrPropertyWithValue("processDefinitionId", "pdId1")
        .hasFieldOrPropertyWithValue("processDefinitionName", "testName")
        .hasFieldOrPropertyWithValue("suspended", false)
        .hasFieldOrPropertyWithValue("businessKey", "businessKey");
    assertThat(userTaskDtos.get(1))
        .hasFieldOrPropertyWithValue("id", "task2")
        .hasFieldOrPropertyWithValue("processDefinitionId", "pdId2")
        .hasFieldOrPropertyWithValue("processDefinitionName", "testName")
        .hasFieldOrPropertyWithValue("suspended", true)
        .hasFieldOrPropertyWithValue("businessKey", null);
  }

  @Test
  void shouldGetLightweightTasks() {
    mockBpmsRequest(StubRequest.builder()
        .path("/api/extended/task/lightweight")
        .method(HttpMethod.POST)
        .status(200)
        .responseBody(fileContent("/json/getLightweightTasksResponse.json"))
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .build());

    var request = get("/api/task/lightweight")
        .accept(MediaType.APPLICATION_JSON_VALUE);
    var userTaskDtos = Arrays.asList(performForObjectAsOfficer(request, DdmTaskDto[].class));

    assertThat(userTaskDtos).hasSize(2);
    assertThat(userTaskDtos.get(0))
        .hasFieldOrPropertyWithValue("id", "task1")
        .hasFieldOrPropertyWithValue("assignee", "testuser");
    assertThat(userTaskDtos.get(1))
        .hasFieldOrPropertyWithValue("id", "task2")
        .hasFieldOrPropertyWithValue("assignee", "testuser");
  }

  @Test
  void shouldGetTaskById_noStorageConnection() {
    mockGetExtendedTask(fileContent("/json/getSignableTaskWithFormVariablesResponse.json"));

    var request = get("/api/task/" + TASK_ID)
        .accept(MediaType.APPLICATION_JSON_VALUE);
    var taskById = performForObjectAsOfficer(request, SignableDataUserTaskResponse.class);

    assertThat(taskById).isNotNull()
        .hasFieldOrPropertyWithValue("id", TASK_ID)
        .hasFieldOrPropertyWithValue("data", new LinkedHashMap<>())
        .hasFieldOrPropertyWithValue("eSign", true)
        .hasFieldOrPropertyWithValue("processDefinitionId", "pdId1")
        .hasFieldOrPropertyWithValue("processDefinitionName", "testPDName")
        .hasFieldOrPropertyWithValue("processInstanceId", "processInstanceId")
        .hasFieldOrPropertyWithValue("rootProcessInstanceId", "rootProcessInstanceId")
        .hasFieldOrPropertyWithValue("taskDefinitionKey", "taskDefinitionKey")
        .hasFieldOrPropertyWithValue("created",
            LocalDateTime.of(2021, 2, 10, 13, 55, 10, 123000000))
        .hasFieldOrPropertyWithValue("formKey", "testFormKey")
        .hasFieldOrPropertyWithValue("assignee", "testuser")
        .hasFieldOrPropertyWithValue("signatureValidationPack", Set.of())
        .hasFieldOrPropertyWithValue("formVariables", Map.of("fullName", "Test Full Name"));
  }

  @Test
  void shouldGetTaskById_validForm() {
    mockGetExtendedTask(fileContent("/json/getSignableTaskWithFormVariablesResponse.json"));

    var taskDefinitionKey = "taskDefinitionKey";
    var processInstanceId = "processInstanceId";
    var storageKey = formDataKeyProvider.generateKey(taskDefinitionKey, processInstanceId);
    LinkedHashMap<String, Object> data = new LinkedHashMap<>();
    data.put("filed1", "fieldValue1");
    var formData = FormDataDto.builder().data(data).build();
    formDataStorageService.putFormData(storageKey, formData);

    var request = get("/api/task/" + TASK_ID)
        .accept(MediaType.APPLICATION_JSON_VALUE);
    var taskById = performForObjectAsOfficer(request, SignableDataUserTaskResponse.class);

    assertThat(taskById).isNotNull()
        .hasFieldOrPropertyWithValue("id", TASK_ID)
        .hasFieldOrPropertyWithValue("data", data)
        .hasFieldOrPropertyWithValue("eSign", true)
        .hasFieldOrPropertyWithValue("processDefinitionId", "pdId1")
        .hasFieldOrPropertyWithValue("processDefinitionName", "testPDName")
        .hasFieldOrPropertyWithValue("processInstanceId", "processInstanceId")
        .hasFieldOrPropertyWithValue("taskDefinitionKey", "taskDefinitionKey")
        .hasFieldOrPropertyWithValue("created",
            LocalDateTime.of(2021, 2, 10, 13, 55, 10, 123000000))
        .hasFieldOrPropertyWithValue("formKey", "testFormKey")
        .hasFieldOrPropertyWithValue("assignee", "testuser")
        .hasFieldOrPropertyWithValue("signatureValidationPack", Set.of())
        .hasFieldOrPropertyWithValue("formVariables", Map.of("fullName", "Test Full Name"));

      formDataStorageService.deleteByProcessInstanceId(processInstanceId);
  }

  @Test
  void shouldReturn404WhenTaskNotFound() {
    var request = get("/api/task/random")
        .accept(MediaType.APPLICATION_JSON_VALUE);

    var result = performForObjectAsOfficerWithStatus(request, SystemErrorDto.class,
        status().isNotFound());

    assertThat(result.getLocalizedMessage()).isEqualTo("Задачі з id random не існує");
  }

  @Test
  void shouldReturn403WhenUserRoleIsEmpty() throws Exception {
    var request = get("/api/task/testIdForTokenWithoutRole")
        .accept(MediaType.APPLICATION_JSON_VALUE);

    performWithTokenWithoutRole(request).andExpect(status().is4xxClientError());
  }

  @Test
  void shouldNotCompleteTaskByIdWhenNoCephConnection() throws Exception {
    mockGetExtendedTask(fileContent("/json/getSignableTaskWithFormVariablesResponse.json"));

    var request = post("/api/task/" + TASK_ID + "/complete")
        .accept(MediaType.APPLICATION_JSON_VALUE).contentType("application/json").content("{}");

    performWithTokenOfficerRole(request).andExpect(status().is5xxServerError());
  }

  @Test
  void shouldCompleteTaskById() throws Exception {
    mockGetExtendedTask(fileContent("/json/getSignableTaskWithFormVariablesResponse.json"));

    var processInstanceId = "processInstanceId";
    var token = tokenConfig.getValueWithRoleOfficer();
    var payload = String.format("{\"data\":{},\"x-access-token\":\"%s\"}", token);

    mockGetForm();
    mockValidationValidFormData("{}", processInstanceId);

    mockCompleteTask(200, "{\"id\":\"" + TASK_ID + "\","
        + "\"processInstanceId\":\"" + processInstanceId + "\","
        + "\"rootProcessInstanceId\":\"" + processInstanceId + "\","
        + "\"rootProcessInstanceEnded\":false,"
        + "\"variables\":{\"var1\":{\"value\":\"variableValue\"}}}");

    var request = post("/api/task/" + TASK_ID + "/complete")
        .accept(MediaType.APPLICATION_JSON_VALUE).contentType("application/json")
        .content(payload);

    var response = performWithTokenOfficerRole(request).andExpect(status().is2xxSuccessful())
        .andReturn().getResponse().getContentAsString();

    var expected = CompletedTaskResponse.builder()
        .id(TASK_ID)
        .processInstanceId(processInstanceId)
        .rootProcessInstanceId(processInstanceId)
        .rootProcessInstanceEnded(false)
        .variables(Map.of("var1", VariableValueResponse.builder().value("variableValue").build()))
        .build();
    var actual = objectMapper.readValue(response, CompletedTaskResponse.class);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  void shouldSignOfficerForm() throws Exception {
    mockGetExtendedTask(fileContent("/json/getSignableTaskWithFormVariablesResponse.json"));

    var processInstanceId = "processInstanceId";
    var token = tokenConfig.getValueWithRoleOfficer();
    var payload = String.format("{\"data\":{},\"x-access-token\":\"%s\"}", token);

    mockGetForm();
    mockValidationValidFormData("{}", processInstanceId);

    mockOfficerDigitalSignature(200, "{\"valid\":true}");

    mockCompleteTask(200, "{\"id\":\"" + TASK_ID + "\","
        + "\"processInstanceId\":\"" + processInstanceId + "\","
        + "\"rootProcessInstanceId\":\"" + processInstanceId + "\","
        + "\"rootProcessInstanceEnded\":true,"
        + "\"variables\":{\"var1\":{\"value\":\"variableValue\"}}}");

    var request = post("/api/officer/task/" + TASK_ID + "/sign-form")
        .accept(MediaType.APPLICATION_JSON_VALUE).contentType("application/json")
        .content(payload);

    var response = performWithTokenOfficerRole(request).andExpect(status().is2xxSuccessful())
        .andReturn().getResponse().getContentAsString();

    var expected = CompletedTaskResponse.builder()
        .id(TASK_ID)
        .processInstanceId(processInstanceId)
        .rootProcessInstanceId(processInstanceId)
        .rootProcessInstanceEnded(true)
        .variables(Map.of("var1", VariableValueResponse.builder().value("variableValue").build()))
        .build();
    var actual = objectMapper.readValue(response, CompletedTaskResponse.class);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  void shouldFailOnSignOfficerForm() {
    mockGetExtendedTask(fileContent("/json/getSignableTaskWithFormVariablesResponse.json"));

    var token = tokenConfig.getValueWithRoleOfficer();
    var payload = String.format("{\"data\":{},\"x-access-token\":\"%s\"}", token);

    mockGetForm();
    mockValidationValidFormData("{}", null);

    mockOfficerDigitalSignature(200,
        "{\"valid\":false,\"error\":{\"localizedMessage\":\"message\"}}");

    var request = post("/api/officer/task/" + TASK_ID + "/sign-form")
        .accept(MediaType.APPLICATION_JSON_VALUE).contentType("application/json")
        .content(payload);

    var result = performForObjectAsOfficerWithStatus(request, ValidationErrorDto.class,
        status().isUnprocessableEntity());
    assertThat(result.getDetails().getErrors().get(0).getMessage()).isEqualTo("message");
  }

  @Test
  void shouldSignCitizenForm() throws Exception {
    mockGetExtendedTask(fileContent("/json/getSignableTaskWithValidationPack.json"));

    var processInstanceId = "processInstanceId";
    var token = tokenConfig.getValueWithRoleCitizen();
    var payload = String.format("{\"data\":{},\"x-access-token\":\"%s\"}", token);

    mockGetForm();
    mockValidationValidFormData("{}", processInstanceId);

    var requestBody = matchingJsonPath("$.allowedSubjects",
        equalToJson("[\"ENTREPRENEUR\"]"));
    mockCitizenDigitalSignature(requestBody, 200, "{\"valid\":true}");

    mockCompleteTask(200, "{\"id\":\"" + TASK_ID + "\","
        + "\"processInstanceId\":\"" + processInstanceId + "\","
        + "\"rootProcessInstanceId\":\"" + processInstanceId + "\","
        + "\"rootProcessInstanceEnded\":false,"
        + "\"variables\":{\"var1\":{\"value\":\"variableValue\"}}}");

    var request = post("/api/citizen/task/" + TASK_ID + "/sign-form")
        .accept(MediaType.APPLICATION_JSON_VALUE).contentType("application/json")
        .content(payload);

    var response = performWithTokenCitizenRole(request).andExpect(status().is2xxSuccessful())
        .andReturn().getResponse().getContentAsString();

    var expected = CompletedTaskResponse.builder()
        .id(TASK_ID)
        .processInstanceId(processInstanceId)
        .rootProcessInstanceId(processInstanceId)
        .rootProcessInstanceEnded(false)
        .variables(Map.of("var1", VariableValueResponse.builder().value("variableValue").build()))
        .build();
    var actual = objectMapper.readValue(response, CompletedTaskResponse.class);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  void shouldSignCitizenFormAsIndividualIfNoValidations() throws Exception {
    mockGetExtendedTask(fileContent("/json/getSignableTaskWithFormVariablesResponse.json"));

    var processInstanceId = "processInstanceId";
    var token = tokenConfig.getValueWithRoleCitizen();
    var payload = String.format("{\"data\":{},\"x-access-token\":\"%s\"}", token);

    mockGetForm();
    mockValidationValidFormData("{}", processInstanceId);

    var requestBody = matchingJsonPath("$.allowedSubjects",
        equalToJson("[\"INDIVIDUAL\"]"));
    mockCitizenDigitalSignature(requestBody, 200, "{\"valid\":true}");

    mockCompleteTask(200, "{\"id\":\"" + TASK_ID + "\","
        + "\"processInstanceId\":\"" + processInstanceId + "\","
        + "\"rootProcessInstanceId\":\"" + processInstanceId + "\","
        + "\"rootProcessInstanceEnded\":false,"
        + "\"variables\":{\"var1\":{\"value\":\"variableValue\"}}}");

    var request = post("/api/citizen/task/" + TASK_ID + "/sign-form")
        .accept(MediaType.APPLICATION_JSON_VALUE).contentType("application/json")
        .content(payload);

    var response = performWithTokenCitizenRole(request).andExpect(status().is2xxSuccessful())
        .andReturn().getResponse().getContentAsString();

    var expected = CompletedTaskResponse.builder()
        .id(TASK_ID)
        .processInstanceId(processInstanceId)
        .rootProcessInstanceId(processInstanceId)
        .rootProcessInstanceEnded(false)
        .variables(Map.of("var1", VariableValueResponse.builder().value("variableValue").build()))
        .build();
    var actual = objectMapper.readValue(response, CompletedTaskResponse.class);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  void shouldFailOnSignCitizenForm() {
    mockGetExtendedTask(fileContent("/json/getSignableTaskWithFormVariablesResponse.json"));

    var token = tokenConfig.getValueWithRoleCitizen();
    var payload = String.format("{\"data\":{},\"x-access-token\":\"%s\"}", token);

    mockGetForm();
    mockValidationValidFormData("{}", null);

    var requestBody = matchingJsonPath("$.allowedSubjects",
        equalToJson("[\"INDIVIDUAL\"]"));
    mockCitizenDigitalSignature(requestBody, 200,
        "{\"valid\":false,\"error\":{\"localizedMessage\":\"message\"}}");

    var request = post("/api/citizen/task/" + TASK_ID + "/sign-form")
        .accept(MediaType.APPLICATION_JSON_VALUE).contentType("application/json")
        .content(payload);

    var result = performForObjectAsCitizenWithStatus(request, ValidationErrorDto.class,
        status().isUnprocessableEntity());
    assertThat(result.getDetails().getErrors().get(0).getMessage()).isEqualTo("message");
  }

  @Test
  void shouldGetTasksByProcessInstanceId() {
    var testProcessInstanceId = "testProcessInstanceId";
    mockBpmsRequest(StubRequest.builder()
        .method(HttpMethod.POST)
        .path("/api/extended/task")
        .requestBody(equalToJson(fileContent("/json/getTasksByProcessInstanceIdRequest.json")))
        .status(200)
        .responseBody("[{\"processInstanceId\":\"testProcessInstanceId\"}]")
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .build());

    var request = get("/api/task")
        .param("processInstanceId", testProcessInstanceId)
        .accept(MediaType.APPLICATION_JSON_VALUE);

    var userTaskDtos = Arrays.asList(performForObjectAsOfficer(request, DdmTaskDto[].class));

    assertThat(userTaskDtos.size()).isOne();
    assertThat(userTaskDtos.get(0).getProcessInstanceId()).isEqualTo(testProcessInstanceId);
  }

  @Test
  void shouldReturn422DuringTaskCompletion() {
    mockGetExtendedTask(fileContent("/json/getSignableTaskWithFormVariablesResponse.json"));
    mockGetForm();
    mockValidationValidFormData("{}", "processInstanceId");

    mockCompleteTask(422,
        "{\"details\":{\"errors\":[{\"message\":\"myMsg\",\"field\":\"variable\",\"value\":\"value\"}]}}");

    var request = post("/api/task/" + TASK_ID + "/complete")
        .accept(MediaType.APPLICATION_JSON_VALUE).contentType("application/json")
        .content("{\"data\" : {}}");

    var result = performForObjectAsOfficerWithStatus(request, ValidationErrorDto.class,
        status().is(422));

    assertThat(result).isNotNull();
    var validationErrorDto = result.getDetails().getErrors().get(0);
    assertThat(validationErrorDto.getMessage()).isEqualTo("myMsg");
    assertThat(validationErrorDto.getField()).isEqualTo("variable");
    assertThat(validationErrorDto.getValue()).isEqualTo("value");
  }

  @Test
  void shouldReturn422OnInvalidFormValidation() {
    mockGetExtendedTask(fileContent("/json/getSignableTaskWithFormVariablesResponse.json"));
    mockGetForm();
    mockValidationFormData(
        "{}",
        "processInstanceId",
        "{\"details\":{\"errors\":[{\"message\":\"myMsg\",\"field\":\"variable\",\"value\":\"value\"}]}}",
        422);

    var request =
        post("/api/task/" + TASK_ID + "/complete")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType("application/json")
            .content("{\"data\" : {}}");

    var result =
        performForObjectAsOfficerWithStatus(request, ValidationErrorDto.class, status().is(422));

    assertThat(result).isNotNull();
    var validationErrorDto = result.getDetails().getErrors().get(0);
    assertThat(validationErrorDto.getMessage()).isEqualTo("myMsg");
    assertThat(validationErrorDto.getField()).isEqualTo("variable");
    assertThat(validationErrorDto.getValue()).isEqualTo("value");
  }

  @Test
  void shouldHandleUserTaskAuthorizationException() {
    mockGetExtendedTask(String.format("{\"id\":\"%s\",\"assignee\":\"testuser2\"}", TASK_ID));

    var request = get("/api/task/" + TASK_ID)
        .accept(MediaType.APPLICATION_JSON_VALUE);

    var response = performForObjectAsOfficerWithStatus(request, SystemErrorDto.class,
        status().isForbidden());

    assertThat(response).isNotNull();
    assertThat(response.getMessage()).isEqualTo(
        "The user with username testuser does not have permission on resource Task with id testTaskId");
    assertThat(response.getLocalizedMessage()).isEqualTo("Немає доступу до задачі з id testTaskId");
  }

  @Test
  void shouldSuccessfullyClaimTask() throws Exception {
    mockGetTask(200, String.format("{\"id\":\"%s\",\"assignee\":null}", TASK_ID));

    mockBpmsRequest(StubRequest.builder()
        .path(String.format("/api/task/%s/claim", TASK_ID))
        .method(HttpMethod.POST)
        .status(204)
        .build());

    performWithTokenOfficerRole(post(String.format("/api/task/%s/claim", TASK_ID)))
        .andExpect(status().isNoContent());
  }

  @Test
  void shouldSuccessfullyClaimTaskIfAlreadyAssignedToUser() throws Exception {
    mockGetTask(200, String.format("{\"id\":\"%s\",\"assignee\":\"testuser\"}", TASK_ID));

    mockBpmsRequest(StubRequest.builder()
        .path(String.format("/api/task/%s/claim", TASK_ID))
        .method(HttpMethod.POST)
        .status(204)
        .build());

    performWithTokenOfficerRole(post(String.format("/api/task/%s/claim", TASK_ID)))
        .andExpect(status().isNoContent());
  }

  @Test
  void shouldHandleUserTaskAlreadyAssignedIfTaskAssignedToOtherUser() {
    mockGetTask(200,
        String.format("{\"id\":\"%s\",\"assignee\":\"testuser2\",\"name\":\"User task\"}",
            TASK_ID));

    var response = performForObjectAsOfficerWithStatus(
        post(String.format("/api/task/%s/claim", TASK_ID)), SystemErrorDto.class,
        status().isConflict());

    assertThat(response.getMessage()).isEqualTo("Task already assigned");
    assertThat(response.getLocalizedMessage())
        .isEqualTo("Задача User task була прийнята до виконання іншим співробітником.");
  }

  @Test
  void shouldHandleUserTaskNotExistsOrCompletedIfTaskNotFound() {
    mockGetTask(404, "{\"message\":\"not found\"}");

    var response = performForObjectAsOfficerWithStatus(
        post(String.format("/api/task/%s/claim", TASK_ID)), SystemErrorDto.class,
        status().isNotFound());

    assertThat(response.getMessage()).isEqualTo("not found");
    assertThat(response.getLocalizedMessage()).isEqualTo("Задача не існує або вже виконана");
  }

  @Test
  void shouldReturnBadRequestWithBrokenInputJson() throws Exception {
    var request = post("/api/task/{taskId}/complete",
        "taskId", TASK_ID)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"data\" : { \"}}");

    performWithTokenOfficerRole(request).andExpect(status().isBadRequest());
  }

  @Test
  void shouldSaveFormData() throws Exception {
    mockGetExtendedTask(fileContent("/json/getSignableTaskWithFormVariablesResponse.json"));

    var processInstanceId = "processInstanceId";
    var token = tokenConfig.getValueWithRoleOfficer();
    var payload = String.format("{\"data\":{\"data\":\"value\"},\"x-access-token\":\"%s\"}", token);

    var expected = objectMapper.readValue(payload, FormDataDto.class);

    mockGetForm();
    mockValidationValidFormData("{\"data\":\"value\"}", processInstanceId);

    var request = post("/api/task/" + TASK_ID + "/save")
        .accept(MediaType.APPLICATION_JSON_VALUE).contentType("application/json")
        .content(payload);

    performWithTokenOfficerRole(request).andExpect(status().is2xxSuccessful())
        .andReturn().getResponse().getContentAsString();

    var actual = formDataStorageService.getFormData("taskDefinitionKey",
        processInstanceId).get();
    assertThat(actual).isEqualTo(expected);
  }

  private void mockGetExtendedTask(String response) {
    mockBpmsRequest(StubRequest.builder()
        .path(String.format("/api/extended/task/%s", TASK_ID))
        .method(HttpMethod.GET)
        .status(200)
        .responseBody(response)
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .build());
  }

  private void mockCompleteTask(int status, String body) {
    mockBpmsRequest(StubRequest.builder()
        .path(String.format("/api/extended/task/%s/complete", TASK_ID))
        .method(HttpMethod.POST)
        .status(status)
        .responseBody(body)
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .build());
  }

  private void mockGetTask(int status, String body) {
    mockBpmsRequest(StubRequest.builder()
        .path(String.format("/api/extended/task/%s", TASK_ID))
        .method(HttpMethod.GET)
        .status(status)
        .responseBody(body)
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .build());
  }

  private void mockBpmsGetTaskCount(int status, String body) {
    mockBpmsRequest(StubRequest.builder()
        .path("/api/task/count")
        .method(HttpMethod.POST)
        .status(status)
        .responseBody(body)
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .build());
  }
}