package com.epam.digital.data.platform.usrtaskmgt.exception;

import com.epam.digital.data.platform.bpms.client.exception.TaskNotFoundException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Exception that is thrown when camunda user task wasn't found. Contains client cause exception
 */
@Getter
@RequiredArgsConstructor
public class UserTaskNotExistsOrCompletedException extends RuntimeException {

  private final TaskNotFoundException cause;
}
