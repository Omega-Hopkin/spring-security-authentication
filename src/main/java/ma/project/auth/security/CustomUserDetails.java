package ma.project.auth.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ma.project.auth.entities.User;
import java.util.Collection;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {
    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Pour l'instant, un seul rôle
        //return List.of(new SimpleGrantedAuthority(user.getRole())); // "ROLE_USER" ou "ROLE_ADMIN", etc.
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())); // si tu veux préfixer avec "ROLE_"

        // Plus tard, tu pourras faire :
        // return user.getRoles().stream()
        //     .map(role -> new SimpleGrantedAuthority(role.getName()))
        //     .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        //return user.isEnabled();
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }

    //autres
    public Long getId() {
        return user.getId();
    }

    /*public String getFullName() {
        return user.getFullName();
    }*/
}
