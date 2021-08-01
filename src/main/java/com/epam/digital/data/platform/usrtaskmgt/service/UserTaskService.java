package com.epam.digital.data.platform.usrtaskmgt.service;

import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.epam.digital.data.platform.usrtaskmgt.dto.SignableUserTaskDto;
import com.epam.digital.data.platform.usrtaskmgt.dto.UserTaskDto;
import com.epam.digital.data.platform.usrtaskmgt.exception.SignatureValidationException;
import com.epam.digital.data.platform.usrtaskmgt.exception.UserTaskAlreadyAssignedException;
import com.epam.digital.data.platform.usrtaskmgt.exception.UserTaskNotExistsOrCompletedException;
import java.util.List;
import org.camunda.bpm.engine.rest.dto.CountResultDto;

/**
 * The UserTaskService class represents a service for {@link UserTaskDto} entity and
 * contains methods for working with a user tasks.
 */
public interface UserTaskService {

  /**
   * Method for getting list of user task entities.
   *
   * @param processInstanceId process instance identifier
   * @return the list of user tasks
   */
  List<UserTaskDto> getTasks(String processInstanceId);

  /**
   * Method for getting user task entity that can be signed by id.
   *
   * @param taskId task identifier
   * @return the user task entity that can be signed
   */
  SignableUserTaskDto getTaskById(String taskId);

  /**
   * Method for getting the number of tasks.
   *
   * @return the number of tasks
   */
  CountResultDto countTasks();

  /**
   * Method should complete user task by id. Before completion, {@link FormDataDto} entity must be
   * saved to the ceph.
   *
   * @param taskId      task identifier
   * @param formDataDto data to save to the ceph
   */
  void completeTaskById(String taskId, FormDataDto formDataDto);

  /**
   * Method should verify {@link FormDataDto} entity and complete task by id. Before completion,
   * form data must be saved to the ceph. Performed by a user with the role of an officer.
   *
   * @param taskId      task identifier
   * @param formDataDto data to verify
   * @throws SignatureValidationException if the form data is invalid
   */
  void signOfficerForm(String taskId, FormDataDto formDataDto);

  /**
   * Method should verify {@link FormDataDto} entity and complete task by id. Before completion,
   * form data must be saved to the ceph. Performed by a user with the role of an citizen.
   *
   * @param taskId      task identifier
   * @param formDataDto data to verify
   * @throws SignatureValidationException if the form data is invalid
   */
  void signCitizenForm(String taskId, FormDataDto formDataDto);

  /**
   * Method should claim task by id.
   *
   * @param taskId task identifier
   * @throws UserTaskNotExistsOrCompletedException if the task not found
   * @throws UserTaskAlreadyAssignedException      if the task already assigned to other user
   */
  void claimTaskById(String taskId);
}
