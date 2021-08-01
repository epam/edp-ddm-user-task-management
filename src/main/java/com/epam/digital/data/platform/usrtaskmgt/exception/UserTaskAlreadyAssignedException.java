package com.epam.digital.data.platform.usrtaskmgt.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Exception that is thrown when user tries to claim the task that is already assigned to other
 * user
 */
@Getter
@RequiredArgsConstructor
public class UserTaskAlreadyAssignedException extends RuntimeException {

  private final String taskName;
  private final String message;
}
