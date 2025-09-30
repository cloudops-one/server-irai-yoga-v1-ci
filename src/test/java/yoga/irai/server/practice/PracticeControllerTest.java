package yoga.irai.server.practice;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.dto.AppResponseDto;
import yoga.irai.server.app.dto.TotalDto;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class PracticeControllerTest {
    @Mock
    PracticeService practiceService;

    @InjectMocks
    PracticeController practiceController;

    private UUID practiceId;
    private UUID practiceCategoryId;
    private PracticeEntity practiceEntity;
    private PracticeRequestDto practiceRequestDto;

    @BeforeEach
    void setUp() {
        practiceId = UUID.randomUUID();
        practiceCategoryId = UUID.randomUUID();
        practiceEntity = PracticeEntity.builder()
                .practiceId(practiceId).orgId(UUID.randomUUID()).practiceName("Test Practice").practiceDescription("Test Practice Description")
                .practiceCategoryId(practiceCategoryId).practiceIconStorageId(UUID.randomUUID())
                .practiceBannerStorageId(UUID.randomUUID()).practiceStorageId(UUID.randomUUID())
                .duration(1000L).tags("[\"tag1\",\"tag2\"]").rating(5F).ratingCount(1000L)
                .build();
        practiceRequestDto = PracticeRequestDto.builder()
                .practiceName(practiceEntity.getPracticeName()).practiceDescription(practiceEntity.getPracticeDescription())
                .practiceStorageId(practiceEntity.getPracticeStorageId()).practiceIconStorageId(practiceEntity.getPracticeIconStorageId())
                .practiceBannerStorageId(practiceEntity.getPracticeBannerStorageId()).duration(practiceEntity.getDuration())
                .tags(Set.of("tag1", "tag2")).practiceCategoryId(practiceEntity.getPracticeCategoryId()).build();
    }

    @Test
    void testAddPractice() {
        when(practiceService.addPractice(any(PracticeRequestDto.class))).thenReturn(practiceEntity);
        when(practiceService.getPracticeResponseDto(any(PracticeEntity.class))).thenReturn(PracticeResponseDto.builder()
                .practiceId(practiceId).practiceName(practiceEntity.getPracticeName()).practiceDescription(practiceEntity.getPracticeDescription())
                .practiceStatus(practiceEntity.getPracticeStatus()).practiceIconStorageId(practiceEntity.getPracticeIconStorageId())
                .practiceBannerStorageId(practiceEntity.getPracticeBannerStorageId())
                .tags(AppUtils.readValue(practiceEntity.getTags(), new TypeReference<>() {
                }))
                .build());
        ResponseEntity<AppResponseDto<PracticeResponseDto>> response = practiceController.addPractice(practiceRequestDto);
        verify(practiceService, times(1)).addPractice(any(PracticeRequestDto.class));
        assert response.getStatusCode() == HttpStatus.OK;
    }

    @Test
    void testUpdatePractice() {
        when(practiceService.updatePractice(any(UUID.class), any(PracticeRequestDto.class))).thenReturn(practiceEntity);
        when(practiceService.getPracticeResponseDto(any(PracticeEntity.class))).thenReturn(PracticeResponseDto.builder()
                .practiceId(practiceId).practiceName(practiceEntity.getPracticeName()).practiceDescription(practiceEntity.getPracticeDescription())
                .practiceStatus(practiceEntity.getPracticeStatus()).practiceIconStorageId(practiceEntity.getPracticeIconStorageId())
                .practiceBannerStorageId(practiceEntity.getPracticeBannerStorageId())
                .tags(AppUtils.readValue(practiceEntity.getTags(), new TypeReference<>() {
                }))
                .build());
        ResponseEntity<AppResponseDto<PracticeResponseDto>> response = practiceController.updatePractice(practiceId, practiceRequestDto);
        verify(practiceService, times(1)).updatePractice(any(UUID.class), any(PracticeRequestDto.class));
        assert response.getStatusCode() == HttpStatus.OK;
    }

    @Test
    void testGetPoemById() {
        when(practiceService.getPracticeById(any(UUID.class))).thenReturn(practiceEntity);
        when(practiceService.getPracticeResponseDto(any(PracticeEntity.class))).thenReturn(PracticeResponseDto.builder()
                .practiceId(practiceId).practiceName(practiceEntity.getPracticeName()).practiceDescription(practiceEntity.getPracticeDescription())
                .practiceStatus(practiceEntity.getPracticeStatus()).practiceIconStorageId(practiceEntity.getPracticeIconStorageId())
                .practiceBannerStorageId(practiceEntity.getPracticeBannerStorageId())
                .tags(AppUtils.readValue(practiceEntity.getTags(), new TypeReference<>() {
                }))
                .build());
        ResponseEntity<AppResponseDto<PracticeResponseDto>> response = practiceController.getPractice(practiceId);
        verify(practiceService, times(1)).getPracticeById(any(UUID.class));
        assert response.getStatusCode() == HttpStatus.OK;
    }

    @Test
    void testSearchPractice() {
        Page<PracticeEntity> practicePage = new PageImpl<>(List.of(practiceEntity));
        when(practiceService.getPractices(anyInt(), anyInt(), anyString(), any(), anyString(),any(UUID.class))).thenReturn(practicePage);
        when(practiceService.toPracticeResponseDto(anyList())).thenReturn(List.of(PracticeResponseDto.builder()
                .practiceId(practiceId).practiceName(practiceEntity.getPracticeName()).practiceDescription(practiceEntity.getPracticeDescription())
                .practiceStatus(practiceEntity.getPracticeStatus()).practiceIconStorageId(practiceEntity.getPracticeIconStorageId())
                .practiceBannerStorageId(practiceEntity.getPracticeBannerStorageId())
                .tags(AppUtils.readValue(practiceEntity.getTags(), new TypeReference<>() {
                }))
                .build()));
        ResponseEntity<AppResponseDto<List<PracticeResponseDto>>> response = practiceController.getPractices(0, 10, "", Sort.Direction.ASC, "",practiceCategoryId);
        verify(practiceService, times(1)).getPractices(anyInt(), anyInt(), anyString(), any(), anyString(),any(UUID.class));
        assert response.getStatusCode() == HttpStatus.OK;
        assert response.getBody() != null;
        assert response.getBody().getData().size() == 1;
    }

    @Test
    void testDeletePractice() {
        doNothing().when(practiceService).delete(any(UUID.class));
        ResponseEntity<AppResponseDto<Void>> response = practiceController.deletePractice(practiceId);
        verify(practiceService, times(1)).delete(any(UUID.class));
        assert response.getStatusCode() == HttpStatus.OK;
        assert response.getBody() != null;
        assert response.getBody().getMessage().equals(AppUtils.Messages.DELETE_SUCCESS.getMessage());
    }

    @Test
    void testChangeNewStatus() {
        doNothing().when(practiceService).changePracticeStatus(any(UUID.class), any(AppUtils.PracticeStatus.class));
        ResponseEntity<AppResponseDto<Void>> response = practiceController.changePracticeStatus(practiceId, AppUtils.PracticeStatus.ACTIVE);
        verify(practiceService, times(1)).changePracticeStatus(any(UUID.class), any(AppUtils.PracticeStatus.class));
        assert response.getStatusCode() == HttpStatus.OK;
        assert response.getBody() != null;
        assert response.getBody().getMessage().equals(AppUtils.Messages.UPDATE_SUCCESS.getMessage());
    }

    @Test
    void testGetPortalDashboard() {
        when(practiceService.getTotalPractices()).thenReturn(1L);
        ResponseEntity<AppResponseDto<TotalDto>> response = practiceController.getPortalDashboard();
        verify(practiceService, times(1)).getTotalPractices();
        assert response.getStatusCode() == HttpStatus.OK;
        assert response.getBody() != null;
        assert response.getBody().getData().getTotal() == 1;
    }
}
