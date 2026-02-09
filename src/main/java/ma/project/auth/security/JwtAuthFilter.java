package ma.project.auth.security;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
//import static org.aspectj.weaver.tools.cache.SimpleCacheFactory.path;

@Component //= équivalent .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class) en SecurityConfig
@AllArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private JwtUtils jwtUtils;
    private CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String jwtToken;
        final String userEmail;

        //if there is no header, it continues the filter without trying extracting the token, otherwise it continue without authenticating if the header doesn't start with "Bearer"
        final String authHeader = request.getHeader("Authorization");
        if(authHeader == null || authHeader.isBlank() || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request, response);
            return;
        }

        /*if(authHeader == null || authHeader.isBlank()){
            filterChain.doFilter(request, response);
            return;
        }*/

        /*String path = request.getServletPath();
        if(path.startsWith("/api/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }*/

        jwtToken = authHeader.substring(7);
        userEmail = jwtUtils.extractUsername(jwtToken);

        if(userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null){
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(userEmail);
            if (jwtUtils.isValidToken(jwtToken, userDetails)){
                SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
                UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                securityContext.setAuthentication(token);
                SecurityContextHolder.setContext(securityContext);
            }
        }
        filterChain.doFilter(request, response);
    }

    /*private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(tokenRequestHeader);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(tokenRequestHeaderPrefix)) {
            log.info("Extracted Token: " + bearerToken);
            return bearerToken.replace(tokenRequestHeaderPrefix, "");
        }
        return null;
    }*/
}
