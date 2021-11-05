package com.epam.digital.data.platform.usrtaskmgt;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.digital.data.platform.starter.security.jwt.TokenParser;
import com.epam.digital.data.platform.usrtaskmgt.config.TokenConfig;
import com.epam.digital.data.platform.usrtaskmgt.model.StubRequest;
import com.epam.digital.data.platform.usrtaskmgt.util.CephKeyProvider;
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
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseIT {

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

  @AfterEach
  public void tearDown() {
    bpmServer.resetAll();
    cephServer.resetAll();
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

  public void mockGetCephContent(String cephKey, String content) {
    mockGetBucket();

    var path = "/" + cephBucketName + "/" + cephKey;
    mockRequest(cephServer, StubRequest.builder()
        .method(HttpMethod.HEAD)
        .path(path)
        .status(200)
        .responseHeaders(Map.of("Content-Length", List.of(String.valueOf(content.length()))))
        .build());

    mockRequest(cephServer, StubRequest.builder()
        .method(HttpMethod.GET)
        .path(path)
        .status(200)
        .responseBody(content)
        .responseHeaders(Map.of("Content-Length", List.of(String.valueOf(content.length()))))
        .build());
  }

  public void mockPutCephContent(String cephKey, String body) {
    mockGetBucket();
    mockRequest(cephServer, StubRequest.builder()
        .method(HttpMethod.PUT)
        .path("/" + cephBucketName + "/" + cephKey)
        .requestBody(containing(body))
        .status(200)
        .build());
  }

  private void mockGetBucket() {
    mockRequest(cephServer, StubRequest.builder()
        .path("/")
        .method(HttpMethod.GET)
        .status(200)
        .responseBody(
            fileContent("/xml/cephBucketsResponse.xml").replaceAll(">\\s*\\r*\\n*\\s*<", "><"))
        .build());
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

  protected void mockValidationFormData(String body) {
    var formDataBody = String.format("{\"data\":%s}", body);
    mockRequest(formProviderServer, StubRequest.builder()
        .method(HttpMethod.POST)
        .path("/testFormKey/submission")
        .requestBody(equalTo(formDataBody))
        .queryParams(Map.of("dryrun", equalTo("1")))
        .status(200)
        .responseBody(formDataBody)
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
