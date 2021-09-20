package com.epam.digital.data.platform.usrtaskmgt.model;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * The class represents a data transfer object for user task, the difference with {@link
 * UserTaskDto} entity - two additional fields:
 *  - {@link SignableUserTaskDto#eSign} this is a flag, if true, then the task can be signed
 *  - {@link SignableUserTaskDto#data} this is a data to sign
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SignableUserTaskDto {

  private String id;
  private String name;
  private String assignee;
  private LocalDateTime created;
  private String description;
  private String processInstanceId;
  private String processDefinitionId;
  private String formKey;
  private boolean eSign;
  private Map<String, Object> data;
  private boolean suspended;
  private Map<String, Object> formVariables;
}
