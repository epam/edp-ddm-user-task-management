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
