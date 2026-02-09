package ma.project.auth.security;

import lombok.RequiredArgsConstructor;
import ma.project.auth.repositories.UserRepository;
import ma.project.auth.entities.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("L'utilisateur avec l'email \"" + email + "\" n'a pas été trouvé."));

        /*return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(), // Mot de passe déjà hashé en BD
                user.isEnabled(),   // Compte actif ?
                true,               // Compte non expiré
                true,               // Credentials non expirés
                true,               // Compte non verrouillé
                getAuthorities()    // Rôles/permissions
        );*/

        return new CustomUserDetails(user);
    }

    private Collection<? extends GrantedAuthority> getAuthorities() {
        // Pour l'instant, un seul rôle
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }
}
