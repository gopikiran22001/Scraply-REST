package com.scraply.rest.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CookieUtil {

    @Value("${security.jwt.cookie.name:token}")
    private String cookieName;

    @Value("${security.jwt.cookie.http-only:true}")
    private boolean httpOnly;

    @Value("${security.jwt.cookie.secure:true}")
    private boolean secure;

    @Value("${security.jwt.cookie.same-site:Strict}")
    private String sameSite;

    @Value("${security.jwt.cookie.path:/}")
    private String path;

    @Value("${security.jwt.cookie.token-expiry:2592000}")
    private int tokenExpiry;


    /**
     * Creates and sets an access token cookie in the response
     */
    public void setTokenCookie(HttpServletResponse response, String token) {
        setCookie(response, cookieName, token, tokenExpiry);
    }

    /**
     * Creates and sets a cookie with secure attributes
     */
    private void setCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(httpOnly);
        cookie.setSecure(secure);
        cookie.setPath(path);
        cookie.setMaxAge(maxAge);
        cookie.setAttribute("SameSite", sameSite);
        response.addCookie(cookie);
    }

    /**
     * Retrieves the token from cookies
     */
    public Optional<String> getTokenFromCookies(HttpServletRequest request) {
        return getTokenFromCookies(request, cookieName);
    }

    /**
     * Retrieves a specific token from cookies by name
     */
    private Optional<String> getTokenFromCookies(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> cookieName.equals(cookie.getName()) && StringUtils.hasText(cookie.getValue()))
                .map(Cookie::getValue)
                .findFirst();
    }

    /**
     * Clears both access and refresh token cookies
     */
    public void clearAuthenticationCookies(HttpServletResponse response) {
        clearCookie(response, cookieName);
    }

    /**
     * Clears a specific cookie
     */
    private void clearCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setHttpOnly(httpOnly);
        cookie.setSecure(secure);
        cookie.setPath(path);
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", sameSite);
        response.addCookie(cookie);
    }
}

