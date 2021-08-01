package com.epam.digital.data.platform.usrtaskmgt.exception;

import lombok.Getter;

/**
 * Exception that is thrown when user does not have permissions for interaction with a user task.
 */
@Getter
public class UserTaskAuthorizationException extends RuntimeException {

  private final String taskId;

  public UserTaskAuthorizationException(String message, String taskId) {
    super(message);
    this.taskId = taskId;
  }
}
