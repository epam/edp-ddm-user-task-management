package com.epam.digital.data.platform.usrtaskmgt.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Class that represents request params for configure pagination parameters to shrink results. Is
 * used for get request that expects list in the response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Pageable {

  private Integer firstResult;
  private Integer maxResults;
  private String sortBy;
  private String sortOrder;
}
