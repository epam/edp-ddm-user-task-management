package com.epam.digital.data.platform.usrtaskmgt.service;

import com.epam.digital.data.platform.usrtaskmgt.dto.HistoryUserTaskDto;
import java.util.List;

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
   * @return the list of finished user tasks.
   */
  List<HistoryUserTaskDto> getHistoryTasks();
}
