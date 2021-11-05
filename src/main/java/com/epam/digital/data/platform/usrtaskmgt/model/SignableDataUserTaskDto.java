package com.epam.digital.data.platform.usrtaskmgt.model;

import com.epam.digital.data.platform.bpms.api.dto.SignableUserTaskDto;
import com.epam.digital.data.platform.dso.api.dto.Subject;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * The class represents a data transfer object for user task, the difference with {@link
 * SignableUserTaskDto} entity additional field:
 * <li>{@link SignableDataUserTaskDto#data} this is a data to sign</li>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SignableDataUserTaskDto {

  private String id;
  private String taskDefinitionKey;
  private String name;
  private String assignee;
  private LocalDateTime created;
  private String description;
  private String processInstanceId;
  private String processDefinitionId;
  private String processDefinitionName;
  private String formKey;
  private boolean eSign;
  private Map<String, Object> data;
  private boolean suspended;
  private Map<String, Object> formVariables;
  private Set<Subject> signatureValidationPack;
}
