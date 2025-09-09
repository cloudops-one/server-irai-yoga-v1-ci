package yoga.irai.server.program.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgramUserRepository extends JpaRepository<ProgramUserEntity, UUID> {
    ProgramUserEntity getByProgramIdAndUserId(UUID programId, UUID userId);

    @Query("SELECT pu.rating FROM ProgramUserEntity pu WHERE pu.programId = :programId AND pu.rating IS NOT NULL")
    List<Float> findNonZeroRatingsByProgramId(@Param("programId") UUID programId);

    Optional<ProgramUserEntity> findByProgramIdAndUserId(UUID programId, UUID userId);
}
