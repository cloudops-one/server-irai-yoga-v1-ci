package yoga.irai.server.authentication.entity;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import yoga.irai.server.app.AppUtils;

import java.util.Collection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserPrincipalEntityTest {
    @Test
    void getAuthoritiesTest() {
        UserEntity userEntity = new UserEntity();
        userEntity.setUserType(AppUtils.UserType.KEYCLOAK_USER);
        UserPrincipalEntity userPrincipalEntity = new UserPrincipalEntity(userEntity);
        Collection<? extends GrantedAuthority> authorities = userPrincipalEntity.getAuthorities();
        assertEquals(1, authorities.size());
        assertTrue(authorities.iterator().next().getAuthority().contains("KEYCLOAK_USER"));
    }
    @Test
    void getPasswordTest() {
        UserEntity userEntity = new UserEntity();
        userEntity.setPasswordHash("Hilton paul");
        UserPrincipalEntity userPrincipalEntity = new UserPrincipalEntity(userEntity);
        assertEquals("Hilton paul", userPrincipalEntity.getPassword());
    }
    @Test
    void getUsernameTest() {
        UserEntity userEntity = new UserEntity();
        userEntity.setUserEmail("hilton.p@terv.pro");
        UserPrincipalEntity userPrincipalEntity = new UserPrincipalEntity(userEntity);
        assertEquals("hilton.p@terv.pro" , userPrincipalEntity.getUsername());
    }
    @Test
    void getOrgIdTest() {
        UUID orgId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity();
        userEntity.setOrgId(orgId);
        UserPrincipalEntity userPrincipalEntity = new UserPrincipalEntity(userEntity);
        assertEquals(orgId, userPrincipalEntity.getOrgId());
    }
}
