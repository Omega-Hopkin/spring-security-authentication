package ma.project.auth.dto;

import lombok.Data;

@Data
public class ChangePasswordRequest {
    private String currentPassword; //ancien mot de passe
    private String newPassword;     //nouveau mot de passe
}
