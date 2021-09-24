package com.epam.digital.data.platform.usrtaskmgt.service;

import com.epam.digital.data.platform.bpms.api.dto.HistoryTaskCountQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.HistoryTaskQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.HistoryUserTaskDto;
import com.epam.digital.data.platform.bpms.api.dto.PaginationQueryDto;
import com.epam.digital.data.platform.bpms.client.ExtendedHistoryUserTaskRestClient;
import com.epam.digital.data.platform.bpms.client.HistoryTaskRestClient;
import com.epam.digital.data.platform.usrtaskmgt.model.Pageable;
import com.epam.digital.data.platform.usrtaskmgt.util.AuthUtil;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryUserTaskServiceImpl implements HistoryUserTaskService {

  private final HistoryTaskRestClient historyTaskRestClient;
  private final ExtendedHistoryUserTaskRestClient extendedHistoryTaskRestClient;

  @Override
  public List<HistoryUserTaskDto> getHistoryTasks(Pageable page) {
    log.info("Getting finished user tasks. Parameters: {}", page);

    var historyTaskQueryDto = buildHistoryTaskQueryDto(page);
    var paginationQueryDto = buildPaginationQueryDto(page);
    var historyTasksByParams = extendedHistoryTaskRestClient.getHistoryUserTasksByParams(
        historyTaskQueryDto, paginationQueryDto);
    log.trace("Found user tasks - {}", historyTasksByParams);

    log.info("Found {} user tasks. Task ids - {}", historyTasksByParams.size(),
        historyTasksByParams.stream().map(HistoryUserTaskDto::getId)
            .collect(Collectors.joining(", ")));

    return historyTasksByParams;
  }

  @Override
  public CountResultDto countHistoryTasks() {
    log.info("Getting count of finished user tasks");

    var result = historyTaskRestClient.getHistoryTaskCountByParams(
        HistoryTaskCountQueryDto.builder()
            .taskAssignee(AuthUtil.getCurrentUsername())
            .finished(true)
            .build());

    log.info("Count of finished user tasks is found - {}", result.getCount());
    return result;
  }

  private HistoryTaskQueryDto buildHistoryTaskQueryDto(Pageable pageable) {
    return HistoryTaskQueryDto.builder()
        .sortBy(pageable.getSortBy())
        .sortOrder(pageable.getSortOrder())
        .taskAssignee(AuthUtil.getCurrentUsername())
        .finished(true)
        .build();
  }

  private PaginationQueryDto buildPaginationQueryDto(Pageable pageable) {
    return PaginationQueryDto.builder()
        .firstResult(pageable.getFirstResult())
        .maxResults(pageable.getMaxResults())
        .build();
  }
}
