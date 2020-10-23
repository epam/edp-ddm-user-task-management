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

package com.epam.digital.data.platform.usrtaskmgt.model.response;

import com.epam.digital.data.platform.bpms.api.dto.DdmSignableTaskDto;
import com.epam.digital.data.platform.dso.api.dto.Subject;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The class represents a data transfer object for user task, the difference with {@link
 * DdmSignableTaskDto} entity additional field:
 * <li>{@link SignableDataUserTaskResponse#data} this is a data to sign</li>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignableDataUserTaskResponse {

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
  private boolean suspended;
  private Map<String, Object> formVariables;
  private Set<Subject> signatureValidationPack;

  private Map<String, Object> data;
}
