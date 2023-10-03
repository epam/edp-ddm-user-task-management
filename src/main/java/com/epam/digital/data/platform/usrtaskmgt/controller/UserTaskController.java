/*
 * Copyright 2023 EPAM Systems.
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
import com.epam.digital.data.platform.usrtaskmgt.controller.swagger.PageableAsQueryParam;
import com.epam.digital.data.platform.usrtaskmgt.model.request.Pageable;
import com.epam.digital.data.platform.usrtaskmgt.model.response.CompletedTaskResponse;
import com.epam.digital.data.platform.usrtaskmgt.model.response.CountResponse;
import com.epam.digital.data.platform.usrtaskmgt.model.response.SignableDataUserTaskResponse;
import com.epam.digital.data.platform.usrtaskmgt.model.response.UserTaskLightweightResponse;
import com.epam.digital.data.platform.usrtaskmgt.model.response.UserTaskResponse;
import com.epam.digital.data.platform.usrtaskmgt.service.UserTaskManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
@Tag(description = "User task management Rest API", name = "user-task-management-api")
public class UserTaskController {

  private final UserTaskManagementService userTaskManagementService;

  @GetMapping("/task")
  @Operation(summary = "Retrieve all tasks",
      description = "### Endpoint purpose:\n This endpoint allows users to retrieve a list of tasks associated with a specified process instance or user. Users can optionally filter tasks by providing a process instance ID. Pagination is supported via the pageable parameter. The endpoint returns a list of UserTaskResponse objects, each representing a retrieved task.",
      parameters = @Parameter(
          in = ParameterIn.HEADER,
          name = "X-Access-Token",
          description = "Token used for endpoint security",
          required = true,
          schema = @Schema(type = "string")
      ),
      responses = {
          @ApiResponse(
              description = "List of user tasks",
              responseCode = "200",
              content = @Content(schema = @Schema(implementation = UserTaskResponse.class),
                  examples = {
                      @ExampleObject(value = "[\n"
                          + "    {\n"
                          + "        \"id\": \"0b52527c-62ae-11ee-be57-0a580a810416\",\n"
                          + "        \"taskDefinitionKey\": \"UserTask_AddStatus\",\n"
                          + "        \"name\": \"my task name\",\n"
                          + "        \"assignee\": \"user\",\n"
                          + "        \"created\": \"2023-10-04T12:03:34.884Z\",\n"
                          + "        \"description\": \"some description\",\n"
                          + "        \"processDefinitionName\": \"my process name\",\n"
                          + "        \"processInstanceId\": \"fd3187f5-62ad-11ee-be57-0a580a810415\",\n"
                          + "        \"processDefinitionId\": \"Process_160gicr:14:b8fa558e-62aa-11ee-be57-0a580a810416\",\n"
                          + "        \"formKey\": null,\n"
                          + "        \"suspended\": false,\n"
                          + "        \"businessKey\": null\n"
                          + "    }]")
                  })),
          @ApiResponse(
              responseCode = "401",
              description = "Unauthorized",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
          ),
          @ApiResponse(
              responseCode = "500",
              description = "Internal server error",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
          )
      }
  )
  @PageableAsQueryParam
  public List<UserTaskResponse> getTasks(@RequestParam(required = false) String processInstanceId,
      @Parameter(hidden = true) Pageable pageable, Authentication authentication) {
    return userTaskManagementService.getTasks(processInstanceId, pageable, authentication);
  }

  @GetMapping("/task/lightweight")
  @Operation(summary = "Retrieve all tasks",
      description = "### Endpoint purpose:\n This endpoint allows users to retrieve a lightweight list of tasks associated with a specified process instance or user. Users can optionally filter tasks by providing a root process instance ID. The endpoint returns a list of lightweight user tasks. This lightweight version of the task list provides essential task details for efficient display purposes.",
      parameters = @Parameter(
          in = ParameterIn.HEADER,
          name = "X-Access-Token",
          description = "Token used for endpoint security",
          required = true,
          schema = @Schema(type = "string")
      ),
      responses = {
          @ApiResponse(
              description = "List of user lightweight tasks",
              responseCode = "200",
              content = @Content(schema = @Schema(implementation = UserTaskLightweightResponse.class),
                  examples = {
                      @ExampleObject(value = "[\n"
                          + "    {\n"
                          + "        \"id\": \"0b52527c-62ae-11ee-be57-0a580a810416\",\n"
                          + "        \"assignee\": \"user\",\n"
                          + "    },\n"
                          + "    {\n"
                          + "        \"id\": \"0b52527c-62ae-11ee-be57-0a580a2132312\",\n"
                          + "        \"assignee\": \"user\",\n"
                          + "    }\n"
                          + "]")
                  })),
          @ApiResponse(
              responseCode = "401",
              description = "Unauthorized",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
          ),
          @ApiResponse(
              responseCode = "500",
              description = "Internal server error",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
          )
      })
  @PageableAsQueryParam
  public List<UserTaskLightweightResponse> getLightweightTasks(
      @RequestParam(required = false) String rootProcessInstanceId,
      @Parameter(hidden = true) Pageable pageable, Authentication authentication) {
    return userTaskManagementService.getLightweightTasks(rootProcessInstanceId, pageable,
        authentication);
  }

  @GetMapping("/task/{id}")
  @Operation(summary = "Get task by id",
      description = "### Endpoint purpose:\n This endpoint allows users to retrieve detailed information about a specific task by providing its unique identifier (ID). The task details include information such as task status, assignee, due date, and other relevant data.",
      parameters = @Parameter(
          in = ParameterIn.HEADER,
          name = "X-Access-Token",
          description = "Token used for endpoint security",
          required = true,
          schema = @Schema(type = "string")
      ),
      responses = {
          @ApiResponse(
              description = "Returns detailed task information",
              responseCode = "200",
              content = @Content(schema = @Schema(implementation = SignableDataUserTaskResponse.class),
                  examples = {
                      @ExampleObject(value = "{\n"
                          + "    \"id\": \"97839db1-62b2-11ee-be57-0a580a810415\",\n"
                          + "    \"taskDefinitionKey\": \"UserTask_SignSuccessfulStatusActivity\",\n"
                          + "    \"name\": \"Sign data\",\n"
                          + "    \"assignee\": \"user\",\n"
                          + "    \"created\": \"2023-10-04T12:36:08.075Z\",\n"
                          + "    \"description\": null,\n"
                          + "    \"processInstanceId\": \"81ae5334-62b2-11ee-be57-0a580a810415\",\n"
                          + "    \"rootProcessInstanceId\": \"81ae5334-62b2-11ee-be57-0a580a810415\",\n"
                          + "    \"processDefinitionId\": \"Process_160gicr:15:4ef94837-62b0-11ee-be57-0a580a810415\",\n"
                          + "    \"processDefinitionName\": \"my-process\",\n"
                          + "    \"formKey\": \"my-user-task-form\",\n"
                          + "    \"suspended\": false,\n"
                          + "    \"formVariables\": {},\n"
                          + "    \"signatureValidationPack\": [],\n"
                          + "    \"data\": {\n"
                          + "        \"myField\": \"myValue\",\n"
                          + "        \"submit\": true\n"
                          + "    },\n"
                          + "    \"esign\": true\n"
                          + "}")
                  })),
          @ApiResponse(
              responseCode = "401",
              description = "Unauthorized",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
          ),
          @ApiResponse(
              responseCode = "404",
              description = "Not found",
              content = @Content(schema = @Schema(implementation = SystemErrorDto.class))
          ),
          @ApiResponse(
              responseCode = "500",
              description = "Internal server error",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
          )
      })
  public SignableDataUserTaskResponse getTaskById(@PathVariable("id") String taskId,
      Authentication authentication) {
    return userTaskManagementService.getTaskById(taskId, authentication);
  }

  @GetMapping("/task/count")
  @Operation(summary = "Retrieve count of all tasks",
      description = "### Endpoint purpose:\n This endpoint allows to retrieve the total count of all available tasks for user.",
      parameters = @Parameter(
          in = ParameterIn.HEADER,
          name = "X-Access-Token",
          description = "Token used for endpoint security",
          required = true,
          schema = @Schema(type = "string")
      ),
      responses = {
          @ApiResponse(
              description = "Returns detailed task information",
              responseCode = "200",
              content = @Content(schema = @Schema(implementation = CountResponse.class),
                  examples = {
                      @ExampleObject(value = "{\n"
                          + "    \"count\": 10,\n"
                          + "}")
                  })),
          @ApiResponse(
              responseCode = "401",
              description = "Unauthorized",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
          )
      })
  public CountResponse countTasks(Authentication authentication) {
    return userTaskManagementService.countTasks(authentication);
  }

  @PostMapping("/task/{id}/complete")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Complete task by id",
      description = "### Endpoint purpose:\n This endpoint allows users to complete a specific task by providing its unique identifier. Users must include the necessary data in the request body using a FormDataDto. Upon successful completion, information about the completed task is returned.\n"
          + "### Authorization:\n If user assigned to task does not match user retrieved from _X-Access-Token_ then _403 Forbidden_ status code returned.\n"
          + "### Form validation:\n This endpoint requires valid form, if form provided in request body does not match form structure assigned to task, then _422_ status code returned.",
      parameters = @Parameter(
          in = ParameterIn.HEADER,
          name = "X-Access-Token",
          description = "Token used for endpoint security",
          required = true,
          schema = @Schema(type = "string")
      ),
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FormDataDto.class),
              examples = {
                  @ExampleObject(value = "{\n"
                      + "  \"data\": {\n"
                      + "     \"formFieldName1\": \"field value 1\",\n"
                      + "     \"formFieldName2\": \"field value 2\"\n"
                      + "}}"
                  )
              }
          )
      ),
      responses = {
          @ApiResponse(
              description = "Task successfully completed",
              responseCode = "200",
              content = @Content(schema = @Schema(implementation = CompletedTaskResponse.class),
                  examples = {
                      @ExampleObject(value = "{\n"
                          + "    \"id\": \"d5a4eddf-6360-11ee-88e8-0a580a81041b\",\n"
                          + "    \"processInstanceId\": \"d5a40376-6360-11ee-88e8-0a580a81041b\",\n"
                          + "    \"rootProcessInstanceId\": \"d5a40376-6360-11ee-88e8-0a580a81041b\",\n"
                          + "    \"rootProcessInstanceEnded\": false,\n"
                          + "    \"variables\": {}\n"
                          + "}")
                  })),
          @ApiResponse(
              responseCode = "401",
              description = "Unauthorized",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
          ),
          @ApiResponse(
              description = "Forbidden",
              responseCode = "403",
              content = @Content(schema = @Schema(implementation = SystemErrorDto.class))),
          @ApiResponse(
              description = "Task hasn't found",
              responseCode = "404",
              content = @Content(schema = @Schema(implementation = SystemErrorDto.class))),
          @ApiResponse(
              description = "Form data is not valid",
              responseCode = "422",
              content = @Content(schema = @Schema(implementation = SystemErrorDto.class))),
          @ApiResponse(
              description = "Internal server error",
              responseCode = "500",
              content = @Content(schema = @Schema(implementation = SystemErrorDto.class)))
      }
  )

  public CompletedTaskResponse completeTaskById(@PathVariable("id") String taskId,
      @RequestBody FormDataDto formDataDto, Authentication authentication) {
    return userTaskManagementService.completeTaskById(taskId, formDataDto, authentication);
  }

  @PreAuthorizeOfficer
  @PostMapping("/officer/task/{id}/sign-form")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Sign and complete officer task by id",
      description = "### Endpoint purpose:\n This endpoint allows officer to sign form data for a specific task. Users must provide the task's unique identifier and the required form data with signature in the request body. Upon successful signing, information about the task is returned.\n"
          + "### Authorization:\n If user assigned to task does not match user retrieved from _X-Access-Token_ then _403 Forbidden_ status code returned.\n"
          + "### Form and signature validation:\n This endpoint requires valid form, if form provided in request body does not match form structure assigned to task or verification of provided signature is failed, then _422_ status code returned.",
      parameters = @Parameter(
          in = ParameterIn.HEADER,
          name = "X-Access-Token",
          description = "Token used for endpoint security",
          required = true,
          schema = @Schema(type = "string")
      ),
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FormDataDto.class),
              examples = {
                  @ExampleObject(value = "{\n"
                      + "  \"data\": {\n"
                      + "     \"formFieldName1\": \"field value 1\",\n"
                      + "     \"formFieldName2\": \"field value 2\"\n"
                      + "  },\n"
                      + "  \"signature\": \"Key-6.dat\""
                      + "}"
                  )
              }
          )
      ),
      responses = {
          @ApiResponse(
              description = "Task successfully signed and completed",
              responseCode = "200",
              content = @Content(schema = @Schema(implementation = CompletedTaskResponse.class),
                  examples = {
                      @ExampleObject(value = "{\n"
                          + "    \"id\": \"fed535d9-6360-11ee-88e8-0a580a81041b\",\n"
                          + "    \"processInstanceId\": \"d5a40376-6360-11ee-88e8-0a580a81041b\",\n"
                          + "    \"rootProcessInstanceId\": \"d5a40376-6360-11ee-88e8-0a580a81041b\",\n"
                          + "    \"rootProcessInstanceEnded\": true,\n"
                          + "    \"variables\": {}\n"
                          + "}"
                      )
                  })),
          @ApiResponse(
              responseCode = "401",
              description = "Unauthorized",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
          ),
          @ApiResponse(
              description = "Forbidden",
              responseCode = "403",
              content = @Content(schema = @Schema(implementation = SystemErrorDto.class))),
          @ApiResponse(
              description = "Task hasn't found",
              responseCode = "404",
              content = @Content(schema = @Schema(implementation = SystemErrorDto.class))),
          @ApiResponse(
              description = "Task hasn't verified",
              responseCode = "422",
              content = @Content(schema = @Schema(implementation = ValidationErrorDto.class))),
          @ApiResponse(
              description = "Internal server error",
              responseCode = "500",
              content = @Content(schema = @Schema(implementation = SystemErrorDto.class)))})

  public CompletedTaskResponse singOfficerForm(@PathVariable("id") String taskId,
      @RequestBody FormDataDto formDataDto, Authentication authentication) {
    return userTaskManagementService.signOfficerForm(taskId, formDataDto, authentication);
  }

  @PreAuthorizeCitizen
  @PostMapping("/citizen/task/{id}/sign-form")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Sign and complete citizen task by id",
      description = "### Endpoint purpose:\n This endpoint allows citizen to sign form data for a specific task. Users must provide the task's unique identifier and the required form data with signature in the request body. Upon successful signing, information about the task is returned.\n"
          + "### Authorization:\n If user assigned to task does not match user retrieved from _X-Access-Token_ then _403 Forbidden_ status code returned.\n"
          + "### Form and signature validation:\n This endpoint requires valid form, if form provided in request body does not match form structure assigned to task or verification of provided signature is failed, then _422_ status code returned.",
      parameters = @Parameter(
          in = ParameterIn.HEADER,
          name = "X-Access-Token",
          description = "Token used for endpoint security",
          required = true,
          schema = @Schema(type = "string")
      ),
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FormDataDto.class),
              examples = {
                  @ExampleObject(value = "{\n"
                      + "  \"data\": {\n"
                      + "     \"formFieldName1\": \"field value 1\",\n"
                      + "     \"formFieldName2\": \"field value 2\"\n"
                      + "  },\n"
                      + "  \"signature\": \"Key-6.dat\""
                      + "}"
                  )
              }
          )
      ),
      responses = {
          @ApiResponse(
              description = "Task successfully signed and completed",
              responseCode = "200",
              content = @Content(schema = @Schema(implementation = CompletedTaskResponse.class),
                  examples = {
                      @ExampleObject(value = "{\n"
                          + "    \"id\": \"fed535d9-6360-11ee-88e8-0a580a81041b\",\n"
                          + "    \"processInstanceId\": \"d5a40376-6360-11ee-88e8-0a580a81041b\",\n"
                          + "    \"rootProcessInstanceId\": \"d5a40376-6360-11ee-88e8-0a580a81041b\",\n"
                          + "    \"rootProcessInstanceEnded\": true,\n"
                          + "    \"variables\": {}\n"
                          + "}"
                      )
                  })),
          @ApiResponse(
              responseCode = "401",
              description = "Unauthorized",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
          ),
          @ApiResponse(
              description = "Forbidden",
              responseCode = "403",
              content = @Content(schema = @Schema(implementation = SystemErrorDto.class))),
          @ApiResponse(
              description = "Task hasn't found",
              responseCode = "404",
              content = @Content(schema = @Schema(implementation = SystemErrorDto.class))),
          @ApiResponse(
              description = "Task hasn't verified",
              responseCode = "422",
              content = @Content(schema = @Schema(implementation = ValidationErrorDto.class))),
          @ApiResponse(
              description = "Internal server error",
              responseCode = "500",
              content = @Content(schema = @Schema(implementation = SystemErrorDto.class)))
      })
  public CompletedTaskResponse signCitizenForm(
      @PathVariable("id") String taskId, @RequestBody FormDataDto formDataDto,
      Authentication authentication) {
    return userTaskManagementService.signCitizenForm(taskId, formDataDto, authentication);
  }

  @Operation(summary = "Claim task by id",
      description = "### Endpoint purpose:\n This endpoint allows users to claim a task by its unique identifier. Once a task is claimed, it becomes the responsibility of the user who claimed it and is no longer available for other users to claim.",
      parameters = @Parameter(
          in = ParameterIn.HEADER,
          name = "X-Access-Token",
          description = "Token used for endpoint security",
          required = true,
          schema = @Schema(type = "string")
      ),
      responses = {
          @ApiResponse(
              description = "Task successfully claimed",
              responseCode = "204"),
          @ApiResponse(
              description = "Task hasn't found or already completed",
              responseCode = "404",
              content = @Content(schema = @Schema(implementation = SystemErrorDto.class))),
          @ApiResponse(
              description = "Task already assigned on another person",
              responseCode = "409",
              content = @Content(schema = @Schema(implementation = SystemErrorDto.class))),
          @ApiResponse(
              description = "Internal server error",
              responseCode = "500",
              content = @Content(schema = @Schema(implementation = SystemErrorDto.class)))
      })

  @PostMapping("/task/{id}/claim")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void claimTaskById(@PathVariable("id") String taskId, Authentication authentication) {
    userTaskManagementService.claimTaskById(taskId, authentication);
  }

  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Save form data",
      description = "### Endpoint purpose:\n This endpoint allows to save form data to temporary storage without task completion.\n"
          + "### Authorization:\n If user assigned to task does not match user retrieved from _X-Access-Token_ then _403 Forbidden_ status code returned.\n"
          + "### Form validation:\n This endpoint requires valid form, if form provided in request body does not match form structure assigned to task, then _422_ status code returned.",
      parameters = @Parameter(
          in = ParameterIn.HEADER,
          name = "X-Access-Token",
          description = "Token used for endpoint security",
          required = true,
          schema = @Schema(type = "string")
      ),
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FormDataDto.class),
              examples = {
                  @ExampleObject(value = "{\n"
                      + "  \"data\": {\n"
                      + "     \"formFieldName1\": \"field value 1\",\n"
                      + "     \"formFieldName2\": \"field value 2\"\n"
                      + "}}"
                  )
              }
          )
      ),
      responses = {
          @ApiResponse(
              description = "Form data successfully saved",
              responseCode = "200"),
          @ApiResponse(
              responseCode = "401",
              description = "Unauthorized",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
          ),
          @ApiResponse(
              description = "Forbidden",
              responseCode = "403",
              content = @Content(schema = @Schema(implementation = SystemErrorDto.class))),
          @ApiResponse(
              description = "Task hasn't found",
              responseCode = "404",
              content = @Content(schema = @Schema(implementation = SystemErrorDto.class))),
          @ApiResponse(
              description = "Form data validation error",
              responseCode = "422",
              content = @Content(schema = @Schema(implementation = ValidationErrorDto.class))),
          @ApiResponse(
              description = "Internal server error",
              responseCode = "500",
              content = @Content(schema = @Schema(implementation = SystemErrorDto.class)))
      })

  @PostMapping("/task/{id}/save")
  public void saveFormData(@PathVariable("id") String taskId, @RequestBody FormDataDto formDataDto,
      Authentication authentication) {
    userTaskManagementService.saveFormData(taskId, formDataDto, authentication);
  }
}
