package yoga.irai.server.provider;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OtpRepository extends JpaRepository<OtpEntity, UUID> {

    /**
     * Finds an OtpEntity by its userId.
     *
     * @param key
     *            the UUID of the user
     * @return an Optional containing the OtpEntity if found, or empty if not found
     */
    Optional<OtpEntity> findByUserId(UUID key);

    /**
     * Finds all OtpEntities associated with a specific userId.
     *
     * @param userId
     *            the UUID of the user
     * @return a List of OtpEntity objects associated with the userId
     */
    List<OtpEntity> findAllByUserId(UUID userId);
}
