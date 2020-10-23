package com.epam.digital.data.platform.usrtaskmgt.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserTaskLightweightResponse {

  private String id;
  private String assignee;
}
