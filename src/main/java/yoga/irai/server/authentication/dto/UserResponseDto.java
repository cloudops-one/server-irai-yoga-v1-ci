package yoga.irai.server.authentication.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.*;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.dto.AddressDto;

@Data
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class UserResponseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -864910645837240860L;

    private UUID userId;
    private UUID orgId;
    private UUID userIconStorageId;
    private String userIconStorageUrl;
    private String orgIconStorageUrl;
    private AppUtils.UserType userType;
    private AppUtils.UserStatus userStatus;
    private String userFirstName;
    private String userLastName;
    private String userEmail;
    private String userMobile;
    private boolean isEmailVerified;
    private boolean isMobileVerified;
    private AppUtils.Gender gender;
    private String orgName;
    private List<AddressDto> addresses;
    private Date dateOfBirth;
    private AppUtils.BloodGroup bloodGroup;
    private String userAoi;
    private UUID createdBy;
    private String createdByName;
    private ZonedDateTime createdAt;
    private UUID updatedBy;
    private String updatedByName;
    private ZonedDateTime updatedAt;
    private ZonedDateTime lastLoginAt;
}
