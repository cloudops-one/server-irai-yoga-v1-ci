package yoga.irai.server.organization;

import java.util.UUID;

public interface OrganizationDropdownDto {
    UUID getOrgId();
    String getOrgName();
    String getOrgRegistrationNumber();
    String getOrgIconStorageId();
}
