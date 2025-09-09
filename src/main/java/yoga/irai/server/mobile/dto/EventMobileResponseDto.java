package yoga.irai.server.mobile.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import lombok.*;
import yoga.irai.server.app.dto.AddressDto;
import yoga.irai.server.app.dto.ContactDto;
import yoga.irai.server.app.dto.UrlDto;

@Data
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EventMobileResponseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 232364645425652L;

    private UUID eventId;
    private String eventIconStorageUrl;
    private String eventIconExternalUrl;
    private String eventBannerStorageUrl;
    private String eventBannerExternalUrl;
    private String eventName;
    private String eventDescription;
    private ZonedDateTime eventStartDateTime;
    private ZonedDateTime eventEndDateTime;
    private List<AddressDto> addresses;
    private List<ContactDto> contacts;
    private List<UrlDto> urls;
}
