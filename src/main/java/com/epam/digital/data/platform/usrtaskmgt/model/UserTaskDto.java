package com.epam.digital.data.platform.usrtaskmgt.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * The class represents a data transfer object for user task.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserTaskDto {

  private String id;
  private String name;
  private String assignee;
  private LocalDateTime created;
  private String description;
  private String processDefinitionName;
  private String processInstanceId;
  private String processDefinitionId;
  private String formKey;
  private boolean suspended;
}
