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

import com.epam.digital.data.platform.starter.errorhandling.dto.SystemErrorDto;
import com.epam.digital.data.platform.starter.errorhandling.dto.ValidationErrorDto;
import com.epam.digital.data.platform.starter.security.annotation.PreAuthorizeAnySystemRole;
import com.epam.digital.data.platform.starter.security.annotation.PreAuthorizeCitizen;
import com.epam.digital.data.platform.starter.security.annotation.PreAuthorizeOfficer;
import com.epam.digital.data.platform.storage.form.dto.FormDataDto;
import com.epam.digital.data.platform.usrtaskmgt.model.request.Pageable;
import com.epam.digital.data.platform.usrtaskmgt.controller.swagger.PageableAsQueryParam;
import com.epam.digital.data.platform.usrtaskmgt.model.response.CountResponse;
import com.epam.digital.data.platform.usrtaskmgt.model.response.SignableDataUserTaskResponse;
import com.epam.digital.data.platform.usrtaskmgt.model.response.UserTaskResponse;
import com.epam.digital.data.platform.usrtaskmgt.service.UserTaskManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@PreAuthorizeAnySystemRole
public class UserTaskController {

  private final UserTaskManagementService userTaskManagementService;

  @GetMapping("/task")
  @Operation(summary = "Retrieve all tasks", description = "Returns task list")
  @PageableAsQueryParam
  public List<UserTaskResponse> getTasks(@RequestParam(required = false) String processInstanceId,
      @Parameter(hidden = true) Pageable pageable, Authentication authentication) {
    return userTaskManagementService.getTasks(processInstanceId, pageable, authentication);
  }

  @GetMapping("/task/{id}")
  @Operation(summary = "Get task by id", description = "Returns task by id")
  @ApiResponse(
      description = "Returns task by id",
      responseCode = "200",
      content = @Content(schema = @Schema(implementation = SignableDataUserTaskResponse.class)))
  @ApiResponse(
      description = "Task hasn't found",
      responseCode = "404",
      content = @Content(schema = @Schema(implementation = SystemErrorDto.class)))
  public SignableDataUserTaskResponse getTaskById(@PathVariable("id") String taskId,
      Authentication authentication) {
    return userTaskManagementService.getTaskById(taskId, authentication);
  }

  @GetMapping("/task/count")
  @Operation(summary = "Retrieve count of all tasks", description = "Returns tasks count")
  public CountResponse countTasks(Authentication authentication) {
    return userTaskManagementService.countTasks(authentication);
  }

  @PostMapping("/task/{id}/complete")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Complete task by id")
  @ApiResponse(description = "Task successfully completed", responseCode = "204")
  @ApiResponse(
      description = "Task hasn't found",
      responseCode = "404",
      content = @Content(schema = @Schema(implementation = SystemErrorDto.class)))
  @ApiResponse(
      description = "Internal server error",
      responseCode = "500",
      content = @Content(schema = @Schema(implementation = SystemErrorDto.class)))
  public void completeTaskById(@PathVariable("id") String taskId,
      @RequestBody FormDataDto formDataDto, Authentication authentication) {
    userTaskManagementService.completeTaskById(taskId, formDataDto, authentication);
  }

  @PreAuthorizeOfficer
  @PostMapping("/officer/task/{id}/sign-form")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Sign and complete officer task by id")
  @ApiResponse(description = "Task successfully signed and completed", responseCode = "204")
  @ApiResponse(
      description = "Task hasn't found",
      responseCode = "404",
      content = @Content(schema = @Schema(implementation = SystemErrorDto.class)))
  @ApiResponse(
      description = "Task hasn't verified",
      responseCode = "422",
      content = @Content(schema = @Schema(implementation = ValidationErrorDto.class)))
  @ApiResponse(
      description = "Internal server error",
      responseCode = "500",
      content = @Content(schema = @Schema(implementation = SystemErrorDto.class)))
  public void singOfficerForm(@PathVariable("id") String taskId,
      @RequestBody FormDataDto formDataDto, Authentication authentication) {
    userTaskManagementService.signOfficerForm(taskId, formDataDto, authentication);
  }

  @PreAuthorizeCitizen
  @PostMapping("/citizen/task/{id}/sign-form")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Sign and complete citizen task by id")
  @ApiResponse(description = "Task successfully signed and completed", responseCode = "204")
  @ApiResponse(
      description = "Task hasn't found",
      responseCode = "404",
      content = @Content(schema = @Schema(implementation = SystemErrorDto.class)))
  @ApiResponse(
      description = "Task hasn't verified",
      responseCode = "422",
      content = @Content(schema = @Schema(implementation = ValidationErrorDto.class)))
  @ApiResponse(
      description = "Internal server error",
      responseCode = "500",
      content = @Content(schema = @Schema(implementation = SystemErrorDto.class)))
  public void signCitizenForm(
      @PathVariable("id") String taskId, @RequestBody FormDataDto formDataDto,
      Authentication authentication) {
    userTaskManagementService.signCitizenForm(taskId, formDataDto, authentication);
  }

  @Operation(summary = "Claim task by id")
  @ApiResponse(description = "Task successfully claimed", responseCode = "204")
  @ApiResponse(
      description = "Task hasn't found or already completed",
      responseCode = "404",
      content = @Content(schema = @Schema(implementation = SystemErrorDto.class)))
  @ApiResponse(
      description = "Task already assigned on another person",
      responseCode = "409",
      content = @Content(schema = @Schema(implementation = SystemErrorDto.class)))
  @ApiResponse(
      description = "Internal server error",
      responseCode = "500",
      content = @Content(schema = @Schema(implementation = SystemErrorDto.class)))
  @PostMapping("/task/{id}/claim")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void claimTaskById(@PathVariable("id") String taskId, Authentication authentication) {
    userTaskManagementService.claimTaskById(taskId, authentication);
  }
}
