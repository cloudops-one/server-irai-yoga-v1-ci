package yoga.irai.server.setting;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettingRepository extends JpaRepository<SettingEntity, UUID> {

    /**
     * Finds a setting by its name.
     *
     * @param settingName
     *            the name of the setting to find
     * @return an Optional containing the SettingEntity if found, or empty if not
     *         found
     */
    Optional<SettingEntity> findBySettingName(String settingName);
}
