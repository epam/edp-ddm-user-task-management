package com.epam.digital.data.platform.usrtaskmgt.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.epam.digital.data.platform.usrtaskmgt.BaseIT;
import com.epam.digital.data.platform.usrtaskmgt.dto.HistoryUserTaskDto;
import java.util.Arrays;
import java.util.List;
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
}
