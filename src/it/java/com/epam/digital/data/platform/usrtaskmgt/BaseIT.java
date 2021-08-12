package com.epam.digital.data.platform.usrtaskmgt;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.head;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.digital.data.platform.bpms.api.dto.TaskQueryDto;
import com.epam.digital.data.platform.dso.api.dto.VerificationResponseDto;
import com.epam.digital.data.platform.dso.api.dto.VerifySubjectResponseDto;
import com.epam.digital.data.platform.starter.security.jwt.TokenParser;
import com.epam.digital.data.platform.starter.validation.dto.FormDto;
import com.epam.digital.data.platform.usrtaskmgt.config.TokenConfig;
import com.epam.digital.data.platform.usrtaskmgt.dto.TestTaskDto;
import com.epam.digital.data.platform.usrtaskmgt.util.CephKeyProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.util.ArrayList;
import java.util.Calendar.Builder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.assertj.core.util.Lists;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricTaskInstanceEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseIT {

  public final String testTaskId = "testTaskId";
  public final Long testTaskCount = 11L;
  public final String testProcessInstanceId = "testProcessInstanceId";
  public final String processDefinitionId1 = "pdId1";

  @Autowired
  @Qualifier("bpms")
  protected WireMockServer bpmServer;
  @Autowired
  @Qualifier("dso")
  protected WireMockServer dsoServer;
  @Autowired
  @Qualifier("ceph")
  protected WireMockServer cephServer;
  @Autowired
  @Qualifier("form-provider")
  protected WireMockServer formProviderServer;
  @Autowired
  protected MockMvc mockMvc;

  @Autowired
  public ObjectMapper objectMapper;
  @Autowired
  protected TokenParser tokenParser;

  @Value("${ceph.bucket}")
  private String cephBucketName;
  @Autowired
  protected TokenConfig tokenConfig;
  @Autowired
  protected CephKeyProvider cephKeyProvider;

  @Before
  public void init() {
    mockTaskCount(testTaskCount);
    mockGetTasks();
    mockGetProcessDefinitionsByPdIds(Lists.newArrayList("pdId1", "pdId2"));
    mockTaskCompleteById(testTaskId);
    mockTaskByProcessInstanceId(testProcessInstanceId);
    mockHistoryTasks();
    mockTaskProperties();
  }

  @After
  public void tearDown() {
    bpmServer.resetAll();
    cephServer.resetAll();
  }

  @SneakyThrows
  public void mockTaskCount(Long count) {
    bpmServer.addStubMapping(
        stubFor(post(urlPathEqualTo("/api/task/count"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(objectMapper.writeValueAsString(new CountResultDto(count))))
        )
    );
  }

  @SneakyThrows
  public void mockTaskCountFail(int status, String body) {
    bpmServer.addStubMapping(
        stubFor(post(urlPathEqualTo("/api/task/count"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(status)
                .withBody(body))));
  }

  public void mockTaskByIdAndProcessDefinitionId(String id, String processDefinitionId) {
    mockTaskByParams(id, processDefinitionId, null, null);
  }

  @SneakyThrows
  public void mockTaskByParams(String id, String processDefinitionId, String processInstanceId,
      String taskDefinitionKey) {
    var task = new TestTaskDto();
    task.setFormKey("testFormKey");
    task.setId(id);
    task.setAssignee(tokenParser.parseClaims(tokenConfig.getValueWithRoleCitizen()).getPreferredUsername());
    task.setProcessDefinitionId(processDefinitionId);
    task.setProcessInstanceId(processInstanceId);
    task.setTaskDefinitionKey(taskDefinitionKey);
    task.setCreateTime(new Builder().setDate(2021, 2, 10).setTimeOfDay(13, 55, 10)
        .setTimeZone(TimeZone.getTimeZone("Etc/GMT+0")).build().getTime());
    TaskDto taskById = TaskDto.fromEntity(task);
    bpmServer.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/task/" + id))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(objectMapper.writeValueAsString(taskById)))
        )
    );
  }

  public void mockGetCephContent(String cephKey, String content) {
    mockGetBucket();

    cephServer.addStubMapping(
        stubFor(head(urlPathEqualTo("/" + cephBucketName + "/" + cephKey))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Length", String.valueOf(content.length())))));

    cephServer.addStubMapping(
        stubFor(get(urlPathEqualTo("/" + cephBucketName + "/" + cephKey))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Length", String.valueOf(content.length()))
                .withBody(content))));
  }

  public void mockPutCephContent(String cephKey, String body) {
    mockGetBucket();
    cephServer.addStubMapping(
        stubFor(put(urlPathEqualTo("/" + cephBucketName + "/" + cephKey))
            .withRequestBody(containing(body))
            .willReturn(aResponse()
                .withStatus(200))));
  }

  private void mockGetBucket() {
    cephServer.addStubMapping(
        stubFor(get(urlPathEqualTo("/")).willReturn(
            aResponse()
                .withStatus(200)
                .withBody("<?xml version=\"1.0\" encoding=\"UTF-8\"?><ListAllMyBucketsResult>"
                    + "<Buckets><Bucket><Name>" + cephBucketName + "</Name></Bucket></Buckets>"
                    + "</ListAllMyBucketsResult>"))));
  }

  @SneakyThrows
  public void mockGetTasks() {
    var task1 = new TaskEntity();
    task1.setId("task1");
    task1.setProcessDefinitionId("pdId1");
    task1.setSuspensionState(SuspensionState.ACTIVE.getStateCode());
    var taskDto1 = TaskDto.fromEntity(task1);
    var task2 = new TaskEntity();
    task2.setId("task2");
    task2.setProcessDefinitionId("pdId2");
    task2.setSuspensionState(SuspensionState.SUSPENDED.getStateCode());
    var taskDto2 = TaskDto.fromEntity(task2);
    bpmServer.addStubMapping(
        stubFor(post(urlPathEqualTo("/api/task"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(objectMapper.writeValueAsString(Lists.newArrayList(taskDto1, taskDto2))))
        )
    );
  }

  @SneakyThrows
  public void mockGetProcessDefinitionsByPdIds(List<String> pdIds) {
    var processDefinitionDtos = pdIds.stream().map(id -> {
      var definition = new ProcessDefinitionEntity();
      definition.setId(id);
      definition.setName("testName");
      return ProcessDefinitionDto.fromProcessDefinition(definition);
    }).collect(Collectors.toList());
    bpmServer.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/process-definition"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(objectMapper.writeValueAsString(processDefinitionDtos)))
        )
    );
  }

  @SneakyThrows
  public void mockTaskCompleteById(String taskId) {
    Map<String, VariableValueDto> completeVariables = new HashMap<>();
    completeVariables.put("var1", new VariableValueDto());
    bpmServer.addStubMapping(
        stubFor(post(urlEqualTo("/api/task/" + testTaskId + "/complete"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(completeVariables))))
    );
  }

  @SneakyThrows
  public void mockTaskByProcessInstanceId(String processInstanceId) {
    var task = new TaskEntity();
    task.setProcessInstanceId(processInstanceId);
    var taskDto = TaskDto.fromEntity(task);

    var requestDto = TaskQueryDto.builder().processInstanceId(testProcessInstanceId)
        .orQueries(Collections.singletonList(TaskQueryDto.builder()
            .assignee(tokenParser.parseClaims(tokenConfig.getValueWithRoleOfficer()).getPreferredUsername())
            .unassigned(true)
            .build()))
        .build();
    bpmServer.addStubMapping(
        stubFor(post(urlPathEqualTo("/api/task"))
            .withRequestBody(equalTo(objectMapper.writeValueAsString(requestDto)))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(objectMapper.writeValueAsString(Lists.newArrayList(taskDto)))))
    );
  }

  @SneakyThrows
  public void mockHistoryTasks() {
    HistoricTaskInstanceEntity historicTaskInstanceEntity = new HistoricTaskInstanceEntity();
    historicTaskInstanceEntity.setId("testHistoryId");
    bpmServer.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/history/task"))
            .withQueryParam("finished", equalTo("true"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(objectMapper
                    .writeValueAsString(Lists.newArrayList(historicTaskInstanceEntity)))))
    );
  }

  @SneakyThrows
  public void mockTaskProperties() {
    var properties = new HashMap<>();
    properties.put("eSign", "true");
    properties.put("INDIVIDUAL", "true");
    properties.put("ENTREPRENEUR", "false");
    properties.put("LEGAL", "true");
    properties.put("formVariables","fullName");
    bpmServer.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/extended/task/" + testTaskId + "/extension-element/property"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(objectMapper.writeValueAsString(properties)))
        )
    );
  }

  @SneakyThrows
  public void mockGetTaskVariables(String taskId) {
    var variableValue = "Test Full Name";
    var varValueDto = new VariableValueDto();
    varValueDto.setValue(variableValue);
    Map<String,VariableValueDto> expectedVariables = new HashMap<>();
    expectedVariables.put("fullName", varValueDto);
    bpmServer.addStubMapping(
        stubFor(get(urlPathEqualTo(String.format("/api/task/%s/variables", taskId)))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(objectMapper.writeValueAsString(expectedVariables)))
        )
    );
  }

  @SneakyThrows
  public void mockOfficerDigitalSignature(int status, VerificationResponseDto verifyResponseDto) {
    dsoServer.addStubMapping(
        stubFor(post(urlPathEqualTo("/api/esignature/officer/verify"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(status)
                .withBody(objectMapper.writeValueAsString(verifyResponseDto)))
        )
    );
  }

  @SneakyThrows
  public void mockCitizenDigitalSignature(int status, VerifySubjectResponseDto verifyResponseDto) {
    dsoServer.addStubMapping(
        stubFor(post(urlPathEqualTo("/api/esignature/citizen/verify"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(status)
                .withBody(objectMapper.writeValueAsString(verifyResponseDto)))
        )
    );
  }

  @SneakyThrows
  public void mockValidationFormData(String reqBody) {
    formProviderServer.addStubMapping(
        stubFor(post(urlPathEqualTo("/testFormKey/submission"))
            .withRequestBody(equalTo("{\"data\":{}}"))
            .withQueryParam("dryrun", equalTo("1"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(reqBody)
            )
        ));
  }

  @SneakyThrows
  public void mockGetForm() {
    formProviderServer.addStubMapping(
        stubFor(get(urlPathEqualTo("/testFormKey"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(objectMapper.writeValueAsString(new FormDto(new ArrayList<>())))
            )
        ));
  }

  @SneakyThrows
  protected <T> T performForObject(MockHttpServletRequestBuilder request, Class<T> tClass) {
    var json = performWithTokenOfficerRole(request)
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();
    return objectMapper.readValue(json, tClass);
  }

  @SneakyThrows
  protected ResultActions performWithTokenOfficerRole(MockHttpServletRequestBuilder request) {
    return mockMvc.perform(request.header(tokenConfig.getName(), tokenConfig.getValueWithRoleOfficer()));
  }

  @SneakyThrows
  protected ResultActions performWithTokenCitizenRole(MockHttpServletRequestBuilder request) {
    return mockMvc.perform(request.header(tokenConfig.getName(), tokenConfig.getValueWithRoleCitizen()));
  }

  @SneakyThrows
  protected ResultActions performWithTokenWithoutRole(MockHttpServletRequestBuilder request) {
    return mockMvc.perform(request.header(tokenConfig.getName(), tokenConfig.getValueWithoutRole()));
  }
}
