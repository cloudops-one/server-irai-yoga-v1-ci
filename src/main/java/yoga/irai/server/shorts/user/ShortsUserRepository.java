package yoga.irai.server.shorts.user;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import yoga.irai.server.app.AppUtils;

public interface ShortsUserRepository extends JpaRepository<yoga.irai.server.shorts.user.ShortsUserEntity, UUID> {

    ShortsUserEntity findShortsUserEntityByShortsIdAndUserId(UUID shortsId, UUID userId);

    List<ShortsUserEntity> getShortsIdsByUserIdAndShortsUserStatusIn(UUID principalUserId, List<AppUtils.ShortsUserStatus> shortsUserStatus);
    
}
