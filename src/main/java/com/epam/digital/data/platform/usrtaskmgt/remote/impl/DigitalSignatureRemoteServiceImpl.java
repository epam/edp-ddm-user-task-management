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

import com.epam.digital.data.platform.dso.api.dto.Subject;
import com.epam.digital.data.platform.dso.api.dto.VerificationRequestDto;
import com.epam.digital.data.platform.dso.api.dto.VerifySubjectRequestDto;
import com.epam.digital.data.platform.dso.client.DigitalSignatureRestClient;
import com.epam.digital.data.platform.storage.form.dto.FormDataDto;
import com.epam.digital.data.platform.usrtaskmgt.exception.SignatureValidationException;
import com.epam.digital.data.platform.usrtaskmgt.remote.DigitalSignatureRemoteService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DigitalSignatureRemoteServiceImpl implements DigitalSignatureRemoteService {

  private final DigitalSignatureRestClient digitalSignatureRestClient;
  private final ObjectMapper objectMapper;

  @Override
  public void verifyOfficerFormData(@NonNull FormDataDto formData) {
    log.debug("Verifying officer signed form data.");

    var signature = formData.getSignature();
    var data = serializeFormData(formData.getData());
    var requestDto = new VerificationRequestDto(signature, data);

    var verifyResponseDto = digitalSignatureRestClient.verifyOfficer(requestDto);

    if (!verifyResponseDto.isValid()) {
      log.error("Officer task form data hasn't passed the signature verification");
      throw new SignatureValidationException(verifyResponseDto.getError());
    }
    log.debug("Officer signed form data verified.");
  }

  @Override
  public void verifyCitizenFormData(@NonNull Set<Subject> signatureValidationPack,
      @NonNull FormDataDto formData) {
    log.debug("Verifying citizen signed form data.");

    var allowedSubjects = getAllowedSubjects(signatureValidationPack);
    var signature = formData.getSignature();
    var data = serializeFormData(formData.getData());
    var requestDto = new VerifySubjectRequestDto(allowedSubjects, signature, data);

    var verifyResponseDto = digitalSignatureRestClient.verifyCitizen(requestDto);

    if (!verifyResponseDto.isValid()) {
      log.error("Citizen task form data hasn't passed the signature verification");
      throw new SignatureValidationException(verifyResponseDto.getError());
    }
    log.debug("Citizen signed form data verified.");
  }

  private <T> String serializeFormData(T formData) {
    try {
      return objectMapper.writeValueAsString(formData);
    } catch (JsonProcessingException e) {
      e.clearLocation();
      throw new IllegalArgumentException("Couldn't serialize form data", e);
    }
  }

  private List<Subject> getAllowedSubjects(Set<Subject> signatureValidationPack) {
    return signatureValidationPack.isEmpty()
        ? List.of(Subject.INDIVIDUAL)
        : new ArrayList<>(signatureValidationPack);
  }
}
