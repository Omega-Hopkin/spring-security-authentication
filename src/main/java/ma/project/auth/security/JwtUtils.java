package ma.project.auth;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.project.auth.config.JwtProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Service;
import io.jsonwebtoken.*;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;
import java.util.Date;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
@Slf4j
public class JwtUtils {
    //1er altenrative
    /*@Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long EXPIRATION_TIME;*/

    //2ème
    private final JwtProperties jwtProperties;
    private SecretKey Key;

    /*public JwtUtils() {
        //String secreteString = "eyJpc3MiOiJodHRwczovL2V4YW1wbGUuYXV0aDAuY29tLyIsImF1ZCI6Imh0dHBzOi8vYXBpLmV4YW1wbGUuY29tL2NhbGFuZGFyL3YxLyIsInN1YiI6InVzcl8xMjMiLCJpYXQiOjE0NT";
        byte[] keyBytes = Base64.getDecoder().decode(jwtProperties.getJwtSecret().getBytes(StandardCharsets.UTF_8));
        this.Key = new SecretKeySpec(keyBytes, "HmacSHA256");
    }*/

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getJwtSecret());
        //byte[] keyBytes = jwtProperties.getJwtSecret().getBytes(StandardCharsets.UTF_8);
        this.Key = Keys.hmacShaKeyFor(keyBytes);

        //jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
    }

    /*public SecretKey getSecretKey() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtProperties.getJwtSecret().getBytes(StandardCharsets.UTF_8));
        this.Key = new SecretKeySpec(keyBytes, "HmacSHA256");
        return new SecretKeySpec(this.Key.getEncoded(), "HmacSHA256");
    }*/

    public String generateToken(Authentication authentication){
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getJwtExpiration()))
                .signWith(Key)
                .compact();
    }

    public String extractUsername(String token){
        return extractClaims(token, Claims::getSubject);
    }

    private <T> T extractClaims(String token, Function<Claims, T> claimsTFunction) {
        return claimsTFunction.apply(
                Jwts.parser()
                        .verifyWith(Key)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload()
        );
    }

    public String getSubjectFromToken(String token) {
        return getClaims(token).getSubject();
    }

    /*public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        }
        catch (SecurityException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        }
        catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }*/

    //fonctionnelle, spécifique à un utilisateurr
    public boolean isValidToken(String token, UserDetails userDetails){
        try {
            final String username = extractUsername(token);
            getClaims(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        }
        catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        }
        return false;
    }

    /*public boolean isValidToken(String token, UserDetails userDetails){
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }*/

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(Key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isTokenExpired(String token){
        return extractClaims(token, Claims::getExpiration).before(new Date(System.currentTimeMillis())); //System.currentTimeMillis() ajouté

        //sinon
        /*
        private boolean isTokenExpired(String token) {
        Date expiration = Jwts.parserBuilder()
            .setSigningKey(SECRET_KEY.getBytes())
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getExpiration();
            return expiration.before(new Date());
        }
        */
    }

    //valider JwtToken
    /*public boolean validateJwtToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException e) {
            System.out.println("Invalid JWT signature: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.out.println("Invalid JWT token: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            System.out.println("JWT token is expired: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.out.println("JWT token is unsupported: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("JWT claims string is empty: " + e.getMessage());
        }
        return false;
    }*/
}
