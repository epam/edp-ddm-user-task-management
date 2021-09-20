package com.epam.digital.data.platform.usrtaskmgt.mapper;

import com.epam.digital.data.platform.usrtaskmgt.model.HistoryUserTaskDto;
import com.epam.digital.data.platform.usrtaskmgt.model.SignableUserTaskDto;
import com.epam.digital.data.platform.usrtaskmgt.model.UserTaskDto;
import java.util.List;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricTaskInstanceEntity;
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
   * Method for converting list of camunda {@link TaskDto} entities to list of {@link UserTaskDto}
   * entities
   *
   * @param taskDtos list of camunda task entities
   * @return a list of user tasks
   */
  List<UserTaskDto> toUserTasks(List<TaskDto> taskDtos);

  /**
   * Method for converting list of camunda {@link HistoricTaskInstanceEntity} entities to list of
   * {@link HistoryUserTaskDto} entities
   *
   * @param historyTasks list of finished camunda task entities
   * @return a list of finished tasks.
   */
  List<HistoryUserTaskDto> toHistoryUserTasks(List<HistoricTaskInstanceEntity> historyTasks);

  /**
   * Method for converting camunda {@link TaskDto} entity to {@link UserTaskDto} entity
   *
   * @param taskDto camunda task entity
   * @return a user task entity
   */
  UserTaskDto toUserTaskDto(TaskDto taskDto);

  /**
   * Method for converting camunda {@link TaskDto} entity to {@link SignableUserTaskDto} entity
   *
   * @param taskDto camunda task entity
   * @return a user task entity that can be signed
   */
  SignableUserTaskDto toSignableUserTaskDto(TaskDto taskDto);
}
