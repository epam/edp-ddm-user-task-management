package com.epam.digital.data.platform.usrtaskmgt.enums;

import com.epam.digital.data.platform.starter.localization.MessageTitle;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum that represents localization message codes for using in {@link
 * com.epam.digital.data.platform.starter.localization.MessageResolver}
 */
@Getter
@RequiredArgsConstructor
public enum UserTaskManagementMessage implements MessageTitle {

  USER_TASK_NOT_EXISTS("user-task.not-exists"),
  USER_TASK_NOT_EXISTS_OR_COMPLETED("user-task.not-exists-or-completed"),
  USER_TASK_ALREADY_ASSIGNED("user-task.already-assigned"),

  USER_TASK_AUTHORIZATION_ERROR("user-task.authorization-error");

  private final String titleKey;
}
