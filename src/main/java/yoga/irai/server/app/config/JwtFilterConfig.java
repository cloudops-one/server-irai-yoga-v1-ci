package yoga.irai.server.app.config;

import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.filter.OncePerRequestFilter;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.authentication.entity.UserEntity;
import yoga.irai.server.authentication.entity.UserPrincipalEntity;
import yoga.irai.server.authentication.service.JwtService;
import yoga.irai.server.authentication.service.UserService;

@Configuration
@RequiredArgsConstructor
public class JwtFilterConfig extends OncePerRequestFilter {

    @Value("${spring.security.oauth2.client.provider.irai-yoga-v1-service-account-client.issuer-uri}")
    private String issuerUrl;

    private final JwtDecoder jwtDecoder;
    private final JwtService jwtService;
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(AppUtils.Constants.AUTHORIZATION_HEADER);
        if (authHeader == null || !authHeader.startsWith(AppUtils.Constants.BEARER)) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = authHeader.substring(7);
        if (checkForIssuer(token, issuerUrl)) {
            handleKeycloakToken(token, request, response, filterChain);
        } else {
            handleLocalToken(token, request, response, filterChain);
        }
    }

    private void handleKeycloakToken(String token, HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        Jwt jwt = jwtDecoder.decode(token);
        if (jwt == null)
            return;
        String username = jwt.getClaimAsString("sub");
        String userEmail = jwt.getClaimAsString("email");
        if (username == null || SecurityContextHolder.getContext().getAuthentication() != null)
            return;
        UserEntity userEntity = userService.checkCredentials(userEmail);
        if (ObjectUtils.isEmpty(userEntity)) {
            userEntity = userService.createUserFromKeycloak(jwt);
        }
        setAuthenticationAndContinue(userEntity.getUserId(), request, response, filterChain);
    }

    private void handleLocalToken(String token, HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String username = jwtService.extractUsername(token);
        if (username == null || SecurityContextHolder.getContext().getAuthentication() != null)
            return;
        UserDetails userDetails = userService.loadUserByUsername(username);
        if (jwtService.validateToken(token, userDetails)) {
            setAuthenticationAndContinue(((UserPrincipalEntity) userDetails).user().getUserId(), request, response,
                    filterChain);
        }
    }

    private void setAuthenticationAndContinue(UUID username, HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        UserDetails userDetails = userService.loadUserByUsername(String.valueOf(username));
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);
        filterChain.doFilter(request, response);

    }

    public boolean checkForIssuer(String token, String issuerUrl) {
        try {
            SignedJWT parsedJwt = (SignedJWT) JWTParser.parse(token);
            String issuer = parsedJwt.getJWTClaimsSet().getIssuer();
            return issuer != null && issuer.equals(issuerUrl);
        } catch (Exception e) {
            return false;
        }
    }
}
