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

package com.epam.digital.data.platform.usrtaskmgt.remote.impl;

import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.epam.digital.data.platform.integration.ceph.exception.CephCommunicationException;
import com.epam.digital.data.platform.integration.ceph.service.FormDataCephService;
import com.epam.digital.data.platform.usrtaskmgt.exception.FormDataStorageException;
import com.epam.digital.data.platform.usrtaskmgt.remote.FormDataRemoteService;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

/**
 * Base service that is responsible for accessing form data in ceph (reading and writing)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FormDataRemoteServiceImpl implements FormDataRemoteService {

  private static final LinkedHashMap<String, Object> EMPTY_FORM_DATA = new LinkedHashMap<>();

  private final CephKeyProvider cephKeyProvider;
  private final FormDataCephService cephService;

  @Override
  public void saveFormData(@NonNull String taskDefinitionKey, @NonNull String processInstanceId,
      @NonNull FormDataDto formData) {
    var formDataCephKey = cephKeyProvider.generateKey(taskDefinitionKey, processInstanceId);

    putFormDataToCeph(formDataCephKey, formData);
  }

  @Override
  @NonNull
  public Map<String, Object> getFormData(@NonNull String taskDefinitionKey,
      @NonNull String processInstanceId) {
    var cephKey = cephKeyProvider.generateKey(taskDefinitionKey, processInstanceId);

    return getFormDataFromCeph(cephKey)
        .map(FormDataDto::getData)
        .orElse(EMPTY_FORM_DATA);
  }

  private void putFormDataToCeph(String cephKey, FormDataDto formData) {
    try {
      cephService.putFormData(cephKey, formData);
    } catch (CephCommunicationException ex) {
      var message = String.format("Couldn't put form data with key %s to ceph", cephKey);
      log.warn(message, ex);
      throw new FormDataStorageException(message, ex);
    }
  }

  private Optional<FormDataDto> getFormDataFromCeph(String formDataCephKey) {
    try {
      return cephService.getFormData(formDataCephKey);
    } catch (CephCommunicationException ex) {
      log.warn("Couldn't get form data with key {} from ceph", formDataCephKey, ex);
      return Optional.empty();
    }
  }
}
