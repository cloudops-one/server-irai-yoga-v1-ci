package yoga.irai.server.poem;

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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PoemControllerTest {

    @Mock
    private PoemService poemService;

    @InjectMocks
    private PoemController poemController;

    private PoemRequestDto poemRequestDto;
    private PoemEntity poemEntity;
    private UUID poemId;

    @BeforeEach
    void setUp() {
        poemId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        poemRequestDto = PoemRequestDto.builder()
                .poemName("Test Poem").poemDescription("This is a test poem.")
                .orgId(orgId).poemAuthor("Author").poemDuration(1000L).poemStorageId(UUID.randomUUID())
                .poemBannerStorageId(UUID.randomUUID()).poemIconStorageId(UUID.randomUUID())
                .poemText("This is a test poem.").poemTags(Set.of("tag1", "tag2"))
                .build();
        poemEntity = PoemEntity.builder()
                .poemId(poemId).poemName("Test Poem").poemDescription("This is a test poem.").orgId(orgId)
                .poemAuthor("Author").poemDuration(1000L).poemStorageId(UUID.randomUUID())
                .poemBannerStorageId(UUID.randomUUID()).poemIconStorageId(UUID.randomUUID())
                .poemText("This is a test poem.").poemTags("[\"tag1\",\"tag2\"]").poemStatus(AppUtils.PoemStatus.ACTIVE)
                .build();
    }

    @Test
    void testAddPoem() {
        when(poemService.addPoem(any(PoemRequestDto.class))).thenReturn(poemEntity);
        when(poemService.getPoemResponseDto(any(PoemEntity.class))).thenReturn(
                PoemResponseDto.builder().poemId(poemEntity.getPoemId()).poemStatus(AppUtils.PoemStatus.ACTIVE)
                        .poemDescription(poemEntity.getPoemDescription())
                        .poemAuthor(poemEntity.getPoemAuthor()).poemDuration(poemEntity.getPoemDuration())
                        .poemStorageId(poemEntity.getPoemStorageId()).poemBannerStorageId(poemEntity.getPoemBannerStorageId())
                        .poemIconStorageId(poemEntity.getPoemIconStorageId()).poemText(poemEntity.getPoemText())
                        .poemTags((AppUtils.readValue(poemEntity.getPoemTags(), new TypeReference<>() {
                        })))
                        .build()
        );

        ResponseEntity<AppResponseDto<PoemResponseDto>> response = poemController.addPoem(poemRequestDto);
        verify(poemService, times(1)).addPoem(poemRequestDto);
        assert response.getStatusCode() == HttpStatus.OK;
    }

    @Test
    void testUpdatePoem() {
        when(poemService.updatePoem(any(UUID.class),any(PoemRequestDto.class))).thenReturn(poemEntity);
        when(poemService.getPoemResponseDto(any(PoemEntity.class))).thenReturn(
                PoemResponseDto.builder().poemId(poemEntity.getPoemId()).poemStatus(AppUtils.PoemStatus.ACTIVE)
                        .poemDescription(poemEntity.getPoemDescription())
                        .poemAuthor(poemEntity.getPoemAuthor()).poemDuration(poemEntity.getPoemDuration())
                        .poemStorageId(poemEntity.getPoemStorageId()).poemBannerStorageId(poemEntity.getPoemBannerStorageId())
                        .poemIconStorageId(poemEntity.getPoemIconStorageId()).poemText(poemEntity.getPoemText())
                        .poemTags((AppUtils.readValue(poemEntity.getPoemTags(), new TypeReference<>() {
                        })))
                        .build()
        );

        ResponseEntity<AppResponseDto<PoemResponseDto>> response = poemController.updatePoem(poemId, poemRequestDto);
        verify(poemService, times(1)).updatePoem(poemId, poemRequestDto);
        assert response.getStatusCode() == HttpStatus.OK;
    }

    @Test
    void testGetPoemById() {
        when(poemService.getPoemById(any(UUID.class))).thenReturn(poemEntity);
        when(poemService.getPoemResponseDto(any(PoemEntity.class))).thenReturn(
                PoemResponseDto.builder().poemId(poemEntity.getPoemId()).poemStatus(AppUtils.PoemStatus.ACTIVE)
                        .poemDescription(poemEntity.getPoemDescription())
                        .poemAuthor(poemEntity.getPoemAuthor()).poemDuration(poemEntity.getPoemDuration())
                        .poemStorageId(poemEntity.getPoemStorageId()).poemBannerStorageId(poemEntity.getPoemBannerStorageId())
                        .poemIconStorageId(poemEntity.getPoemIconStorageId()).poemText(poemEntity.getPoemText())
                        .poemTags((AppUtils.readValue(poemEntity.getPoemTags(), new TypeReference<>() {
                        })))
                        .build()
        );

        ResponseEntity<AppResponseDto<PoemResponseDto>> response = poemController.getPoem(poemId);
        verify(poemService, times(1)).getPoemById(poemId);
        assert response.getStatusCode() == HttpStatus.OK;
        assert response.getBody() != null;
        assert response.getBody().getData().getPoemId().equals(poemEntity.getPoemId());
    }

    @Test
    void testSearchPoems() {
        Page<PoemEntity> poemPage = new PageImpl<>(List.of(poemEntity));
        when(poemService.getPoems(anyInt(), anyInt(), anyString(), any(), anyString())).thenReturn(poemPage);
        when(poemService.toPoemResponseDto(anyList())).thenReturn(List.of(
                        PoemResponseDto.builder().poemId(poemEntity.getPoemId()).poemStatus(AppUtils.PoemStatus.ACTIVE)
                                .poemDescription(poemEntity.getPoemDescription())
                                .poemAuthor(poemEntity.getPoemAuthor()).poemDuration(poemEntity.getPoemDuration())
                                .poemStorageId(poemEntity.getPoemStorageId()).poemBannerStorageId(poemEntity.getPoemBannerStorageId())
                                .poemIconStorageId(poemEntity.getPoemIconStorageId()).poemText(poemEntity.getPoemText())
                                .poemTags((AppUtils.readValue(poemEntity.getPoemTags(), new TypeReference<>() {
                                })))
                                .build()
                )
        );

        ResponseEntity<AppResponseDto<List<PoemResponseDto>>> response = poemController.getAllPoems(0, 0, "", Sort.Direction.ASC, "createdAt");
        verify(poemService, times(1)).getPoems(anyInt(), anyInt(), anyString(), any(), anyString());
        assert response.getStatusCode() == HttpStatus.OK;
        assert response.getBody() != null;
        assert response.getBody().getData().size() == 1;
    }

    @Test
    void testDeletePoem() {
        doNothing().when(poemService).deletePoem(any(UUID.class));
        ResponseEntity<AppResponseDto<Void>> response = poemController.deletePoem(poemId);
        verify(poemService, times(1)).deletePoem(poemId);
        assert response.getStatusCode() == HttpStatus.OK;
        assert response.getBody() != null;
        assert response.getBody().getMessage().equals(AppUtils.Messages.DELETE_SUCCESS.getMessage());
    }

    @Test
    void testChangePoemStatus() {
        doNothing().when(poemService).changePoemStatus(any(UUID.class), any(AppUtils.PoemStatus.class));
        ResponseEntity<AppResponseDto<Void>> response = poemController.changePoemStatus(poemId, AppUtils.PoemStatus.ACTIVE);
        verify(poemService, times(1)).changePoemStatus(poemId, AppUtils.PoemStatus.ACTIVE);
        assert response.getStatusCode() == HttpStatus.OK;
        assert response.getBody() != null;
        assert response.getBody().getMessage().equals(AppUtils.Messages.UPDATE_SUCCESS.getMessage());
    }

    @Test
    void testGetPortalDashboard() {
        when(poemService.getTotalPoems()).thenReturn(1L);
        ResponseEntity<AppResponseDto<TotalDto>> response = poemController.getPortalDashboard();
        verify(poemService, times(1)).getTotalPoems();
        assert response.getStatusCode() == HttpStatus.OK;
        assert response.getBody() != null;
        assert response.getBody().getData().getTotal() == 1L;
    }
}
