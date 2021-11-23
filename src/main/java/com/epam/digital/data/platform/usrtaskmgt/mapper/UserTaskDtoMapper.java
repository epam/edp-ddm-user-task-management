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

import com.epam.digital.data.platform.bpms.api.dto.SignableUserTaskDto;
import com.epam.digital.data.platform.bpms.api.dto.UserTaskDto;
import com.epam.digital.data.platform.usrtaskmgt.model.SignableDataUserTaskDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * The interface represents a mapper for user task entity. The interface contains a methods for
 * converting camunda task entity.The methods are implemented using the MapStruct.
 */
@Mapper(uses = MapperUtils.class, componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserTaskDtoMapper {

  /**
   * Method for converting camunda {@link TaskDto} entity to {@link UserTaskDto} entity
   *
   * @param taskDto camunda task entity
   * @return a user task entity
   */
  UserTaskDto toUserTaskDto(TaskDto taskDto);

  /**
   * Method for converting camunda {@link TaskDto} entity to {@link SignableDataUserTaskDto} entity
   *
   * @param taskDto camunda task entity
   * @return a user task entity that can be signed
   */
  SignableDataUserTaskDto toSignableDataUserTaskDto(TaskDto taskDto);

  /**
   * Method for converting {@link SignableDataUserTaskDto} entity to {@link SignableDataUserTaskDto}
   * entity
   *
   * @param signableUserTaskDto bpms task entity
   * @return a user task entity that can be signed
   */
  SignableDataUserTaskDto toSignableDataUserTaskDto(SignableUserTaskDto signableUserTaskDto);
}
