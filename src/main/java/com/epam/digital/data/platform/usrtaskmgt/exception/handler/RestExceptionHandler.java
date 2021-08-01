package com.epam.digital.data.platform.usrtaskmgt.exception.handler;

import com.epam.digital.data.platform.starter.errorhandling.BaseRestExceptionHandler;
import com.epam.digital.data.platform.starter.errorhandling.dto.ErrorDetailDto;
import com.epam.digital.data.platform.starter.errorhandling.dto.ErrorsListDto;
import com.epam.digital.data.platform.starter.errorhandling.dto.SystemErrorDto;
import com.epam.digital.data.platform.starter.errorhandling.dto.ValidationErrorDto;
import com.epam.digital.data.platform.starter.localization.MessageResolver;
import com.epam.digital.data.platform.usrtaskmgt.enums.UserTaskManagementMessage;
import com.epam.digital.data.platform.usrtaskmgt.exception.SignatureValidationException;
import com.epam.digital.data.platform.usrtaskmgt.exception.UserTaskAlreadyAssignedException;
import com.epam.digital.data.platform.usrtaskmgt.exception.UserTaskAuthorizationException;
import com.epam.digital.data.platform.usrtaskmgt.exception.UserTaskNotExistsException;
import com.epam.digital.data.platform.usrtaskmgt.exception.UserTaskNotExistsOrCompletedException;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * The class represents a handler for digital signature validation exception. Contains a method to
 * handle {@link SignatureValidationException} exception.
 */
@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class RestExceptionHandler {

  private final MessageResolver messageResolver;

  /**
   * Method for catching {@link SignatureValidationException} exception, build {@link
   * ValidationErrorDto} entity and put to response
   *
   * @param ex caught exception
   * @return response entity with error
   */
  @ExceptionHandler(SignatureValidationException.class)
  public ResponseEntity<ValidationErrorDto> handleSignatureException(
      SignatureValidationException ex) {
    var errorDto = ex.getErrorDto();
    var validationError = ValidationErrorDto.builder()
        .traceId(MDC.get(BaseRestExceptionHandler.TRACE_ID_KEY))
        .code(errorDto.getCode())
        .message(errorDto.getMessage())
        .details(new ErrorsListDto(Collections.singletonList(
            new ErrorDetailDto(errorDto.getLocalizedMessage(), null, null))))
        .build();
    log.warn("Signature is not valid", ex);
    return new ResponseEntity<>(validationError, HttpStatus.UNPROCESSABLE_ENTITY);
  }

  /**
   * Catching {@link UserTaskNotExistsException} exception and return localized response about task
   * not found
   *
   * @param ex caught exception
   * @return response entity with localized response
   */
  @ExceptionHandler(UserTaskNotExistsException.class)
  public ResponseEntity<SystemErrorDto> handleUserTaskNotFoundException(
      UserTaskNotExistsException ex) {
    var localizedMessage =
        messageResolver.getMessage(UserTaskManagementMessage.USER_TASK_NOT_EXISTS, ex.getTaskId());

    var systemErrorDto = SystemErrorDto.builder()
        .traceId(ex.getCause().getTraceId())
        .message(ex.getCause().getMessage())
        .code(ex.getCause().getCode())
        .localizedMessage(localizedMessage)
        .build();

    return new ResponseEntity<>(systemErrorDto, ex.getCause().getHttpStatus());
  }

  @ExceptionHandler(UserTaskAuthorizationException.class)
  public ResponseEntity<SystemErrorDto> handleUserTaskAuthorizationException(
      UserTaskAuthorizationException ex) {
    var localizedMessage = messageResolver
        .getMessage(UserTaskManagementMessage.USER_TASK_AUTHORIZATION_ERROR, ex.getTaskId());

    var systemErrorDto = SystemErrorDto.builder()
        .traceId(MDC.get(BaseRestExceptionHandler.TRACE_ID_KEY))
        .message(ex.getMessage())
        .code(String.valueOf(HttpStatus.FORBIDDEN.value()))
        .localizedMessage(localizedMessage)
        .build();

    return new ResponseEntity<>(systemErrorDto, HttpStatus.FORBIDDEN);
  }

  /**
   * Catching {@link UserTaskNotExistsOrCompletedException} exception and return localized response
   * about task that is not exists or already completed
   *
   * @param ex caught exception
   * @return response entity with localized response
   */
  @ExceptionHandler(UserTaskNotExistsOrCompletedException.class)
  public ResponseEntity<SystemErrorDto> handleUserTaskNotExistsOrCompletedException(
      UserTaskNotExistsOrCompletedException ex) {
    var localizedMessage =
        messageResolver.getMessage(UserTaskManagementMessage.USER_TASK_NOT_EXISTS_OR_COMPLETED);

    var systemErrorDto =
        SystemErrorDto.builder()
            .traceId(ex.getCause().getTraceId())
            .message(ex.getCause().getMessage())
            .code(ex.getCause().getCode())
            .localizedMessage(localizedMessage)
            .build();

    return new ResponseEntity<>(systemErrorDto, HttpStatus.NOT_FOUND);
  }

  /**
   * Catching {@link UserTaskNotExistsOrCompletedException} exception and return localized response
   * about task that is already assigned to other user
   *
   * @param ex caught exception
   * @return response entity with localized response
   */
  @ExceptionHandler(UserTaskAlreadyAssignedException.class)
  public ResponseEntity<SystemErrorDto> handleUserTaskAlreadyAssignedException(
      UserTaskAlreadyAssignedException ex) {
    var localizedMessage =
        messageResolver.getMessage(
            UserTaskManagementMessage.USER_TASK_ALREADY_ASSIGNED, ex.getTaskName());

    var systemErrorDto =
        SystemErrorDto.builder()
            .traceId(MDC.get(BaseRestExceptionHandler.TRACE_ID_KEY))
            .message(ex.getMessage())
            .code(String.valueOf(HttpStatus.CONFLICT))
            .localizedMessage(localizedMessage)
            .build();

    return new ResponseEntity<>(systemErrorDto, HttpStatus.CONFLICT);
  }
}
