/*package ma.project.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.project.auth.config.JwtProperties;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.beans.factory.annotation.Value;
import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthProvider {
    //1er alternative : @Value + final
    //private final String jwtSecret;
    //private final long jwtExpirationInMs;

    //2ème alernative :
    private final JwtProperties jwtProperties;
    private SecretKey signingKey;

    //sois opter pour @Value sans final sois ajouter un contructeur
    //public JwtAuthProvider(@Value("${jwt.secret}") String jwtSecret, @Value("${jwt.expiration}") long jwtExpirationInMs) {
    //    this.jwtSecret = jwtSecret;
    //    this.jwtExpirationInMs = jwtExpirationInMs;
    //}

    @PostConstruct
    public void init() {
        //JJWT 0.12+ nécessite une SecretKey sécurisée, on décode la clé Base64 définie dans yml
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getJwtSecret());
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);

        //jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
    }

    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Date now = new Date();

        //Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + jwtProperties.getJwtExpiration()))
                .signWith(signingKey)
                .compact();
    }

    public String generateTokenFromUserId(Long userId) {
        Instant expiryDate = Instant.now().plusMillis(jwtProperties.getJwtExpiration());
        return Jwts.builder()
                .subject(Long.toString(userId))
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(expiryDate))
                .signWith(SignatureAlgorithm.HS512, jwtProperties.getJwtSecret())
                .compact();
    }

//    public Long getUserIdFromJWT(String token) {
//        Claims claims = Jwts.parser()
//                .verifyWith(jwtSecret)
//                .parseSignedClaims(token)
//                .getBody();
//
//        return Long.parseLong(claims.getSubject());
//    }
//
//    public Date getTokenExpiration(String token) {
//        return getClaims(token).getExpiration();
//    }
//
//    public Long getUserIdFromToken(String token) {
//        return Long.parseLong(getClaims(token).getSubject());
//    }


}*/
