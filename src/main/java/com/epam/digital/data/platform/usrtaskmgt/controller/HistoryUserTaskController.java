package com.epam.digital.data.platform.usrtaskmgt.controller;

import com.epam.digital.data.platform.bpms.api.dto.HistoryUserTaskDto;
import com.epam.digital.data.platform.starter.security.annotation.PreAuthorizeAnySystemRole;
import com.epam.digital.data.platform.usrtaskmgt.model.Pageable;
import com.epam.digital.data.platform.usrtaskmgt.model.swagger.PageableAsQueryParam;
import com.epam.digital.data.platform.usrtaskmgt.service.HistoryUserTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/history/task")
@PreAuthorizeAnySystemRole
public class HistoryUserTaskController {

  private final HistoryUserTaskService historyUserTaskService;

  @GetMapping
  @Operation(
      summary = "Retrieve all completed tasks",
      description = "Returns completed task list")
  @PageableAsQueryParam
  public List<HistoryUserTaskDto> getHistoryTasks(@Parameter(hidden = true) Pageable pageable) {
    return historyUserTaskService.getHistoryTasks(pageable);
  }

  @GetMapping("/count")
  @Operation(summary = "Retrieve count of all finished tasks", description = "Returns finished tasks count")
  public CountResultDto countHistoryTasks() {
    return historyUserTaskService.countHistoryTasks();
  }
}