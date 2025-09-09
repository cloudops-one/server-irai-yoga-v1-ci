package yoga.irai.server.authentication.repository;

import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.authentication.dto.UserStatsDto;
import yoga.irai.server.authentication.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    /**
     * Finds a user by their mobile number.
     *
     * @param userEmail
     *            - the user's email address
     *
     * @return the UserEntity if found, otherwise null
     */
    UserEntity findByUserEmail(String userEmail);

    /**
     * Finds a user by their mobile number.
     *
     * @param orgId
     *            - the organization ID to filter users by
     * @param keyword
     *            - the search keyword to filter users by first name, last name, or
     *            email
     *
     * @return the UserEntity if found, otherwise null
     */
    @Query("""
            SELECT u FROM UserEntity u WHERE (:orgIds IS NULL OR u.orgId = :orgIds) AND
                       (CASE WHEN :keyword IS NULL OR :keyword = '' THEN true ELSE(
                  LOWER(u.userFirstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(u.userLastName) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(u.userEmail) LIKE LOWER(CONCAT('%', :keyword, '%'))) END)
            """)
    Page<UserEntity> search(@Param("keyword") String keyword, @Param("orgIds") UUID orgId, Pageable pageable);

    /**
     * Checks if a user exists by their mobile number and status.
     *
     * @param userMobile
     *            - the user's mobile number
     * @param userStatus
     *            - the user's status
     *
     * @return true if the user exists, otherwise false
     */
    boolean existsByUserMobileAndUserStatus(String userMobile, AppUtils.UserStatus userStatus);

    /**
     * Checks if a user exists by their email and status.
     *
     * @param email
     *            - the user's email address
     * @param userStatus
     *            - the user's status
     * @return true if the user exists, otherwise false
     */
    boolean existsByUserEmailAndUserStatus(String email, AppUtils.UserStatus userStatus);

    /**
     * Finds a user by their user ID and status.
     *
     * @param userId
     *            - the user's ID
     * @param userStatus
     *            - the user's status
     *
     * @return the UserEntity if found, otherwise null
     */
    UserEntity getUserEntityByUserIdAndUserStatus(UUID userId, AppUtils.UserStatus userStatus);

    /**
     * Finds a user by their mobile number.
     *
     * @param userMobile
     *            - the user's mobile number
     * @return the UserEntity if found, otherwise null
     */
    UserEntity findByUserMobile(String userMobile);

    /**
     * Finds a user by their email and status.
     *
     * @param orgIds
     *            - the organization IDs to filter users by
     *
     * @return the UserEntity if found, otherwise null
     */
    @Query("""
            SELECT new yoga.irai.server.authentication.dto.UserStatsDto (
                COUNT(u),
                SUM(CASE WHEN u.userStatus = :userStatus THEN 1 ELSE 0 END),
                SUM(CASE WHEN u.userType = :userType THEN 1 ELSE 0 END),
                SUM(CASE WHEN u.userStatus = :userStatus AND u.userType = :userType THEN 1 ELSE 0 END)
            )
            FROM UserEntity u WHERE (:orgIds IS NULL OR u.orgId IN :orgIds)
            """)
    UserStatsDto getUserStats(@Param("orgIds") Set<UUID> orgIds, @Param("userStatus") AppUtils.UserStatus userStatus,
            @Param("userType") AppUtils.UserType userType);
}
