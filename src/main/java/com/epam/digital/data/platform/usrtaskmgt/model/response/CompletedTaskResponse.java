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

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompletedTaskResponse {

  private String id;
  private String processInstanceId;
  private String rootProcessInstanceId;
  private boolean rootProcessInstanceEnded;
  private Map<String, VariableValueResponse> variables;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static final class VariableValueResponse {

    private String type;
    private Object value;
    private Map<String, Object> valueInfo;
  }
}
