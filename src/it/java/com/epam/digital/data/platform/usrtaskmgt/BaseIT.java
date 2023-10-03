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

package com.epam.digital.data.platform.usrtaskmgt;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.digital.data.platform.starter.security.jwt.TokenParser;
import com.epam.digital.data.platform.storage.form.service.FormDataKeyProvider;
import com.epam.digital.data.platform.storage.form.service.FormDataKeyProviderImpl;
import com.epam.digital.data.platform.storage.form.service.FormDataStorageService;
import com.epam.digital.data.platform.usrtaskmgt.config.TokenConfig;
import com.epam.digital.data.platform.usrtaskmgt.model.StubRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.ContentPattern;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SuppressWarnings({"SameParameterValue", "unused"})
@AutoConfigureMockMvc
@ActiveProfiles({"test", "local"})
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = UserTaskManagementApplication.class)
public abstract class BaseIT {

  @Autowired
  @Qualifier("bpms")
  protected WireMockServer bpmServer;
  @Autowired
  @Qualifier("dso")
  protected WireMockServer dsoServer;
  @Autowired
  @Qualifier("form-provider")
  protected WireMockServer formProviderServer;
  @Autowired
  protected MockMvc mockMvc;
  @Autowired
  protected FormDataStorageService<?> formDataStorageService;

  @Autowired
  public ObjectMapper objectMapper;
  @Autowired
  protected TokenParser tokenParser;

  @Value("${ceph.bucket}")
  private String cephBucketName;
  @Autowired
  protected TokenConfig tokenConfig;

  protected FormDataKeyProvider formDataKeyProvider = new FormDataKeyProviderImpl();

  @AfterEach
  public void tearDown() {
    bpmServer.resetAll();
  }

  protected final void mockBpmsRequest(StubRequest stubRequest) {
    mockRequest(bpmServer, stubRequest);
  }

  private void mockRequest(WireMockServer mockServer, StubRequest stubRequest) {
    var mappingBuilderMethod = getMappingBuilderMethod(stubRequest.getMethod());
    var mappingBuilder = mappingBuilderMethod.apply(urlPathEqualTo(stubRequest.getPath()));
    stubRequest.getQueryParams().forEach(mappingBuilder::withQueryParam);
    stubRequest.getRequestHeaders().forEach(
        (header, values) -> values.forEach(value -> mappingBuilder.withHeader(header, value)));
    if (Objects.nonNull(stubRequest.getRequestBody())) {
      mappingBuilder.withRequestBody(stubRequest.getRequestBody());
    }

    var response = aResponse().withStatus(stubRequest.getStatus());
    stubRequest.getResponseHeaders()
        .forEach((header, values) -> response.withHeader(header, values.toArray(new String[0])));
    if (Objects.nonNull(stubRequest.getResponseBody())) {
      response.withBody(stubRequest.getResponseBody());
    }

    mockServer.addStubMapping(stubFor(mappingBuilder.willReturn(response)));
  }

  private Function<UrlPattern, MappingBuilder> getMappingBuilderMethod(HttpMethod method) {
    switch (method) {
      case GET:
        return WireMock::get;
      case PUT:
        return WireMock::put;
      case POST:
        return WireMock::post;
      case DELETE:
        return WireMock::delete;
      case HEAD:
        return WireMock::head;
      case OPTIONS:
        return WireMock::options;
      case PATCH:
        return WireMock::patch;
      case TRACE:
        return WireMock::trace;
      default:
        throw new IllegalStateException("All http methods are mapped with mapping builder");
    }
  }

  @SneakyThrows
  protected final String fileContent(String filePath) {
    var resource = BaseIT.class.getResource(filePath);
    if (Objects.isNull(resource)) {
      throw new IllegalArgumentException(
          String.format("Resource %s not found in classpath", filePath));
    }
    return Files.readString(Paths.get(resource.toURI()), StandardCharsets.UTF_8);
  }

  protected void mockOfficerDigitalSignature(int status, String responseBody) {
    mockDigitalSignature("/api/esignature/officer/verify", null, status, responseBody);
  }

  protected void mockCitizenDigitalSignature(ContentPattern<String> requestBody, int status,
      String responseBody) {
    mockDigitalSignature("/api/esignature/citizen/verify", requestBody, status, responseBody);
  }

  protected void mockDigitalSignature(String path, ContentPattern<String> requestBody, int status,
      String responseBody) {
    mockRequest(dsoServer, StubRequest.builder()
        .method(HttpMethod.POST)
        .requestBody(requestBody)
        .path(path)
        .status(status)
        .responseBody(responseBody)
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .build());
  }

  protected void mockValidationValidFormData(String data, String processInstanceId) {
    mockValidationFormData(data, processInstanceId, "{}", 200);
  }

  protected void mockValidationFormData(String data, String processInstanceId, String responseBody, int status) {
    var formDataBody =
        String.format("{\"data\":%s,\"processInstanceId\":\"%s\"}", data, processInstanceId);
    mockRequest(formProviderServer, StubRequest.builder()
        .method(HttpMethod.POST)
        .path("/api/form-submissions/testFormKey/validate")
        .requestBody(equalTo(formDataBody))
        .status(status)
        .responseBody(responseBody)
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .build());
  }

  protected void mockGetForm() {
    mockRequest(formProviderServer, StubRequest.builder()
        .path("/testFormKey")
        .method(HttpMethod.GET)
        .status(200)
        .responseBody("{\"components\":[]}")
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .build());
  }

  protected <T> T performForObjectAsOfficer(MockHttpServletRequestBuilder request,
      Class<T> tClass) {
    return performForObjectAsOfficerWithStatus(request, tClass, status().isOk());
  }

  protected <T> T performForObjectAsCitizen(MockHttpServletRequestBuilder request,
      Class<T> tClass) {
    return performForObjectAsCitizenWithStatus(request, tClass, status().isOk());
  }

  protected <T> T performForObjectAsOfficerWithStatus(MockHttpServletRequestBuilder request,
      Class<T> tClass, ResultMatcher resultMatcher) {
    return performForObjectWithStatus(performWithTokenOfficerRole(request), tClass,
        resultMatcher);
  }

  protected <T> T performForObjectAsCitizenWithStatus(MockHttpServletRequestBuilder request,
      Class<T> tClass, ResultMatcher resultMatcher) {
    return performForObjectWithStatus(performWithTokenCitizenRole(request), tClass,
        resultMatcher);
  }

  @SneakyThrows
  protected <T> T performForObjectWithStatus(ResultActions actions, Class<T> tClass,
      ResultMatcher resultMatcher) {
    var json = actions
        .andExpect(resultMatcher)
        .andReturn()
        .getResponse()
        .getContentAsString(StandardCharsets.UTF_8);
    return objectMapper.readValue(json, tClass);
  }

  protected ResultActions performWithTokenOfficerRole(MockHttpServletRequestBuilder request) {
    return performWithToken(request, tokenConfig.getValueWithRoleOfficer());
  }

  protected ResultActions performWithTokenCitizenRole(MockHttpServletRequestBuilder request) {
    return performWithToken(request, tokenConfig.getValueWithRoleCitizen());
  }

  protected ResultActions performWithTokenWithoutRole(MockHttpServletRequestBuilder request) {
    return performWithToken(request, tokenConfig.getValueWithoutRole());
  }

  @SneakyThrows
  private ResultActions performWithToken(MockHttpServletRequestBuilder request, String token) {
    return mockMvc.perform(request.header(tokenConfig.getName(), token));
  }
}
