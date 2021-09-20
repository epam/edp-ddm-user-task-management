package com.epam.digital.data.platform.usrtaskmgt.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.epam.digital.data.platform.usrtaskmgt.BaseIT;
import com.epam.digital.data.platform.usrtaskmgt.model.HistoryUserTaskDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Arrays;
import java.util.List;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

public class HistoryUserTaskControllerIT extends BaseIT {

  @Test
  public void shouldGetHistoryTasks() {
    MockHttpServletRequestBuilder request = get("/api/history/task?finished=true")
        .accept(MediaType.APPLICATION_JSON_VALUE);

    List<HistoryUserTaskDto> historyUserTaskDtos = Arrays
        .asList(performForObject(request, HistoryUserTaskDto[].class));

    assertThat(historyUserTaskDtos.size()).isOne();
    assertThat(historyUserTaskDtos.get(0).getId()).isEqualTo("testHistoryId");
  }

  @Test
  public void shouldCountHistoryTasks() throws JsonProcessingException {
    bpmServer.addStubMapping(
        stubFor(post(urlPathEqualTo("/api/history/task/count"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(objectMapper.writeValueAsString(new CountResultDto(11L))))
        )
    );

    var request = get("/api/history/task/count").accept(MediaType.APPLICATION_JSON_VALUE);
    var count = performForObject(request, CountResultDto.class);

    assertThat(count).isNotNull();
    assertThat(count.getCount()).isEqualTo(testTaskCount);
  }
}
