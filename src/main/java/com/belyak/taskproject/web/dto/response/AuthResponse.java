package com.belyak.taskproject.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Response containing the authentication token and its expiration details")
public record AuthResponse(

        @Schema(
                description = "JWT Access Token. Must be used in the 'Authorization' header with the 'Bearer' prefix for protected endpoints.",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
        String token,

        @Schema(description = "Token expiration time in milliseconds", example = "86400000")
        long expiresIn
) {
}
