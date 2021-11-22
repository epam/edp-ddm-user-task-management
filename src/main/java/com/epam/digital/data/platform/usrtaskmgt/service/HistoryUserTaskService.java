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

package com.epam.digital.data.platform.usrtaskmgt.service;

import com.epam.digital.data.platform.bpms.api.dto.HistoryUserTaskDto;
import com.epam.digital.data.platform.usrtaskmgt.model.Pageable;
import java.util.List;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.springframework.security.core.Authentication;

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
   * @param pageable       specifies the index of the first result, maximum number of results and
   *                       result sorting
   * @param authentication object with authentication data
   * @return the list of finished user tasks.
   */
  List<HistoryUserTaskDto> getHistoryTasks(Pageable pageable, Authentication authentication);

  /**
   * Method for getting the number of finished tasks.
   *
   * @param authentication object with authentication data
   * @return the number of finished tasks
   */
  CountResultDto countHistoryTasks(Authentication authentication);
}