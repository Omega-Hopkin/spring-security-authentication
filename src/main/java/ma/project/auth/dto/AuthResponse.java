package ma.project.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor @AllArgsConstructor
public class AuthResponse {
    private String message;
    private String email;
    private String token;

    @Builder.Default
    private String tokenType = "Bearer";

    public AuthResponse(String message) {
        this.message = message;
    }

    public AuthResponse(String message, String email) {
        this.message = message;
        this.email = email;
    }

    public AuthResponse(String message, String email, String token) {
        this.message = message;
        this.email = email;
        this.token = token;
        this.tokenType = "Bearer";
    }
}