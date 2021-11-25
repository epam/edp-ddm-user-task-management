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

import com.epam.digital.data.platform.dso.api.dto.Subject;
import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.epam.digital.data.platform.usrtaskmgt.exception.SignatureValidationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Set;
import org.springframework.lang.NonNull;

/**
 * Service that used for verifying if digital signature corresponds to {@link FormDataDto form
 * data}.
 * <p>
 * Provides such methods:
 * <li>{@link DigitalSignatureRemoteService#verifyOfficerFormData(FormDataDto)} for verifying
 * officer form data</li>
 * <li>{@link DigitalSignatureRemoteService#verifyCitizenFormData(Set, FormDataDto)} for verifying
 * citizen form data</li>
 */
public interface DigitalSignatureRemoteService {

  /**
   * Verify signed form data by officer
   *
   * @param formData object that contains form data itself and its signature
   * @throws IllegalArgumentException     if there was a {@link JsonProcessingException} an
   *                                      exception during form data serialization
   * @throws SignatureValidationException if signature isn't corresponds to form data
   */
  void verifyOfficerFormData(@NonNull FormDataDto formData);

  /**
   * Verify signed form data by citizen. Checks if form data was signed by user with different
   * subject that isn't allowed for signing the form data
   *
   * @param signatureValidationPack set of allowed citizen subjects that should be verified (will be
   *                                verified individual subject if it's empty)
   * @param formData                object that contains form data itself and its signature
   * @throws IllegalArgumentException     if there was a {@link JsonProcessingException} an
   *                                      exception during form data serialization
   * @throws SignatureValidationException if signature isn't corresponds to form data
   */
  void verifyCitizenFormData(@NonNull Set<Subject> signatureValidationPack,
      @NonNull FormDataDto formData);
}
