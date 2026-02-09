package ma.project.auth.services;

import lombok.RequiredArgsConstructor;
import ma.project.auth.repositories.UserRepository;
import ma.project.auth.dto.RegisterRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ma.project.auth.entities.User;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerUser(RegisterRequest registerRequest) {
        //if(userRepository.existsByEmail(email.toLowerCase())) {
        if(userRepository.existsByEmail(registerRequest.getEmail().toLowerCase())) {
            throw new RuntimeException("Cet email existe déjà.");
        }

        //version sans Dto
        /*User user = User.builder()
                .email(email)
                .username(username)
                .password(passwordEncoder.encode(password)) //comme passwordEncoder.encode("password123"); // → "$2a$10$abc123..." | y a aussi la méthode matches(rawPassword, encodedPassword)
                .enabled(true)
                .build();
         */

        //version avec Dto
        User user = User.builder()
                .email(registerRequest.getEmail().toLowerCase())
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .enabled(true)
                .build(
        );

        return userRepository.save(user);
    }

    //vérifie pas l'ancien mot de passe
    /*public void changePassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }*/

    //vérifie l'ancien mot de passe
    public void updatePassword(String email, String currentPassword, String newPassword) throws Exception {
        // Find the user by username or throw an exception if not found
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'email : " + email));

        // Check if the current password matches
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new Exception("Le mot de passe actuel est incorrect.");
        }

        // Verify that the new password is different from the current password
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new Exception("Le nouveau mot de passe doit être différent de l'ancien mot de passe.");
        }

        // Encode and set the new password, then save the user
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void deactivateUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'email : " + email));

        if (!user.isEnabled()) {
            throw new RuntimeException("Ce compte est déjà désactivé");
        }

        user.setEnabled(false);
        userRepository.save(user);
    }

    public void activateUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'email : " + email));

        if (user.isEnabled()) {
            throw new RuntimeException("Ce compte est déjà activé");
        }

        user.setEnabled(true);
        userRepository.save(user);
    }

    public ResponseEntity<String> logout() {
        return ResponseEntity.ok("Déconnexion réussie");
    }

    public boolean toggleUserStatus(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'email : " + email));

        user.setEnabled(!user.isEnabled());
        userRepository.save(user);

        return user.isEnabled(); //retourne nouvel état
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'email : " + email));
    }

    public boolean isUserEnabled(String email) {
        return userRepository.findByEmail(email)
                .map(User::isEnabled)
                .orElse(false);
    }
}