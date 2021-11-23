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

package com.epam.digital.data.platform.usrtaskmgt.mapper;

import static java.util.Objects.isNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * The class represents a mapper for converting date by specified format.
 */
public final class MapperUtils {

  /**
   * Method for converting date by specified format
   *
   * @param string input date
   * @return converted date or null if input parameter is null
   */
  @SuppressWarnings("unused")
  public LocalDateTime toLocalDateTime(String string) {
    if (isNull(string)) {
      return null;
    }
    return LocalDateTime.parse(string, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
  }
}
