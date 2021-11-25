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

import com.epam.digital.data.platform.usrtaskmgt.model.request.Pageable;
import com.epam.digital.data.platform.usrtaskmgt.model.response.HistoryUserTaskResponse;
import com.epam.digital.data.platform.usrtaskmgt.remote.HistoryUserTaskRemoteService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Represents a service that contains methods for working with a history user tasks.
 * <p>
 * Implements such business functions:
 * <li>{@link HistoryUserTaskManagementService#getHistoryTasks(Pageable, Authentication) Getting
 * completed user tasks}</li>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryUserTaskManagementService {

  private final HistoryUserTaskRemoteService historyUserTaskRemoteService;

  /**
   * Getting list of completed user task
   *
   * @param page           object that contains paging and sorting parameters
   * @param authentication current user authentication object
   * @return list of completed user task
   */
  public List<HistoryUserTaskResponse> getHistoryTasks(Pageable page,
      Authentication authentication) {
    log.info("Getting finished user tasks. Parameters: {}", page);

    var result = historyUserTaskRemoteService.getHistoryTasks(authentication.getName(), page);
    log.trace("Found user tasks - {}", result);

    log.info("Found {} user tasks", result.size());
    return result;
  }
}
