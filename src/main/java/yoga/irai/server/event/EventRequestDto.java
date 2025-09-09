package yoga.irai.server.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import lombok.*;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.dto.AddressDto;
import yoga.irai.server.app.dto.ContactDto;
import yoga.irai.server.app.dto.UrlDto;

@Data
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 24816724536456L;

    @NotNull(message = AppUtils.Constants.VALIDATION_FAILED_ID_BLANK)
    private UUID orgId;

    private UUID eventIconStorageId;
    private String eventIconExternalUrl;

    private UUID eventBannerStorageId;
    private String eventBannerExternalUrl;

    @NotBlank(message = AppUtils.Constants.VALIDATION_FAILED_NAME_BLANK)
    private String eventName;

    @NotBlank(message = AppUtils.Constants.VALIDATION_FAILED_DESCRIPTION_BLANK)
    private String eventDescription;

    @NotNull(message = AppUtils.Constants.VALIDATION_FAILED_DATE_TIME_BLANK)
    private ZonedDateTime eventStartDateTime;

    @NotNull(message = AppUtils.Constants.VALIDATION_FAILED_DATE_TIME_BLANK)
    private ZonedDateTime eventEndDateTime;

    @Valid
    @NotEmpty(message = AppUtils.Constants.VALIDATION_FAILED_ADDRESSES_BLANK)
    private List<AddressDto> addresses;

    @Valid
    @NotEmpty(message = AppUtils.Constants.VALIDATION_FAILED_CONTACTS_BLANK)
    private List<ContactDto> contacts;

    @Valid
    @NotEmpty(message = AppUtils.Constants.VALIDATION_FAILED_URLS_BLANK)
    private List<UrlDto> urls;
}
