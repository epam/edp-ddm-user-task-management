package com.epam.digital.data.platform.usrtaskmgt.util;

import java.security.Principal;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class AuthUtil {

  public static String getCurrentUsername() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return Optional.ofNullable(authentication).map(Principal::getName).orElse(null);
  }
}
