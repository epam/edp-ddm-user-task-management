package com.epam.digital.data.platform.usrtaskmgt.service;

import com.epam.digital.data.platform.usrtaskmgt.dto.HistoryUserTaskDto;
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
   * @param firstResult specifies the index of the first result
   * @param maxResults specifies the maximum number of results
   * @param sortBy parameter to sort the results by a given criterion
   * @param sortOrder parameter to sort the results in a given order
   * @return the list of finished user tasks.
   */
  List<HistoryUserTaskDto> getHistoryTasks(Integer firstResult, Integer maxResults, String sortBy,
      String sortOrder);

  /**
   * Method for getting the number of finished tasks.
   *
   * @return the number of finished tasks
   */
  CountResultDto countHistoryTasks();
}