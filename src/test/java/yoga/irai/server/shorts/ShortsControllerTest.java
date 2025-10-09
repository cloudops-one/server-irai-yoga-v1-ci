package yoga.irai.server.shorts;

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

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShortsControllerTest {
    @Mock
    private ShortsService shortsService;
    @InjectMocks
    private ShortsController shortsController;
    private ShortsRequestDto  shortsRequestDto;
    private ShortsResponseDto shortsResponseDto;
    private ShortsEntity shortsEntity;
    private UUID shortsId;
    @BeforeEach
    void setUp() {
        UUID orgId = UUID.randomUUID();
        shortsId = UUID.randomUUID();
        shortsResponseDto = ShortsResponseDto.builder()
                .shortsId(UUID.randomUUID())
                .shortsName("AI Shorts")
                .shortsDescription("AI in action")
                .likes(100L)
                .views(500L)
                .build();
        shortsEntity = ShortsEntity.builder()
                .shortsId(UUID.randomUUID())
                .orgId(orgId)
                .shortsStorageId(UUID.randomUUID())
                .shortsBannerStorageId(UUID.randomUUID())
                .shortsStatus(AppUtils.ShortsStatus.ACTIVE)
                .shortsExternalUrl("https://www.google.com")
                .shortsBannerExternalUrl("https://www.google.com")
                .shortsName("Test1")
                .shortsDescription("test")
                .duration(1L)
                .likes(5L)
                .views(4L)
                .tags("Meditation")
                .build();
        shortsRequestDto = ShortsRequestDto.builder()
                .orgId(orgId)
                .shortsStorageId(UUID.randomUUID())
                .shortsBannerStorageId(UUID.randomUUID())
                .shortsExternalUrl("https://www.google.com")
                .shortsBannerExternalUrl("https://www.google.com")
                .shortsName("Test1")
                .shortsDescription("Meditation")
                .duration(14L)
                .build();
    }
    @Test
    void addShortsTest(){
        when(shortsService.addShorts(any(ShortsRequestDto.class))).thenReturn(shortsEntity);
        when(shortsService.getShortsResponseDto(shortsEntity)).thenReturn(shortsResponseDto);
        ResponseEntity<AppResponseDto<ShortsResponseDto>> response =  shortsController.addShorts(shortsRequestDto);
        assert response.getStatusCode() == HttpStatus.OK;
        assertNotNull(response.getBody());
    }

    @Test
    void updateShortsTest(){
        when(shortsService.updateShorts(shortsId, shortsRequestDto)).thenReturn(shortsEntity);
        when(shortsService.getShortsResponseDto(shortsEntity)).thenReturn(shortsResponseDto);
        ResponseEntity<AppResponseDto<ShortsResponseDto>> response =  shortsController.updateShorts(shortsId , shortsRequestDto);
        assert response.getStatusCode() == HttpStatus.OK;
        assertNotNull(response.getBody());
    }

    @Test
    void getShortsTest(){
        when(shortsService.getShortsById(shortsId)).thenReturn(shortsEntity);
        when(shortsService.getShortsResponseDto(shortsEntity)).thenReturn(shortsResponseDto);
        ResponseEntity<AppResponseDto<ShortsResponseDto>> response =  shortsController.getShorts(shortsId);
        assert response.getStatusCode() == HttpStatus.OK;
        assertNotNull(response.getBody());
    }

    @Test
    void getShortsListTest() {
        int pageNumber = 0;
        int pageSize = 10;
        String sortBy = "createdAt";
        Sort.Direction direction = Sort.Direction.DESC;
        String keyword = "test";
        List<ShortsEntity> shortsEntities = Collections.singletonList(shortsEntity);
        Page<ShortsEntity> shortsPage = new PageImpl<>(shortsEntities);
        List<ShortsResponseDto> responseDto = Collections.singletonList(
                ShortsResponseDto.builder()
                        .shortsId(shortsEntity.getShortsId())
                        .shortsName(shortsEntity.getShortsName())
                        .shortsDescription(shortsEntity.getShortsDescription())
                        .build()
        );
        when(shortsService.getShorts(pageNumber, pageSize, sortBy, direction, keyword))
                .thenReturn(shortsPage);
        when(shortsService.toShortsResponseDto(shortsEntities)).thenReturn(responseDto);
        ResponseEntity<AppResponseDto<List<ShortsResponseDto>>> response =
                shortsController.getShortsList(pageNumber, pageSize, sortBy, direction, keyword);
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getData().size());
        assertEquals("Test1", response.getBody().getData().getFirst().getShortsName());
        assertEquals(AppUtils.Messages.SEARCH_FOUND.getMessage(), response.getBody().getMessage());
        verify(shortsService, times(1)).getShorts(pageNumber, pageSize, sortBy, direction, keyword);
        verify(shortsService, times(1)).toShortsResponseDto(shortsEntities);
    }
    @Test
    void deleteShortsTest() {
        ResponseEntity<AppResponseDto<Void>> response = shortsController.deleteShorts(shortsId);
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(AppUtils.Messages.DELETE_SUCCESS.getMessage(), response.getBody().getMessage());
        verify(shortsService, times(1)).deleteShorts(shortsId);
    }
    @Test
    void changeShortStatusTest() {
        AppUtils.ShortsStatus status = AppUtils.ShortsStatus.ACTIVE;
        ResponseEntity<AppResponseDto<Void>> response = shortsController.changeShortStatus(shortsId, status);
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(AppUtils.Messages.UPDATE_SUCCESS.getMessage(), response.getBody().getMessage());
        verify(shortsService, times(1)).changeShortsStatus(shortsId, status);
    }
    @Test
    void getPortalDashboardTest() {
        long totalShorts = 15L;
        when(shortsService.getTotalShorts()).thenReturn(totalShorts);
        ResponseEntity<AppResponseDto<TotalDto>> response = shortsController.getPortalDashboard();
        assertNotNull(response);
        AppResponseDto<TotalDto> body = response.getBody();
        assertNotNull(body);
        assertEquals(AppUtils.Messages.PRACTICE_FOUND.getMessage(), body.getMessage());
        assertNotNull(body.getData());
        assertEquals(totalShorts, body.getData().getTotal());
        verify(shortsService, times(1)).getTotalShorts();
    }
}
