package com.epam.digital.data.platform.usrtaskmgt.exception;

import com.epam.digital.data.platform.dso.api.dto.ErrorDto;

/**
 * The class represents an exception for digital signature validation which will be thrown in case
 * of a validation error.
 */
public class SignatureValidationException extends RuntimeException {

  private final ErrorDto errorDto;

  public SignatureValidationException(ErrorDto errorDto) {
    this.errorDto = errorDto;
  }

  public ErrorDto getErrorDto() {
    return this.errorDto;
  }
}
