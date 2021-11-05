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
