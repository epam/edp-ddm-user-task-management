package com.epam.digital.data.platform.usrtaskmgt.service.internal;

import com.epam.digital.data.platform.bpms.api.dto.ProcessDefinitionQueryDto;
import com.epam.digital.data.platform.bpms.client.ProcessDefinitionRestClient;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessDefinitionService {

  private final ProcessDefinitionRestClient processDefinitionRestClient;

  public Map<String, String> getProcessDefinitionNames(List<String> processDefinitionIds) {
    log.debug("Selecting process definitions for extracting names. Ids - {}", processDefinitionIds);
    var processDefinitionQueryDto =
        ProcessDefinitionQueryDto.builder().processDefinitionIdIn(processDefinitionIds).build();
    var processDefinitions =
        processDefinitionRestClient.getProcessDefinitionsByParams(processDefinitionQueryDto);
    return processDefinitions.stream()
        .collect(Collectors.toMap(ProcessDefinitionDto::getId, ProcessDefinitionDto::getName));
  }
}
