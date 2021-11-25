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

package com.epam.digital.data.platform.usrtaskmgt.remote;

import com.epam.digital.data.platform.usrtaskmgt.model.request.Pageable;
import com.epam.digital.data.platform.usrtaskmgt.model.response.HistoryUserTaskResponse;
import java.util.List;
import org.springframework.lang.NonNull;

public interface HistoryUserTaskRemoteService {

  /**
   * Method for getting a list of finished task entities for specific user.
   *
   * @param assignee the assignee username
   * @param page     specifies the index of the first result, maximum number of results and result
   *                 sorting
   * @return the list of finished user tasks.
   */
  @NonNull
  List<HistoryUserTaskResponse> getHistoryTasks(@NonNull String assignee, @NonNull Pageable page);

}
