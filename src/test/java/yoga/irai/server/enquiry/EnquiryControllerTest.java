package yoga.irai.server.enquiry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import yoga.irai.server.app.dto.AppResponseDto;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
class EnquiryControllerTest {
    @Mock
    private EnquiryService enquiryService;
    @InjectMocks
    private EnquiryController enquiryController;
    private EnquiryRequestDto  enquiryRequestDto;
    @BeforeEach
    void setUp(){
        enquiryRequestDto = EnquiryRequestDto.builder()
                .name("Hilton")
                .email("hilton.p@terv.pro")
                .message("Test")
                .build();
    }
    @Test
    void addEnquiryTest(){
        doNothing().when(enquiryService).updateEnquiry(enquiryRequestDto);
        ResponseEntity<AppResponseDto<Void>> response = enquiryController.addEnquiry(enquiryRequestDto);
        assert response.getStatusCode() == HttpStatus.OK;
        assertNotNull(response.getBody());
    }
}
