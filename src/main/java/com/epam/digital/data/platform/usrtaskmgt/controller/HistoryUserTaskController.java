package com.epam.digital.data.platform.usrtaskmgt.controller;

import com.epam.digital.data.platform.starter.security.annotation.PreAuthorizeAnySystemRole;
import com.epam.digital.data.platform.usrtaskmgt.dto.HistoryUserTaskDto;
import com.epam.digital.data.platform.usrtaskmgt.service.HistoryUserTaskService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
  public List<HistoryUserTaskDto> getHistoryTasks(@RequestParam(required = false) Integer firstResult,
      @RequestParam(required = false) Integer maxResults,
      @RequestParam(required = false) String sortBy,
      @RequestParam(required = false) String sortOrder) {
    return historyUserTaskService.getHistoryTasks(firstResult, maxResults, sortBy, sortOrder);
  }

  @GetMapping("/count")
  @Operation(summary = "Retrieve count of all finished tasks", description = "Returns finished tasks count")
  public CountResultDto countHistoryTasks() {
    return historyUserTaskService.countHistoryTasks();
  }
}