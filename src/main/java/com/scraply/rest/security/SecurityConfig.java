package com.scraply.rest.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;
    private final RateLimitFilter rateLimitFilter;

    @Value("${webapp.origin:http://localhost:5173}")
    private String webappOrigin;

    @Value("${server.servlet.session.cookie.name:JSESSIONID}")
    private String cookieName;

    @Value("${server.servlet.session.cookie.same-site:Lax}")
    private String sameSite;

    @Value("${server.servlet.session.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${scraply.session.encryption-key:scraply-super-secret-key-for-session-encryption-32bytes}")
    private String sessionEncryptionKey;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        try {
            http.csrf(AbstractHttpConfigurer::disable)
                    .cors(Customizer.withDefaults())
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                    .securityContext(context -> context.securityContextRepository(securityContextRepository()))
                    .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                    .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint).accessDeniedHandler(accessDeniedHandler))
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(
                                    "/auth/login",
                                    "/auth/register"
                            ).permitAll()
                            .anyRequest().authenticated());
            return http.build();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to build security filter chain", ex);
        }
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = Arrays.stream(webappOrigin.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer defaultSerializer = new DefaultCookieSerializer();
        defaultSerializer.setCookieName(cookieName);
        defaultSerializer.setSameSite(sameSite);
        defaultSerializer.setUseSecureCookie(cookieSecure);
        defaultSerializer.setUseHttpOnlyCookie(true);
        defaultSerializer.setCookiePath("/");
        return new EncryptedCookieSerializer(defaultSerializer, sessionEncryptionKey);
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new DelegatingSecurityContextRepository(
                new RequestAttributeSecurityContextRepository(),
                new HttpSessionSecurityContextRepository()
        );
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
