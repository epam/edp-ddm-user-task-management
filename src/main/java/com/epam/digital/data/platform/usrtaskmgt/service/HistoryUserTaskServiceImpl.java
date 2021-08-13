package com.epam.digital.data.platform.usrtaskmgt.service;

import static java.util.Objects.nonNull;

import com.epam.digital.data.platform.bpms.api.dto.HistoryTaskCountQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.HistoryTaskQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.ProcessDefinitionQueryDto;
import com.epam.digital.data.platform.bpms.client.HistoryTaskRestClient;
import com.epam.digital.data.platform.bpms.client.ProcessDefinitionRestClient;
import com.epam.digital.data.platform.usrtaskmgt.dto.HistoryUserTaskDto;
import com.epam.digital.data.platform.usrtaskmgt.mapper.UserTaskDtoMapper;
import com.epam.digital.data.platform.usrtaskmgt.util.AuthUtil;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricTaskInstanceEntity;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HistoryUserTaskServiceImpl implements HistoryUserTaskService {

  private final ProcessDefinitionRestClient processDefinitionRestClient;
  private final HistoryTaskRestClient historyTaskRestClient;
  private final UserTaskDtoMapper userTaskDtoMapper;

  @Override
  public List<HistoryUserTaskDto> getHistoryTasks(Integer firstResult, Integer maxResults, String sortBy,
      String sortOrder) {
    List<HistoricTaskInstanceEntity> historyTasksByParams = historyTaskRestClient
        .getHistoryTasksByParams(
            HistoryTaskQueryDto.builder()
                .firstResult(firstResult)
                .maxResults(maxResults)
                .sortBy(sortBy)
                .sortOrder(sortOrder)
                .taskAssignee(AuthUtil.getCurrentUsername())
                .finished(true)
                .build());
    return postProcess(userTaskDtoMapper.toHistoryUserTasks(historyTasksByParams));
  }

  @Override
  public CountResultDto countHistoryTasks() {
    return historyTaskRestClient.getHistoryTaskCountByParams(HistoryTaskCountQueryDto.builder()
        .taskAssignee(AuthUtil.getCurrentUsername())
        .finished(true)
        .build());
  }

  private List<HistoryUserTaskDto> postProcess(List<HistoryUserTaskDto> historyUserTaskDtos) {
    fillProcessDefinitionName(historyUserTaskDtos);
    return historyUserTaskDtos;
  }

  private void fillProcessDefinitionName(List<HistoryUserTaskDto> historyUserTaskDtos) {
    var processDefinitionIds = historyUserTaskDtos.stream()
        .map(HistoryUserTaskDto::getProcessDefinitionId)
        .distinct().collect(Collectors.toList());
    var processDefinitionQueryDto = ProcessDefinitionQueryDto.builder()
        .processDefinitionIdIn(processDefinitionIds)
        .build();
    var processDefinitions = processDefinitionRestClient
        .getProcessDefinitionsByParams(processDefinitionQueryDto);
    var processDefinitionIdAndDtoMap = processDefinitions.stream()
        .collect(Collectors.toMap(ProcessDefinitionDto::getId, Function.identity()));
    historyUserTaskDtos.forEach(task -> {
      var processDefinitionDto = processDefinitionIdAndDtoMap
          .get(task.getProcessDefinitionId());
      if (nonNull(processDefinitionDto)) {
        task.setProcessDefinitionName(processDefinitionDto.getName());
      }
    });
  }
}