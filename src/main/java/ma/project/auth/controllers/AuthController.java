package ma.project.auth.controllers;

import lombok.RequiredArgsConstructor;
import ma.project.auth.security.JwtUtils;
import ma.project.auth.dto.AuthResponse;
import ma.project.auth.dto.ChangePasswordRequest;
import ma.project.auth.dto.LoginRequest;
import ma.project.auth.dto.RegisterRequest;
import ma.project.auth.entities.User;
import ma.project.auth.security.CustomUserDetails;
import ma.project.auth.security.CustomUserDetailsService;
import ma.project.auth.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
//import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    //private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtUtils jwtUtils;

    @GetMapping("/home")
    public String home() {
        return "Welcome to the Auth Service!";
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            //Without Dto
            /*User user = userService.registerUser(
                    request.getEmail(),
                    request.getPassword(),
                    request.getUsername()
            );*/

            //With Dto
            User user = userService.registerUser(request);

            //return ResponseEntity.ok(user);
            return ResponseEntity.ok(new AuthResponse("Inscription réussie pour l'email : ", user.getEmail()));
            /*return ResponseEntity.ok(AuthResponse.builder()
                .message("Inscription réussie"))
                .email(user.getEmail())
                .build();*/
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        /*
        //auto-login après register
        User user = userService.registerUser(request);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());

        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
            );

        String jwtToken = jwtUtils.generateToken(auth);

        return ResponseEntity.ok(
            new AuthResponse("Inscription réussie", user.getEmail(), jwtToken)
        );
        */
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            String jwtToken = jwtUtils.generateToken(authentication);

            //stocke l'authentication dans le SecurityContext
            //SecurityContextHolder.getContext().setAuthentication(authentication);

            //return ResponseEntity.ok(new AuthResponse("Connexion pour l'email : " + request.getEmail(), jwtToken));
            return ResponseEntity.ok(AuthResponse.builder()
                    .message("Connexion réussié")
                    .email(request.getEmail())
                    .token(jwtToken)
                    .build());
        }
        catch (DisabledException e) {
            // Renvoie un 403 ou 401 explicite au lieu de 500
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new AuthResponse("Le compte est désactivé. Veuillez l'activer via /enable-account"));
        }
        catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse("Email ou mot de passe incorrect"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse("Erreur lors de la connexion : " + e.getMessage()));
        }
    }

    /*@GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        //récupère l'utilisateur connecté depuis le SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            //return ResponseEntity.ok(authentication.getPrincipal() + authentication.getName());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non authentifié.");
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        return ResponseEntity.ok(Map.of(
                "enabled", userDetails.isEnabled(),
                "email", userDetails.getUsername(),
                "authorities", userDetails.getAuthorities()
        ));
    }*/
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        try {
            String email = getAuthenticatedUserEmail();
            CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            return ResponseEntity.ok(Map.of(
                    "email", email,
                    "enabled", userDetails.isEnabled(),
                    "authorities", userDetails.getAuthorities()
            ));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }

    //pas logique
    /*@PostMapping("/logout")
    public ResponseEntity<?> logout() {
        SecurityContextHolder.clearContext();

        if (!isUserAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non authentifié.");
        }

        return ResponseEntity.ok("Déconnexion réussie");
    }*/

    //Faut envisager cette solution au cas où j'utilise pas le cas de gestion de déconnexion en SecurityConfig
    /*@PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        // 1. Vérifier d'abord si l'utilisateur est là
        if (!isUserAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Erreur : Aucun utilisateur connecté.");
        }

        // 2. Nettoyer le contexte
        SecurityContextHolder.clearContext();

        // 3. Invalider la session manuellement (crucial pour le JSESSIONID)
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        return ResponseEntity.ok("Déconnexion réussie");
    }*/


    //Logout spécifique pas besoin si c'est géré dans le SecurityConfig
    /*
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }

        return ResponseEntity.ok("Déconnexion réussie");
    }
    */

    //Logout Handler est utilie qu'en besoin spéciale
        //- log d'audit
        //- blacklist JWT
        //- notifier service externe
        //- ...

    @PostMapping("/update-password")
    public ResponseEntity<?> updatePassword(@RequestBody ChangePasswordRequest request) {
        try {
            String email = getAuthenticatedUserEmail();

            userService.updatePassword(email, request.getCurrentPassword(), request.getNewPassword());
            return ResponseEntity.ok("Mot de passe mis à jour avec succès.");
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la mise à jour du mot de passe: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Map.of(
                "message", "Déconnexion réussie. Supprimez le token côté client."
        ));
    }

    @PostMapping("/disable-account")
    public ResponseEntity<?> disableAccount() {
        try {
            String email = getAuthenticatedUserEmail();
            userService.deactivateUser(email);
            SecurityContextHolder.clearContext();
            return ResponseEntity.ok("Compte désactivé avec succès.");
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        }
    }

    @PostMapping("/enable-account")
    public ResponseEntity<?> enableAccount(@RequestParam String email) {
        //Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        /*if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non authentifié.");
        }*/

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body("L'email est requis.");
        }

        try {
            //String email = request.get("email");
            userService.activateUser(email);
            return ResponseEntity.ok("Compte activé avec succès pour " + email + ".");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erreur lors de l'activation du compte: " + e.getMessage());
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> getAccountStatus() {
        try {
            String email = getAuthenticatedUserEmail();
            boolean isEnabled = userService.isUserEnabled(email);

            return ResponseEntity.ok(Map.of(
                    "email", email,
                    "enabled", isEnabled,
                    "status", isEnabled ? "Compte actif" : "Compte désactivé"
            ));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }

    /* Helpers */
    private boolean isUserAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null &&
                authentication.isAuthenticated() &&
                !(authentication instanceof AnonymousAuthenticationToken);
    }

    private String getAuthenticatedUserEmail() {
        if (!isUserAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non authentifié");
        }
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    /*
    //Route pour changer le mot de passe
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        try {
            userService.changePassword(request.getEmail(), request.getNewPassword());
            return ResponseEntity.ok("Mot de passe changé avec succès.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors du changement de mot de passe: " + e.getMessage());
        }
    }

    // Route pour désactiver un utilisateur
    @PostMapping("/deactivate")
    public ResponseEntity<?> deactivateUser(@RequestBody DeactivateUserRequest request) {
        try {
            userService.deactivateUser(request.getEmail());
            return ResponseEntity.ok("Utilisateur désactivé avec succès.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la désactivation de l'utilisateur: " + e.getMessage());
        }
    }

    ////DTOs
    @Data
    public class ChangePasswordRequest {
        private String email;
        private String newPassword;
    }

    @Data
    public class DeactivateUserRequest {
        private String email;
    }*/
}
