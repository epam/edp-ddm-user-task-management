package com.epam.digital.data.platform.usrtaskmgt.service;

import com.epam.digital.data.platform.bpms.api.dto.HistoryTaskCountQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.HistoryTaskQueryDto;
import com.epam.digital.data.platform.bpms.client.HistoryTaskRestClient;
import com.epam.digital.data.platform.usrtaskmgt.mapper.UserTaskDtoMapper;
import com.epam.digital.data.platform.usrtaskmgt.model.HistoryUserTaskDto;
import com.epam.digital.data.platform.usrtaskmgt.model.Pageable;
import com.epam.digital.data.platform.usrtaskmgt.service.internal.ProcessDefinitionService;
import com.epam.digital.data.platform.usrtaskmgt.util.AuthUtil;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricTaskInstanceEntity;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryUserTaskServiceImpl implements HistoryUserTaskService {

  private final HistoryTaskRestClient historyTaskRestClient;

  private final ProcessDefinitionService processDefinitionService;

  private final UserTaskDtoMapper userTaskDtoMapper;

  @Override
  public List<HistoryUserTaskDto> getHistoryTasks(Pageable page) {
    log.info("Getting finished user tasks. Parameters: {}", page);

    var historyTaskQueryDto = buildHistoryTaskQueryDto(page);
    var historyTasksByParams = historyTaskRestClient.getHistoryTasksByParams(historyTaskQueryDto);
    log.trace("Found {} history tasks", historyTasksByParams.size());
    var processDefinitionIds = extractProcessDefinitionIds(historyTasksByParams);
    log.trace("Found {} process definition ids from task list. Result - {}",
        processDefinitionIds.size(), processDefinitionIds);
    var processDefinitionNames = processDefinitionService
        .getProcessDefinitionNames(processDefinitionIds);
    log.trace("Found process definition names - {}", processDefinitionNames);

    var result = userTaskDtoMapper.toHistoryUserTasks(historyTasksByParams);
    result.forEach(task -> task.setProcessDefinitionName(
        processDefinitionNames.get(task.getProcessDefinitionId())));
    log.trace("Found user tasks - {}", result);

    log.info("Found {} user tasks. Task ids - {}", result.size(),
        result.stream().map(HistoryUserTaskDto::getId).collect(Collectors.joining(", ")));

    return result;
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
        .firstResult(pageable.getFirstResult())
        .maxResults(pageable.getMaxResults())
        .sortBy(pageable.getSortBy())
        .sortOrder(pageable.getSortOrder())
        .taskAssignee(AuthUtil.getCurrentUsername())
        .finished(true)
        .build();
  }

  private List<String> extractProcessDefinitionIds(List<HistoricTaskInstanceEntity> tasks) {
    return tasks.stream()
        .map(HistoricTaskInstanceEntity::getProcessDefinitionId)
        .distinct()
        .collect(Collectors.toList());
  }
}
