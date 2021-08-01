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
