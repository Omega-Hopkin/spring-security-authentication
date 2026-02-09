package ma.project.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import ma.project.auth.entities.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    int countByEnabled(boolean enabled);

    void deleteByEmail(String email);
}
