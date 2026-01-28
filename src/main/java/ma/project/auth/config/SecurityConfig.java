package ma.project.auth.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.project.auth.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    //private final CustomUserDetailsService customUserDetailsService;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        //hashage BCrypt pour la sécurité des credentials
        return new BCryptPasswordEncoder(10);
    }

    //gère l'authentification, lie automatiquement CustomUserDetailsService et le PasswordEncoder
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable) // Pour le moment (à activer en prod)

                //configuration des routes
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/home", "/api/auth/enable-account").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                )

                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"message\": \"Déconnexion réussie\"}");
                        })
                        .permitAll()
                )

                //désactiver le form login par défaut de Spring
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                //gestion de session
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // Crée une session si nécessaire
                );

        //.authenticationProvider(authenticationProvider());

        return http.build();
    }

    /**
    - déprécié avec Spring Security 6.x

    //pour l'AuthenticationProvider : connecte UserDetailsService et PasswordEncoder
    /*@Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);

        provider.setHideUserNotFoundExceptions(false); //sécurité
        return provider;
    }*/

    /**
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                //.csrf(csrf -> csrf.disable()) // <- 7it warning : Lambda can be replaced with method reference
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/home").permitAll()
                        .anyRequest().authenticated()
                        //.and().cors()
                )
                // Utilise userDetailsService() et passwordEncoder() directement
                //.userDetailsService(customUserDetailsService);
                .authenticationProvider(authenticationProvider())
                .formLogin(AbstractHttpConfigurer::disable)   // <-- important
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }*/

    /*
    ### `PasswordEncoder`
    - Utilise **BCrypt** (algorithme de hashing sécurisé)
    - Transformation : `"password123"` → `"$2a$10$N9qo8uLO..."`
    - **Impossible à déchiffrer** (hash unidirectionnel)

    ### `DaoAuthenticationProvider`
    - **DAO** = Data Access Object
    - Combine `UserDetailsService` + `PasswordEncoder`
    - Flow : charge user → compare les mots de passe hashés

    ### `AuthenticationManager`
    - Chef d'orchestre de l'authentification
    - Délègue au `DaoAuthenticationProvider`

    ### `SecurityFilterChain`
    - **Chaîne de filtres** qui interceptent les requêtes HTTP
    - `.requestMatchers(...).permitAll()` : routes accessibles sans login
    - `.anyRequest().authenticated()` : le reste nécessite une authentification
    - `.csrf().di
     */
}
