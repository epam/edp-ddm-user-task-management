package com.epam.digital.data.platform.usrtaskmgt.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.digital.data.platform.dso.api.dto.ErrorDto;
import com.epam.digital.data.platform.dso.api.dto.Subject;
import com.epam.digital.data.platform.dso.api.dto.VerificationResponseDto;
import com.epam.digital.data.platform.dso.api.dto.VerifySubjectRequestDto;
import com.epam.digital.data.platform.dso.api.dto.VerifySubjectResponseDto;
import com.epam.digital.data.platform.starter.errorhandling.dto.ErrorDetailDto;
import com.epam.digital.data.platform.starter.errorhandling.dto.ErrorsListDto;
import com.epam.digital.data.platform.starter.errorhandling.dto.SystemErrorDto;
import com.epam.digital.data.platform.starter.errorhandling.dto.ValidationErrorDto;
import com.epam.digital.data.platform.usrtaskmgt.BaseIT;
import com.epam.digital.data.platform.usrtaskmgt.dto.SignableUserTaskDto;
import com.epam.digital.data.platform.usrtaskmgt.dto.UserTaskDto;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.assertj.core.util.Lists;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

public class UserTaskControllerIT extends BaseIT {

  @Test
  public void shouldCountTasks() {
    MockHttpServletRequestBuilder request = get("/api/task/count")
        .accept(MediaType.APPLICATION_JSON_VALUE);
    CountResultDto count = performForObject(request, CountResultDto.class);

    assertThat(count).isNotNull();
    assertThat(count.getCount()).isEqualTo(testTaskCount);
  }

  @Test
  public void shouldCountTasks_badRequest() throws Exception {
    mockTaskCountFail(400, "{\"message\":\"Bad request\"}");

    MockHttpServletRequestBuilder request = get("/api/task/count")
        .accept(MediaType.APPLICATION_JSON_VALUE);
    performWithTokenOfficerRole(request).andExpect(status().isBadRequest());
  }

  @Test
  public void shouldCountTasks_unauthorized() throws Exception {
    mockTaskCountFail(401, "{\"message\":\"Unauthorized\"}");

    MockHttpServletRequestBuilder request = get("/api/task/count")
        .accept(MediaType.APPLICATION_JSON_VALUE);
    performWithTokenOfficerRole(request).andExpect(status().isUnauthorized());
  }

  @Test
  public void shouldGetTasks() {
    MockHttpServletRequestBuilder request = get("/api/task")
        .accept(MediaType.APPLICATION_JSON_VALUE);
    List<UserTaskDto> userTaskDtos = Arrays
        .asList(performForObject(request, UserTaskDto[].class));

    assertThat(userTaskDtos).hasSize(2);
    userTaskDtos.forEach(t -> {
      assertThat(t.getId()).isNotEmpty();
      assertThat(t.getProcessDefinitionName()).isNotEmpty();
    });
    assertThat(userTaskDtos.get(0).isSuspended()).isFalse();
    assertThat(userTaskDtos.get(1).isSuspended()).isTrue();
  }

  @Test
  public void shouldGetTaskById_noSecureVarRefTaskFormData() {
    mockTaskByIdAndProcessDefinitionId(testTaskId, processDefinitionId1);
    mockGetTaskVariables(testTaskId);

    MockHttpServletRequestBuilder request = get("/api/task/" + testTaskId)
        .accept(MediaType.APPLICATION_JSON_VALUE);
    var taskById = performForObject(request, SignableUserTaskDto.class);

    assertThat(taskById).isNotNull();
    assertThat(taskById.getId()).isEqualTo(testTaskId);
    assertThat(taskById.getData()).isNull();
    assertThat(taskById.isESign()).isTrue();
    assertThat(taskById.getFormVariables().get("fullName")).isEqualTo("Test Full Name");
  }

  @Test
  public void shouldGetTaskById_noCephConnection() {
    var taskDefinitionKey = "taskDefinitionKey";
    var processInstanceId = "processInstanceId";

    mockTaskByParams(testTaskId, processDefinitionId1, processInstanceId, taskDefinitionKey);
    mockGetTaskVariables(testTaskId);

    MockHttpServletRequestBuilder request = get("/api/task/" + testTaskId)
        .accept(MediaType.APPLICATION_JSON_VALUE);
    var taskById = performForObject(request, SignableUserTaskDto.class);

    assertThat(taskById).isNotNull();
    assertThat(taskById.getId()).isEqualTo(testTaskId);
    assertThat(taskById.getData()).isNull();
    assertThat(taskById.isESign()).isTrue();
    assertThat(taskById.getFormVariables().get("fullName")).isEqualTo("Test Full Name");
  }

  @Test
  public void shouldGetTaskById_validForm() {
    var taskDefinitionKey = "taskDefinitionKey";
    var processInstanceId = "processInstanceId";

    var processInstanceVariableName = String
        .format("secure-sys-var-ref-task-form-data-%s", taskDefinitionKey);
    var cephKey = String.format("lowcode-%s-%s", processInstanceId, processInstanceVariableName);

    mockTaskByParams(testTaskId, processDefinitionId1, processInstanceId, taskDefinitionKey);
    mockGetCephContent(cephKey, "{\"data\" : {\"field1\": \"fieldValue1\"}}");
    mockGetTaskVariables(testTaskId);

    MockHttpServletRequestBuilder request = get("/api/task/" + testTaskId)
        .accept(MediaType.APPLICATION_JSON_VALUE);
    var taskById = performForObject(request, SignableUserTaskDto.class);

    assertThat(taskById).isNotNull();
    assertThat(taskById.getId()).isEqualTo(testTaskId);
    assertThat(taskById.getData()).isNotNull();
    assertThat(taskById.getData()).hasSize(1);
    assertThat(taskById.getData()).containsEntry("field1", "fieldValue1");
    assertThat(taskById.getFormVariables().get("fullName")).isEqualTo("Test Full Name");
  }

  @Test
  public void shouldReturn404WhenTaskNotFound() throws Exception {
    MockHttpServletRequestBuilder request = get("/api/task/random")
        .accept(MediaType.APPLICATION_JSON_VALUE);

    var result = performWithTokenOfficerRole(request).andExpect(status().isNotFound()).andReturn();

    var resultBody = objectMapper
        .readValue(result.getResponse().getContentAsString(StandardCharsets.UTF_8),
            SystemErrorDto.class);

    assertThat(resultBody.getLocalizedMessage()).isEqualTo("Задачі з id random не існує");
  }

  @Test
  public void shouldReturn403WhenUserRoleIsEmpty() throws Exception {
    MockHttpServletRequestBuilder request = get("/api/task/testIdForTokenWithoutRole")
        .accept(MediaType.APPLICATION_JSON_VALUE);

    performWithTokenWithoutRole(request).andExpect(status().is4xxClientError());
  }

  @Test
  public void shouldCompleteTaskById_noCephConnection() throws Exception {
    mockTaskByIdAndProcessDefinitionId(testTaskId, processDefinitionId1);

    MockHttpServletRequestBuilder request = post("/api/task/" + testTaskId + "/complete")
        .accept(MediaType.APPLICATION_JSON_VALUE).contentType("application/json").content("{}");

    performWithTokenOfficerRole(request).andExpect(status().is5xxServerError());
  }

  @Test
  public void shouldCompleteTaskById() throws Exception {
    var processInstanceId = "processInstance";
    var taskDefinitionKey = "taskDefinition";
    var cephKey = cephKeyProvider.generateKey(taskDefinitionKey,processInstanceId);
    var payload = "{\"data\":{},\"x-access-token\":\"" + tokenConfig.getValueWithRoleOfficer() + "\"}";

    mockTaskByParams(testTaskId, processDefinitionId1, processInstanceId, taskDefinitionKey);
    mockPutCephContent(cephKey, payload);
    mockGetForm();

    MockHttpServletRequestBuilder request = post("/api/task/" + testTaskId + "/complete")
        .accept(MediaType.APPLICATION_JSON_VALUE).contentType("application/json")
        .content(payload);

    performWithTokenOfficerRole(request).andExpect(status().is2xxSuccessful());
  }

  @Test
  public void shouldSignOfficerForm() throws Exception {
    var processInstanceId = "processInstance";
    var taskDefinitionKey = "taskDefinition";
    var cephKey = cephKeyProvider.generateKey(taskDefinitionKey,processInstanceId);
    var payload =
        "{\"data\":{},\"signature\":\"eSign\",\"x-access-token\":\"" + tokenConfig.getValueWithRoleOfficer() + "\"}";

    mockTaskByParams(testTaskId, processDefinitionId1, processInstanceId, taskDefinitionKey);
    mockPutCephContent(cephKey, payload);
    mockValidationFormData(payload);
    mockGetForm();

    var verifyResponseDto = new VerificationResponseDto();
    verifyResponseDto.setValid(true);
    mockOfficerDigitalSignature(200, verifyResponseDto);

    MockHttpServletRequestBuilder request = post("/api/officer/task/" + testTaskId + "/sign-form")
        .accept(MediaType.APPLICATION_JSON_VALUE).contentType("application/json")
        .content(payload);

    performWithTokenOfficerRole(request).andExpect(status().is2xxSuccessful());
  }

  @Test
  public void shouldFailOnSignOfficerForm() throws Exception {
    var processInstanceId = "processInstance";
    var taskDefinitionKey = "taskDefinition";
    var cephKey = cephKeyProvider.generateKey(taskDefinitionKey,processInstanceId);
    var payload = "{\"data\":{},\"signature\":\"eSign\"}";

    mockTaskByParams(testTaskId, processDefinitionId1, processInstanceId, taskDefinitionKey);
    mockPutCephContent(cephKey, payload);
    mockGetForm();

    var verifyResponseDto = new VerificationResponseDto();
    verifyResponseDto.setValid(false);
    verifyResponseDto.setError(ErrorDto.builder().localizedMessage("message").build());

    mockOfficerDigitalSignature(200, verifyResponseDto);

    var request = post("/api/officer/task/" + testTaskId + "/sign-form")
        .accept(MediaType.APPLICATION_JSON_VALUE).contentType("application/json")
        .content(payload);

    var result = performWithTokenOfficerRole(request).andExpect(status().isUnprocessableEntity()).andReturn();
    var response = result.getResponse().getContentAsString();
    var responseTree = objectMapper.readTree(response);
    assertThat(
        responseTree.get("details").get("errors").get(0).get("message")
            .asText()).isEqualTo("message");
  }

  @Test
  public void shouldSignCitizenForm() throws Exception {
    var processInstanceId = "processInstance";
    var taskDefinitionKey = "taskDefinition";
    var cephKey = cephKeyProvider.generateKey(taskDefinitionKey,processInstanceId);
    var payload =
        "{\"data\":{},\"signature\":\"eSign\",\"x-access-token\":\"" + tokenConfig.getValueWithRoleCitizen() + "\"}";

    mockTaskByParams(testTaskId, processDefinitionId1, processInstanceId, taskDefinitionKey);
    mockPutCephContent(cephKey, payload);
    mockValidationFormData(payload);
    mockGetForm();

    var verifyResponseDto = new VerifySubjectResponseDto();
    verifyResponseDto.setValid(true);
    mockCitizenDigitalSignature(200, verifyResponseDto);

    MockHttpServletRequestBuilder request = post("/api/citizen/task/" + testTaskId + "/sign-form")
        .accept(MediaType.APPLICATION_JSON_VALUE).contentType("application/json")
        .content(payload);

    performWithTokenCitizenRole(request).andExpect(status().is2xxSuccessful());
  }

  @Test
  public void shouldSignCitizenFormAsCitizenIfNoValidations() throws Exception {
    var processInstanceId = "processInstance";
    var taskDefinitionKey = "taskDefinition";
    var cephKey = cephKeyProvider.generateKey(taskDefinitionKey, processInstanceId);
    var payload =
        "{\"data\":{},\"signature\":\"eSign\",\"x-access-token\":\"" + tokenConfig
            .getValueWithRoleCitizen() + "\"}";

    mockTaskByParams(testTaskId, processDefinitionId1, processInstanceId, taskDefinitionKey);
    mockPutCephContent(cephKey, payload);
    mockValidationFormData(payload);
    mockGetForm();

    var properties = new HashMap<>();
    properties.put("eSign", "true");
    bpmServer.addStubMapping(
        stubFor(WireMock
            .get(urlPathEqualTo("/api/extended/task/" + testTaskId + "/extension-element/property"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(objectMapper.writeValueAsString(properties)))
        )
    );

    var verifyResponseDto = new VerifySubjectResponseDto();
    verifyResponseDto.setValid(true);
    dsoServer.addStubMapping(
        stubFor(WireMock.post(urlPathEqualTo("/api/esignature/citizen/verify"))
            .withRequestBody(equalTo(objectMapper.writeValueAsString(
                new VerifySubjectRequestDto(Collections.singletonList(Subject.INDIVIDUAL), "eSign",
                    "{}"))))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(objectMapper.writeValueAsString(verifyResponseDto)))
        )
    );

    MockHttpServletRequestBuilder request = post("/api/citizen/task/" + testTaskId + "/sign-form")
        .accept(MediaType.APPLICATION_JSON_VALUE).contentType("application/json")
        .content(payload);

    performWithTokenCitizenRole(request).andExpect(status().is2xxSuccessful());
  }

  @Test
  public void shouldFailOnSignCitizenForm() throws Exception {
    var processInstanceId = "processInstance";
    var taskDefinitionKey = "taskDefinition";
    var cephKey = cephKeyProvider.generateKey(taskDefinitionKey, processInstanceId);
    var payload = "{\"data\":{},\"signature\":\"eSign\"}";

    mockTaskByParams(testTaskId, processDefinitionId1, processInstanceId, taskDefinitionKey);
    mockPutCephContent(cephKey, payload);
    mockGetForm();

    var verifyResponseDto = new VerifySubjectResponseDto();
    verifyResponseDto.setValid(false);
    verifyResponseDto.setError(ErrorDto.builder().localizedMessage("message").build());

    mockCitizenDigitalSignature(200, verifyResponseDto);

    var request = post("/api/citizen/task/" + testTaskId + "/sign-form")
        .accept(MediaType.APPLICATION_JSON_VALUE).contentType("application/json")
        .content(payload);

    var result = performWithTokenCitizenRole(request).andExpect(status().isUnprocessableEntity()).andReturn();
    var response = result.getResponse().getContentAsString();
    var responseTree = objectMapper.readTree(response);
    assertThat(
        responseTree.get("details").get("errors").get(0).get("message")
            .asText()).isEqualTo("message");
  }

  @Test
  public void shouldGetTasksByProcessInstanceId() {
    MockHttpServletRequestBuilder request = get("/api/task")
        .param("processInstanceId", testProcessInstanceId)
        .accept(MediaType.APPLICATION_JSON_VALUE);

    List<UserTaskDto> userTaskDtos = Arrays
        .asList(performForObject(request, UserTaskDto[].class));

    assertThat(userTaskDtos.size()).isOne();
    assertThat(userTaskDtos.get(0).getProcessInstanceId()).isEqualTo(testProcessInstanceId);
  }

  @Test
  public void shouldReturn422DuringTaskCompletion() throws Exception {
    var processInstanceId = "processInstance";
    var taskDefinitionKey = "taskDefinition";
    var cephKey = cephKeyProvider.generateKey(taskDefinitionKey,processInstanceId);
    var payload = "{\"data\":{},\"x-access-token\":\"" + tokenConfig.getValueWithRoleOfficer() + "\"}";
    mockTaskByParams(testTaskId, processDefinitionId1, processInstanceId, taskDefinitionKey);
    mockPutCephContent(cephKey, payload);
    mockValidationFormData(payload);
    mockGetForm();

    var errorDto = new ValidationErrorDto();
    errorDto.setDetails(new ErrorsListDto(Lists.newArrayList(new ErrorDetailDto("myMsg",
        "variable", "value"))));
    bpmServer.addStubMapping(
        stubFor(WireMock.post(urlEqualTo("/api/task/" + testTaskId + "/complete"))
            .willReturn(aResponse()
                .withStatus(422)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(errorDto))))
    );

    MockHttpServletRequestBuilder request = post("/api/task/" + testTaskId + "/complete")
        .accept(MediaType.APPLICATION_JSON_VALUE).contentType("application/json")
        .content("{\"data\" : {}}");

    String contentAsString = performWithTokenOfficerRole(request)
        .andExpect(status().is(422))
        .andReturn()
        .getResponse()
        .getContentAsString();

    var response = objectMapper.readValue(contentAsString, ValidationErrorDto.class);

    assertThat(response).isNotNull();
    var validationErrorDto = response.getDetails().getErrors().get(0);
    assertThat(validationErrorDto.getMessage()).isEqualTo("myMsg");
    assertThat(validationErrorDto.getField()).isEqualTo("variable");
    assertThat(validationErrorDto.getValue()).isEqualTo("value");
  }

  @Test
  public void shouldHandleUserTaskAuthorizationException() throws Exception {
    TaskEntity task = new TaskEntity();
    task.setId(testTaskId);
    task.setAssignee("testuser2");
    TaskDto taskById = TaskDto.fromEntity(task);
    bpmServer.addStubMapping(
        stubFor(WireMock.get(urlPathEqualTo("/api/task/" + testTaskId))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(objectMapper.writeValueAsString(taskById)))
        )
    );

    MockHttpServletRequestBuilder request = get("/api/task/" + testTaskId)
        .accept(MediaType.APPLICATION_JSON_VALUE);

    String contentAsString = performWithTokenOfficerRole(request)
        .andExpect(status().is(403))
        .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    var response = objectMapper.readValue(contentAsString, SystemErrorDto.class);

    assertThat(response).isNotNull();
    assertThat(response.getMessage())
        .isEqualTo(
            "The user with username testuser does not have permission on resource Task with id testTaskId");
    assertThat(response.getLocalizedMessage()).isEqualTo("Немає доступу до задачі з id testTaskId");
  }

  @Test
  public void shouldSuccessfullyClaimTask() throws Exception {
    var task = new TaskEntity();
    task.setId(testTaskId);
    var taskById = TaskDto.fromEntity(task);
    bpmServer.addStubMapping(stubFor(WireMock.get(urlPathEqualTo("/api/task/" + testTaskId))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(200)
            .withBody(objectMapper.writeValueAsString(taskById)))));
    bpmServer.addStubMapping(
        stubFor(WireMock.post(urlPathEqualTo("/api/task/" + testTaskId + "/claim"))
            .willReturn(aResponse().withStatus(204))));

    performWithTokenOfficerRole(post("/api/task/" + testTaskId + "/claim"))
        .andExpect(status().isNoContent());
  }

  @Test
  public void shouldSuccessfullyClaimTaskIfAlreadyAssignedToUser() throws Exception {
    var task = new TaskEntity();
    task.setId(testTaskId);
    task.setAssignee("testuser");
    var taskById = TaskDto.fromEntity(task);
    bpmServer.addStubMapping(stubFor(WireMock.get(urlPathEqualTo("/api/task/" + testTaskId))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(200)
            .withBody(objectMapper.writeValueAsString(taskById)))));
    bpmServer.addStubMapping(
        stubFor(WireMock.post(urlPathEqualTo("/api/task/" + testTaskId + "/claim"))
            .willReturn(aResponse().withStatus(204))));

    performWithTokenOfficerRole(post("/api/task/" + testTaskId + "/claim"))
        .andExpect(status().isNoContent());
  }


  @Test
  public void shouldHandleUserTaskAlreadyAssignedIfTaskAssignedToOtherUser() throws Exception {
    var task = new TaskEntity();
    task.setId(testTaskId);
    task.setName("User task");
    task.setAssignee("testuser2");
    var taskById = TaskDto.fromEntity(task);
    bpmServer.addStubMapping(stubFor(WireMock.get(urlPathEqualTo("/api/task/" + testTaskId))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(200)
            .withBody(objectMapper.writeValueAsString(taskById)))));

    var contentAsString = performWithTokenOfficerRole(post("/api/task/" + testTaskId + "/claim"))
        .andExpect(status().isConflict()).andReturn().getResponse()
        .getContentAsString(StandardCharsets.UTF_8);

    var response = objectMapper.readValue(contentAsString, SystemErrorDto.class);

    assertThat(response.getMessage()).isEqualTo("Task already assigned");
    assertThat(response.getLocalizedMessage())
        .isEqualTo("Задача User task була прийнята до виконання іншим співробітником.");
  }

  @Test
  public void shouldHandleUserTaskNotExistsOrCompletedIfTaskNotFound() throws Exception {
    bpmServer.addStubMapping(stubFor(WireMock.get(urlPathEqualTo("/api/task/" + testTaskId))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(404)
            .withBody(objectMapper.writeValueAsString(SystemErrorDto.builder()
                .message("not found")
                .build())))));

    var contentAsString = performWithTokenOfficerRole(post("/api/task/" + testTaskId + "/claim"))
        .andExpect(status().isNotFound()).andReturn().getResponse()
        .getContentAsString(StandardCharsets.UTF_8);

    var response = objectMapper.readValue(contentAsString, SystemErrorDto.class);

    assertThat(response.getMessage()).isEqualTo("not found");
    assertThat(response.getLocalizedMessage()).isEqualTo("Задача не існує або вже виконана");
  }

  @Test
  public void shouldReturnBadRequestWithBrokenInputJson() throws Exception {
    MockHttpServletRequestBuilder request = post("/api/task/{taskId}/complete",
        "taskId", testTaskId)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"data\" : { \"}}");

    performWithTokenOfficerRole(request).andExpect(status().is(400));
  }
}