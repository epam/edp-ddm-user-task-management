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

package com.epam.digital.data.platform.usrtaskmgt.mapper;

import com.epam.digital.data.platform.bpms.api.dto.DdmSignableTaskDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmTaskDto;
import com.epam.digital.data.platform.usrtaskmgt.model.response.CountResponse;
import com.epam.digital.data.platform.usrtaskmgt.model.response.SignableDataUserTaskResponse;
import com.epam.digital.data.platform.usrtaskmgt.model.response.UserTaskResponse;
import java.util.List;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

/**
 * The interface represents a mapper for user task entity. The interface contains a methods for
 * converting camunda task entity.The methods are implemented using the MapStruct.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserTaskDtoMapper {

  /**
   * Method for converting {@link DdmTaskDto} entity to {@link UserTaskResponse} entity
   *
   * @param dto bpms task entity
   * @return a user task entity
   */
  @Named("toUserTaskDto")
  UserTaskResponse toUserTaskDto(DdmTaskDto dto);

  @IterableMapping(qualifiedByName = "toUserTaskDto")
  List<UserTaskResponse> toUserTaskDtoList(List<DdmTaskDto> dtos);

  /**
   * Method for converting {@link SignableDataUserTaskResponse} entity to {@link
   * SignableDataUserTaskResponse} entity
   *
   * @param signableUserTaskDto bpms task entity
   * @return a user task entity that can be signed
   */
  SignableDataUserTaskResponse toSignableDataUserTaskDto(DdmSignableTaskDto signableUserTaskDto);

  CountResponse toCountResponse(CountResultDto dto);
}
