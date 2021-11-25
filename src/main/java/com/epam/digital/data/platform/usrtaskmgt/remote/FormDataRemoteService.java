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

import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.epam.digital.data.platform.usrtaskmgt.exception.FormDataStorageException;
import java.util.Map;
import org.springframework.lang.NonNull;

/**
 * Base service that is responsible for accessing form data in ceph (reading and writing)
 */
public interface FormDataRemoteService {

  /**
   * Save form data in form data storage (ceph)
   *
   * @param taskDefinitionKey task definition key (used for generating ceph key)
   * @param processInstanceId id of process instance (used for generating ceph key)
   * @param formData          the form data that has to be saved
   * @throws FormDataStorageException if there was an error during saving the form data
   */
  void saveFormData(@NonNull String taskDefinitionKey, @NonNull String processInstanceId,
      @NonNull FormDataDto formData);

  /**
   * Get form data from form data storage (ceph) if it exists
   *
   * @param taskDefinitionKey task definition key (used for generating ceph key)
   * @param processInstanceId id of process instance (used for generating ceph key)
   */
  @NonNull
  Map<String, Object> getFormData(@NonNull String taskDefinitionKey,
      @NonNull String processInstanceId);
}
