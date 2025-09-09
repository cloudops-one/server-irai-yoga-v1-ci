package yoga.irai.server.authentication.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yoga.irai.server.authentication.entity.DeviceEntity;
import yoga.irai.server.authentication.entity.UserEntity;

@Repository
public interface DeviceRepository extends JpaRepository<DeviceEntity, UUID> {
    Optional<DeviceEntity> findByDeviceCodeAndUser(String deviceCode, UserEntity user);
}
