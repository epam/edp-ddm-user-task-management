package com.epam.digital.data.platform.usrtaskmgt.service;

import static java.util.Objects.nonNull;

import com.epam.digital.data.platform.bpms.api.dto.HistoryTaskQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.ProcessDefinitionQueryDto;
import com.epam.digital.data.platform.bpms.client.HistoryTaskRestClient;
import com.epam.digital.data.platform.bpms.client.ProcessDefinitionRestClient;
import com.epam.digital.data.platform.usrtaskmgt.dto.HistoryUserTaskDto;
import com.epam.digital.data.platform.usrtaskmgt.mapper.UserTaskDtoMapper;
import com.epam.digital.data.platform.usrtaskmgt.util.AuthUtil;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricTaskInstanceEntity;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HistoryUserTaskServiceImpl implements HistoryUserTaskService {

  private final ProcessDefinitionRestClient processDefinitionRestClient;
  private final HistoryTaskRestClient historyTaskRestClient;
  private final UserTaskDtoMapper userTaskDtoMapper;

  @Override
  public List<HistoryUserTaskDto> getHistoryTasks() {
    List<HistoricTaskInstanceEntity> historyTasksByParams = historyTaskRestClient
        .getHistoryTasksByParams(
            HistoryTaskQueryDto.builder()
                .taskAssignee(AuthUtil.getCurrentUsername())
                .finished(true)
            .build());
    return postProcess(userTaskDtoMapper.toHistoryUserTasks(historyTasksByParams));
  }

  private List<HistoryUserTaskDto> postProcess(List<HistoryUserTaskDto> historyUserTaskDtos) {
    fillProcessDefinitionName(historyUserTaskDtos);
    return historyUserTaskDtos;
  }

  private void fillProcessDefinitionName(List<HistoryUserTaskDto> historyUserTaskDtos) {
    List<String> processDefinitionIds = historyUserTaskDtos.stream()
        .map(HistoryUserTaskDto::getProcessDefinitionId)
        .distinct().collect(Collectors.toList());
    ProcessDefinitionQueryDto processDefinitionQueryDto = ProcessDefinitionQueryDto.builder()
        .processDefinitionIdIn(processDefinitionIds)
        .build();
    List<ProcessDefinitionDto> processDefinitions = processDefinitionRestClient
        .getProcessDefinitionsByParams(processDefinitionQueryDto);
    Map<String, ProcessDefinitionDto> processDefinitionIdAndDtoMap = processDefinitions.stream()
        .collect(Collectors.toMap(ProcessDefinitionDto::getId, Function.identity()));
    historyUserTaskDtos.forEach(task -> {
      ProcessDefinitionDto processDefinitionDto = processDefinitionIdAndDtoMap
          .get(task.getProcessDefinitionId());
      if (nonNull(processDefinitionDto)) {
        task.setProcessDefinitionName(processDefinitionDto.getName());
      }
    });
  }
}
