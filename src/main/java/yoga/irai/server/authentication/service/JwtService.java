package yoga.irai.server.authentication.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.AppUtils.RefreshTokenStatus;
import yoga.irai.server.authentication.dto.RefreshTokenRequestDto;
import yoga.irai.server.authentication.dto.SignOutRequestDto;
import yoga.irai.server.authentication.entity.DeviceEntity;
import yoga.irai.server.authentication.entity.RefreshTokenEntity;
import yoga.irai.server.authentication.entity.UserEntity;
import yoga.irai.server.authentication.entity.UserPrincipalEntity;
import yoga.irai.server.authentication.repository.DeviceRepository;
import yoga.irai.server.authentication.repository.RefreshTokenRepository;
import yoga.irai.server.notification.NotificationService;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final DeviceRepository deviceRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final NotificationService notificationService;

    @Value("${app.jwt.private-key}")
    private String privateKey;

    @Value("${app.jwt.public-key}")
    private String publicKey;

    /**
     * Validates the JWT token.
     *
     * @param token
     *            the JWT token to validate
     * @param userDetails
     *            the user details to validate against
     * @return true if the token is valid, false otherwise
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        String userId = ((UserPrincipalEntity) userDetails).user().getUserId().toString();
        return (username.equals(userId) && !isTokenExpired(token));
    }

    /**
     * Extracts the username from the JWT token.
     *
     * @param token
     *            the JWT token
     * @return the username extracted from the token
     */
    public String extractUsername(String token) {
        return Jwts.parserBuilder().setSigningKey(getPublicKey()).build().parseClaimsJws(token).getBody().getSubject();
    }

    /**
     * Gets the private key used for signing JWT tokens.
     *
     * @return the private key used for signing JWT tokens.
     */
    private PrivateKey getPrivateKey() {
        try {
            byte[] keyBytes = AppUtils.decodeBase64ToByteArray(privateKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance(AppUtils.Constants.RSA).generatePrivate(keySpec);
        } catch (Exception e) {
            throw AppUtils.Messages.ERROR_LOADING_PRIVATE_KEY.getException(e.getMessage());
        }
    }

    /**
     * Gets the public key used for signing JWT tokens.
     *
     * @return the public key used for signing JWT tokens.
     */
    private PublicKey getPublicKey() {
        try {
            byte[] keyBytes = AppUtils.decodeBase64ToByteArray(publicKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance(AppUtils.Constants.RSA).generatePublic(keySpec);
        } catch (Exception e) {
            throw AppUtils.Messages.ERROR_LOADING_PUBLIC_KEY.getException(e.getMessage());
        }
    }

    /**
     * Generates an access token for the given user details.
     *
     * @param userDetails
     *            the user details for which to generate the access token
     * @return the generated access token
     */
    public String generateAccessToken(UserDetails userDetails) {
        return Jwts.builder().setSubject(((UserPrincipalEntity) userDetails).user().getUserId().toString())
                .claim("firstName", ((UserPrincipalEntity) userDetails).user().getUserFirstName())
                .claim("lastName", ((UserPrincipalEntity) userDetails).user().getUserLastName())
                .claim("organizationId", "f3435ed6-a81f-4af5-bb09-9ec2cb624c61").claim("level", "SYSTEM")
                .claim("role", ((UserPrincipalEntity) userDetails).user().getUserType()).setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(1, ChronoUnit.DAYS)))
                .signWith(getPrivateKey(), SignatureAlgorithm.RS256).compact();
    }

    /**
     * Creates and saves a refresh token for the given user and device.
     *
     * @param user
     *            the user for whom to create the refresh token
     * @param userDetails
     *            the user details for the user
     * @param deviceEntity
     *            the device audit associated with the refresh token
     * @return the created and saved refresh token audit
     */
    public RefreshTokenEntity createAndSaveRefreshToken(UserEntity user, UserDetails userDetails,
            DeviceEntity deviceEntity) {

        RefreshTokenEntity existingToken = refreshTokenRepository.findByUserAndRefreshTokenStatus(user,
                RefreshTokenStatus.ACTIVE);

        if (existingToken != null && existingToken.getRefreshTokenStatus().equals(RefreshTokenStatus.ACTIVE)
                && existingToken.getExpiresAt().isAfter(ZonedDateTime.now())) {
            return existingToken;
        } else {
            if (existingToken != null) {
                refreshTokenRepository.delete(existingToken);
            }

            // Set expiration 30 Days from now
            Instant now = Instant.now();
            Instant expiryInstant = now.plus(30, ChronoUnit.DAYS);
            Date jwtExpiryDate = Date.from(expiryInstant);
            ZonedDateTime dbExpiry = expiryInstant.atZone(ZoneId.systemDefault());

            String refreshToken = Jwts.builder()
                    .setSubject(((UserPrincipalEntity) userDetails).user().getUserId().toString())
                    .setIssuedAt(new Date()).setExpiration(jwtExpiryDate)
                    .signWith(getPrivateKey(), SignatureAlgorithm.RS256).compact();

            RefreshTokenEntity token = RefreshTokenEntity.builder().user(user).deviceEntity(deviceEntity)
                    .token(refreshToken).expiresAt(dbExpiry).refreshTokenStatus(RefreshTokenStatus.ACTIVE)
                    .createdAt(ZonedDateTime.now()).build();

            refreshTokenRepository.save(token);

            return token;
        }
    }

    /**
     * Checks if the given JWT token is expired.
     *
     * @param token
     *            the JWT token to check
     * @return true if the token is expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = Jwts.parserBuilder().setSigningKey(getPublicKey()).build().parseClaimsJws(token).getBody()
                    .getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Checks if the given refresh token is valid.
     *
     * @param refreshTokenRequestDto
     *            the DTO containing the refresh token to validate
     * @return true if the refresh token is valid, false otherwise
     */
    public boolean isRefreshTokenValid(RefreshTokenRequestDto refreshTokenRequestDto) {
        String token = refreshTokenRequestDto.getRefreshToken();
        Optional<RefreshTokenEntity> optional = refreshTokenRepository.findByToken(token);
        if (optional.isEmpty())
            return false;
        RefreshTokenEntity refreshTokenEntity = optional.get();
        return (refreshTokenEntity.getRefreshTokenStatus().equals(RefreshTokenStatus.ACTIVE))
                && refreshTokenEntity.getExpiresAt().isAfter(ZonedDateTime.now());
    }

    /**
     * Revokes the refresh token for the given sign-out request.
     *
     * @param signOutRequestDto
     *            the DTO containing the user ID for which to revoke the refresh
     *            token
     */
    public void revokeRefreshToken(SignOutRequestDto signOutRequestDto) {
        Optional<RefreshTokenEntity> optional = Optional
                .ofNullable(refreshTokenRepository.findByUser_UserIdAndRefreshTokenStatus(
                        UUID.fromString(signOutRequestDto.getUserId()), RefreshTokenStatus.ACTIVE));
        if (optional.isEmpty()) {
            throw AppUtils.Messages.REFRESH_TOKEN_NOT_FOUND.getException();
        }
        RefreshTokenEntity refreshTokenEntity = optional.get();
        DeviceEntity deviceEntity = refreshTokenEntity.getDeviceEntity();
        refreshTokenRepository.delete(refreshTokenEntity);
        deviceRepository.delete(deviceEntity);
    }
}
