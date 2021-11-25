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

package com.epam.digital.data.platform.usrtaskmgt.remote.impl;

import com.epam.digital.data.platform.bpms.api.dto.HistoryTaskQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.PaginationQueryDto;
import com.epam.digital.data.platform.bpms.client.ExtendedHistoryUserTaskRestClient;
import com.epam.digital.data.platform.usrtaskmgt.mapper.HistoryUserTaskMapper;
import com.epam.digital.data.platform.usrtaskmgt.model.request.Pageable;
import com.epam.digital.data.platform.usrtaskmgt.model.response.HistoryUserTaskResponse;
import com.epam.digital.data.platform.usrtaskmgt.remote.HistoryUserTaskRemoteService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryUserTaskRemoteServiceImpl implements HistoryUserTaskRemoteService {

  private final ExtendedHistoryUserTaskRestClient extendedHistoryTaskRestClient;
  private final HistoryUserTaskMapper historyUserTaskMapper;

  @Override
  @NonNull
  public List<HistoryUserTaskResponse> getHistoryTasks(@NonNull String assignee,
      @NonNull Pageable page) {
    log.debug("Querying user task list. Parameters - {}", page);
    var historyTaskQueryDto = HistoryTaskQueryDto.builder()
        .sortBy(page.getSortBy())
        .sortOrder(page.getSortOrder())
        .taskAssignee(assignee)
        .finished(true)
        .build();
    var paginationQueryDto = PaginationQueryDto.builder()
        .firstResult(page.getFirstResult())
        .maxResults(page.getMaxResults())
        .build();

    var dtos = extendedHistoryTaskRestClient.getHistoryUserTasksByParams(
        historyTaskQueryDto, paginationQueryDto);
    log.debug("Found {} user tasks. {}", dtos.size(), dtos);
    return historyUserTaskMapper.toHistoryUserTaskResponseList(dtos);
  }
}
