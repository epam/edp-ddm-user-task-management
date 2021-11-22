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
import org.springframework.security.core.Authentication;
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
  public List<HistoryUserTaskDto> getHistoryTasks(@Parameter(hidden = true) Pageable pageable,
      Authentication authentication) {
    return historyUserTaskService.getHistoryTasks(pageable, authentication);
  }

  @GetMapping("/count")
  @Operation(summary = "Retrieve count of all finished tasks", description = "Returns finished tasks count")
  public CountResultDto countHistoryTasks(Authentication authentication) {
    return historyUserTaskService.countHistoryTasks(authentication);
  }
}