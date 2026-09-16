package com.scraply.rest.utilities;

import com.scraply.rest.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public class SecurityUtil {

    public static UUID getCurrentUserId() {
    Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
        throw new IllegalStateException("No authenticated user found");
    }

    return UUID.fromString(userDetails.getUsername());
}


}