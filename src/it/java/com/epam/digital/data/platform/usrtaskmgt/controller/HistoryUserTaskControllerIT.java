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
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.epam.digital.data.platform.bpms.api.dto.HistoryUserTaskDto;
import com.epam.digital.data.platform.usrtaskmgt.BaseIT;
import com.epam.digital.data.platform.usrtaskmgt.model.StubRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
}
