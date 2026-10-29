package com.gana.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * Writes ErrorResponse-shaped JSON from the security filter chain, where
 * @RestControllerAdvice does not apply. Messages are fixed strings, so no
 * JSON escaping is needed.
 */
final class SecurityErrorWriter {

    private SecurityErrorWriter() {
    }

    static void write(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(
                "{\"message\":\"" + message + "\",\"status\":" + status.value()
                        + ",\"timestamp\":\"" + LocalDateTime.now() + "\"}"
        );
    }
}
