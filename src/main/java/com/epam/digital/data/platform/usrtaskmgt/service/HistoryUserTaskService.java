package com.epam.digital.data.platform.usrtaskmgt.service;

import com.epam.digital.data.platform.bpms.api.dto.HistoryUserTaskDto;
import com.epam.digital.data.platform.usrtaskmgt.model.Pageable;
import java.util.List;
import org.camunda.bpm.engine.rest.dto.CountResultDto;

/**
 * The HistoryUserTaskService class represents a service for {@link HistoryUserTaskDto} entity and
 * contains methods for working with a finished user tasks.
 * <p>
 * The HistoryUserTaskService class provides a method to get a list of finished user tasks
 */
public interface HistoryUserTaskService {


  /**
   * Method for getting a list of finished user task entities.
   *
   * @param pageable specifies the index of the first result, maximum number of results and result
   *                 sorting
   * @return the list of finished user tasks.
   */
  List<HistoryUserTaskDto> getHistoryTasks(Pageable pageable);

  /**
   * Method for getting the number of finished tasks.
   *
   * @return the number of finished tasks
   */
  CountResultDto countHistoryTasks();
}