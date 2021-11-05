package com.epam.digital.data.platform.usrtaskmgt.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.epam.digital.data.platform.bpms.api.dto.HistoryUserTaskDto;
import com.epam.digital.data.platform.usrtaskmgt.BaseIT;
import com.epam.digital.data.platform.usrtaskmgt.model.StubRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

class HistoryUserTaskControllerIT extends BaseIT {

  @Test
  void shouldGetHistoryTasks() {
    mockBpmsRequest(StubRequest.builder()
        .method(HttpMethod.POST)
        .path("/api/extended/history/task")
        .requestBody(equalToJson("{\"taskAssignee\": \"testuser\", \"finished\": true}"))
        .status(200)
        .responseBody("[{\"id\":\"testHistoryId\"}]")
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .build());

    var request = get("/api/history/task?finished=true")
        .accept(MediaType.APPLICATION_JSON_VALUE);

    var historyUserTaskDtos = Arrays
        .asList(performForObjectAsOfficer(request, HistoryUserTaskDto[].class));

    assertThat(historyUserTaskDtos.size()).isOne();
    assertThat(historyUserTaskDtos.get(0).getId()).isEqualTo("testHistoryId");
  }

  @Test
  void shouldCountHistoryTasks() {
    var testTaskCount = 11L;
    mockBpmsRequest(StubRequest.builder()
        .method(HttpMethod.POST)
        .path("/api/history/task/count")
        .status(200)
        .responseBody(String.format("{\"count\":%d}", testTaskCount))
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .build());

    var request = get("/api/history/task/count").accept(MediaType.APPLICATION_JSON_VALUE);
    var count = performForObjectAsOfficer(request, CountResultDto.class);

    assertThat(count).isNotNull();
    assertThat(count.getCount()).isEqualTo(testTaskCount);
  }
}
