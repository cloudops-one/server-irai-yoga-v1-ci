package yoga.irai.server.news;

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

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsControllerTest {

    @Mock
    private NewsService newsService;

    @InjectMocks
    private NewsController newsController;

    private NewsRequestDto newsRequestDto;
    private NewsEntity newsEntity;
    private UUID newsId;

    @BeforeEach
    void setUp() {
        newsId = UUID.randomUUID();
        newsRequestDto = NewsRequestDto.builder()
                .newsName("Test News").newsDescription("Test News Description")
                .newsIconStorageId(UUID.randomUUID()).newsBannerStorageId(UUID.randomUUID())
                .isRecommended(true).tags(Set.of("tag1", "tag2"))
                .build();

        newsEntity = NewsEntity.builder()
                .newsId(newsId).newsIconStorageId(newsRequestDto.getNewsIconStorageId())
                .newsBannerStorageId(newsRequestDto.getNewsBannerStorageId()).isRecommended(newsRequestDto.getIsRecommended())
                .newsName(newsRequestDto.getNewsName()).newsDescription(newsRequestDto.getNewsDescription())
                .tags("[\"tag1\", \"tag2\"]").build();
    }

    @Test
    void testAddNews() {
        when(newsService.addNews(any(NewsRequestDto.class))).thenReturn(newsEntity);
        when(newsService.toNewsResponseDto(any(NewsEntity.class))).thenReturn(NewsResponseDto.builder()
                .newsId(newsId).newsName(newsEntity.getNewsName()).newsDescription(newsEntity.getNewsDescription())
                .newsStatus(newsEntity.getNewsStatus()).newsIconStorageId(newsEntity.getNewsIconStorageId())
                .newsBannerStorageId(newsEntity.getNewsBannerStorageId()).isRecommended(newsEntity.getIsRecommended())
                .tags(AppUtils.readValue(newsEntity.getTags(), new TypeReference<>() {
                }))
                .build());
        ResponseEntity<AppResponseDto<NewsResponseDto>> response = newsController.addNews(newsRequestDto);
        verify(newsService, times(1)).addNews(any(NewsRequestDto.class));
        assert response.getStatusCode() == HttpStatus.OK;
    }

    @Test
    void testUpdateNews() {
        when(newsService.updateNews(any(UUID.class), any(NewsRequestDto.class))).thenReturn(newsEntity);
        when(newsService.toNewsResponseDto(any(NewsEntity.class))).thenReturn(NewsResponseDto.builder()
                .newsId(newsId).newsName(newsEntity.getNewsName()).newsDescription(newsEntity.getNewsDescription())
                .newsStatus(newsEntity.getNewsStatus()).newsIconStorageId(newsEntity.getNewsIconStorageId())
                .newsBannerStorageId(newsEntity.getNewsBannerStorageId()).isRecommended(newsEntity.getIsRecommended())
                .tags(AppUtils.readValue(newsEntity.getTags(), new TypeReference<>() {
                }))
                .build());
        ResponseEntity<AppResponseDto<NewsResponseDto>> response = newsController.updateNews(newsId, newsRequestDto);
        verify(newsService, times(1)).updateNews(any(UUID.class), any(NewsRequestDto.class));
        assert response.getStatusCode() == HttpStatus.OK;
    }

    @Test
    void testGetPoemById() {
        when(newsService.getNewsById(any(UUID.class))).thenReturn(newsEntity);
        when(newsService.toNewsResponseDto(any(NewsEntity.class))).thenReturn(NewsResponseDto.builder()
                .newsId(newsId).newsName(newsEntity.getNewsName()).newsDescription(newsEntity.getNewsDescription())
                .newsStatus(newsEntity.getNewsStatus()).newsIconStorageId(newsEntity.getNewsIconStorageId())
                .newsBannerStorageId(newsEntity.getNewsBannerStorageId()).isRecommended(newsEntity.getIsRecommended())
                .tags(AppUtils.readValue(newsEntity.getTags(), new TypeReference<>() {
                }))
                .build());
        ResponseEntity<AppResponseDto<NewsResponseDto>> response = newsController.getNews(newsId);
        verify(newsService, times(1)).getNewsById(any(UUID.class));
        assert response.getStatusCode() == HttpStatus.OK;
    }

    @Test
    void testSearchNews() {
        Page<NewsEntity> newsPage = new PageImpl<>(List.of(newsEntity));
        when(newsService.getNews(anyInt(), anyInt(), anyString(), any(), anyString())).thenReturn(newsPage);
        when(newsService.toNewsResponseDtos(anyList())).thenReturn(List.of(NewsResponseDto.builder()
                .newsId(newsId).newsName(newsEntity.getNewsName()).newsDescription(newsEntity.getNewsDescription())
                .newsStatus(newsEntity.getNewsStatus()).newsIconStorageId(newsEntity.getNewsIconStorageId())
                .newsBannerStorageId(newsEntity.getNewsBannerStorageId()).isRecommended(newsEntity.getIsRecommended())
                .tags(AppUtils.readValue(newsEntity.getTags(), new TypeReference<>() {
                }))
                .build()));
        ResponseEntity<AppResponseDto<List<NewsResponseDto>>> response = newsController.getNewsList(0, 10, "", Sort.Direction.ASC, "");
        verify(newsService, times(1)).getNews(anyInt(), anyInt(), anyString(), any(), anyString());
        assert response.getStatusCode() == HttpStatus.OK;
        assert response.getBody() != null;
        assert response.getBody().getData().size() == 1;
    }

    @Test
    void testDeleteNews() {
        doNothing().when(newsService).deleteNews(any(UUID.class));
        ResponseEntity<AppResponseDto<Void>> response = newsController.deleteNews(newsId);
        verify(newsService, times(1)).deleteNews(any(UUID.class));
        assert response.getStatusCode() == HttpStatus.OK;
        assert response.getBody() != null;
        assert response.getBody().getMessage().equals(AppUtils.Messages.DELETE_SUCCESS.getMessage());
    }

    @Test
    void testChangeNewStatus() {
        doNothing().when(newsService).changeNewsStatus(any(UUID.class), any(AppUtils.NewsStatus.class));
        ResponseEntity<AppResponseDto<Void>> response = newsController.changeNewsStatus(newsId, AppUtils.NewsStatus.ACTIVE);
        verify(newsService, times(1)).changeNewsStatus(any(UUID.class), any(AppUtils.NewsStatus.class));
        assert response.getStatusCode() == HttpStatus.OK;
        assert response.getBody() != null;
        assert response.getBody().getMessage().equals(AppUtils.Messages.UPDATE_SUCCESS.getMessage());
    }

    @Test
    void testGetPortalDashboard() {
        when(newsService.getTopNews()).thenReturn(List.of(newsEntity));
        when(newsService.toNewsResponseDtos(anyList())).thenReturn(
                List.of(
                        NewsResponseDto.builder()
                                .newsId(newsId).newsName(newsEntity.getNewsName()).newsDescription(newsEntity.getNewsDescription())
                                .newsStatus(newsEntity.getNewsStatus()).newsIconStorageId(newsEntity.getNewsIconStorageId())
                                .newsBannerStorageId(newsEntity.getNewsBannerStorageId()).isRecommended(newsEntity.getIsRecommended())
                                .tags(AppUtils.readValue(newsEntity.getTags(), new TypeReference<>() {
                                }))
                                .build()
                )
        );
        ResponseEntity<AppResponseDto<List<NewsResponseDto>>> response = newsController.getPortalDashboard();
        verify(newsService, times(1)).getTopNews();
        assert response.getStatusCode() == HttpStatus.OK;
        assert response.getBody() != null;
        assert response.getBody().getData().size() == 1;
    }
}
