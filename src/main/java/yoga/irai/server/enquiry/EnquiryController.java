package yoga.irai.server.enquiry;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.dto.AppResponseDto;

@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/enquiry")
@Tag(name = "Enquiry", description = "Enquiry related operations")
public class EnquiryController {

    private final EnquiryService enquiryService;

    /**
     * Adds a new enquiry.
     *
     * @param enquiryRequestDto
     *            the enquiry request data transfer object
     * @return a response audit containing the result of the operation
     */
    @PostMapping
    @Operation(summary = "Add Enquiry", description = "Adds a new enquiry to the system.")
    public ResponseEntity<AppResponseDto<Void>> addEnquiry(@Valid @RequestBody EnquiryRequestDto enquiryRequestDto) {
        AppResponseDto.AppResponseDtoBuilder<Void> builder = AppResponseDto.builder();
        enquiryService.updateEnquiry(enquiryRequestDto);
        return ResponseEntity.ok(builder.message(AppUtils.Messages.UPDATE_SUCCESS.getMessage()).build());
    }
}
