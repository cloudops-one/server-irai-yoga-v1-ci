package yoga.irai.server.authentication.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yoga.irai.server.app.AppUtils.RefreshTokenStatus;
import yoga.irai.server.authentication.entity.RefreshTokenEntity;
import yoga.irai.server.authentication.entity.UserEntity;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {
    Optional<RefreshTokenEntity> findByToken(String token);

    RefreshTokenEntity findByUserAndRefreshTokenStatus(UserEntity user, RefreshTokenStatus refreshTokenStatus);

    RefreshTokenEntity findByUser_UserIdAndRefreshTokenStatus(UUID uuid, RefreshTokenStatus refreshTokenStatus);
}
