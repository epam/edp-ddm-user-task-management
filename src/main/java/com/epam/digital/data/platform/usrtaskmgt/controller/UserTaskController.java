package com.epam.digital.data.platform.usrtaskmgt.controller;

import com.epam.digital.data.platform.bpms.api.dto.UserTaskDto;
import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.epam.digital.data.platform.starter.errorhandling.dto.SystemErrorDto;
import com.epam.digital.data.platform.starter.errorhandling.dto.ValidationErrorDto;
import com.epam.digital.data.platform.starter.security.annotation.PreAuthorizeAnySystemRole;
import com.epam.digital.data.platform.starter.security.annotation.PreAuthorizeCitizen;
import com.epam.digital.data.platform.starter.security.annotation.PreAuthorizeOfficer;
import com.epam.digital.data.platform.usrtaskmgt.model.Pageable;
import com.epam.digital.data.platform.usrtaskmgt.model.SignableUserTaskDto;
import com.epam.digital.data.platform.usrtaskmgt.model.swagger.PageableAsQueryParam;
import com.epam.digital.data.platform.usrtaskmgt.service.UserTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@PreAuthorizeAnySystemRole
public class UserTaskController {

  private final UserTaskService userTaskService;

  @GetMapping("/task")
  @Operation(summary = "Retrieve all tasks", description = "Returns task list")
  @PageableAsQueryParam
  public List<UserTaskDto> getTasks(@RequestParam(required = false) String processInstanceId,
      @Parameter(hidden = true) Pageable pageable) {
    return userTaskService.getTasks(processInstanceId, pageable);
  }

  @GetMapping("/task/{id}")
  @Operation(summary = "Get task by id", description = "Returns task by id")
  @ApiResponse(
      description = "Returns task by id",
      responseCode = "200",
      content = @Content(schema = @Schema(implementation = SignableUserTaskDto.class)))
  @ApiResponse(
      description = "Task hasn't found",
      responseCode = "404",
      content = @Content(schema = @Schema(implementation = SystemErrorDto.class)))
  public SignableUserTaskDto getTaskById(@PathVariable("id") String taskId) {
    return userTaskService.getTaskById(taskId);
  }

  @GetMapping("/task/count")
  @Operation(summary = "Retrieve count of all tasks", description = "Returns tasks count")
  public CountResultDto countTasks() {
    return userTaskService.countTasks();
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
  public void completeTaskById(
      @PathVariable("id") String taskId,
      @RequestBody FormDataDto formDataDto,
      @RequestHeader("x-access-token") String accessToken) {
    formDataDto.setAccessToken(accessToken);
    userTaskService.completeTaskById(taskId, formDataDto);
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
  public void singOfficerForm(
      @PathVariable("id") String taskId,
      @RequestBody FormDataDto formDataDto,
      @RequestHeader("x-access-token") String accessToken) {
    formDataDto.setAccessToken(accessToken);
    userTaskService.signOfficerForm(taskId, formDataDto);
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
  public void singCitizenForm(
      @PathVariable("id") String taskId,
      @RequestBody FormDataDto formDataDto,
      @RequestHeader("x-access-token") String accessToken) {
    formDataDto.setAccessToken(accessToken);
    userTaskService.signCitizenForm(taskId, formDataDto);
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
  public void claimTaskById(@PathVariable("id") String taskId) {
    userTaskService.claimTaskById(taskId);
  }
}
