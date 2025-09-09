package yoga.irai.server.practice.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PracticeUserRepository extends JpaRepository<PracticeUserEntity, UUID> {

    /**
     * Finds all PracticeUserEntity records by userId.
     *
     * @param practiceId
     *            the ID of the practice
     * @return a list of PracticeUserEntity records associated with the given
     *         practiceId
     */
    @Query("SELECT pu.rating FROM PracticeUserEntity pu WHERE pu.practiceId = :practiceId AND pu.rating IS NOT NULL")
    List<Float> findNonZeroRatingsByPracticeId(@Param("practiceId") UUID practiceId);

    /**
     * Finds all PracticeUserEntity records by practiceId.
     *
     * @param practiceId
     *            the ID of the practice
     * @return a list of PracticeUserEntity records associated with the given
     *         practiceId
     */
    @Query("SELECT pu FROM PracticeUserEntity pu WHERE pu.practiceId = :practiceId AND pu.userId = :userId")
    PracticeUserEntity getByPracticeUserByPracticeIdAndUserId(@Param("practiceId") UUID practiceId,
            @Param("userId") UUID userId);

    Optional<PracticeUserEntity> findByPracticeIdAndUserId(UUID practiceId, UUID userId);
}
