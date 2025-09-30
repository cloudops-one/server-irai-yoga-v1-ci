package yoga.irai.server.organization;


import org.junit.jupiter.api.Test;
import yoga.irai.server.app.AppUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrganizationEntityTest {
    @Test
    void onCreateTest(){
        OrganizationEntity organizationEntity = new OrganizationEntity();
        organizationEntity.onCreate();
        assertEquals(AppUtils.OrganizationStatus.INACTIVE , organizationEntity.getOrgStatus());
    }
}
