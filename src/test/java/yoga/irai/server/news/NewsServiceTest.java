package yoga.irai.server.news;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.exception.AppException;
import yoga.irai.server.authentication.service.UserService;
import yoga.irai.server.mobile.dto.NewsMobileResponseDto;
import yoga.irai.server.notification.NotificationService;
import yoga.irai.server.organization.OrganizationService;
import yoga.irai.server.setting.SettingService;
import yoga.irai.server.storage.StorageService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsServiceTest {

    @InjectMocks
    private NewsService newsService;

    @Mock
    private UserService userService;

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private StorageService storageService;

    @Mock
    private SettingService settingService;

    @Mock
    private OrganizationService organizationService;

    @Mock
    private NotificationService notificationService;

    private UUID newsId;
    private UUID newsIconStorageId;
    private UUID newsBannerStorageId;
    private UUID updatedNewsIconStorageId;
    private UUID updatedNewsBannerStorageId;
    private String newsIconExternalUrl;
    private String newsBannerExternalUrl;
    private String updatedNewsIconExternalUrl;
    private String updatedNewsBannerExternalUrl;
    private NewsEntity newsEntity;
    private NewsRequestDto newsRequestDto;
    private Page<NewsEntity> mockPage;

    @BeforeEach
    void setUp() {
        newsId = UUID.randomUUID();
        newsBannerStorageId = UUID.randomUUID();
        newsIconStorageId = UUID.randomUUID();
        updatedNewsIconStorageId = UUID.randomUUID();
        updatedNewsBannerStorageId = UUID.randomUUID();
        newsBannerExternalUrl = "https://test.test/banner";
        newsIconExternalUrl = "https://test.test/icon";
        updatedNewsBannerExternalUrl = "https://test.test/banner";
        updatedNewsIconExternalUrl = "https://test.test/icon";
        newsRequestDto = NewsRequestDto.builder()
                .newsName("Test News").newsDescription("Test News Description")
                .newsIconStorageId(newsIconStorageId).newsBannerStorageId(newsBannerStorageId)
                .isRecommended(true).tags(Set.of("tag1", "tag2"))
                .build();

        newsEntity = NewsEntity.builder()
                .newsId(newsId).newsIconStorageId(newsIconStorageId)
                .newsBannerStorageId(newsBannerStorageId).isRecommended(newsRequestDto.getIsRecommended())
                .newsName(newsRequestDto.getNewsName()).newsDescription(newsRequestDto.getNewsDescription())
                .tags("[\"tag1\", \"tag2\"]").build();
        newsEntity.setCreatedBy(UUID.randomUUID());
        newsEntity.setUpdatedBy(UUID.randomUUID());
        mockPage = new PageImpl<>(List.of(newsEntity));
    }

    @Test
    void testAddNews_Success() {
        when(newsRepository.save(any(NewsEntity.class))).thenReturn(newsEntity);
        when(newsRepository.existsByNewsName(newsRequestDto.getNewsName()))
                .thenReturn(false);
        doNothing().when(settingService).updateSetting(any(AppUtils.SettingName.class), anySet());
        NewsEntity news = newsService.addNews(newsRequestDto);
        verify(newsRepository, times(1)).save(any(NewsEntity.class));
        verify(newsRepository, times(1)).existsByNewsName(anyString());
        assert news != null;
        assert news.getNewsId().equals(newsId);
    }

    @Test
    void testAddNews_Success_ExternalUrl() {
        newsRequestDto.setNewsBannerExternalUrl(newsBannerExternalUrl);
        newsRequestDto.setNewsIconExternalUrl(newsIconExternalUrl);
        newsRequestDto.setNewsBannerStorageId(null);
        newsRequestDto.setNewsIconStorageId(null);
        when(newsRepository.save(any(NewsEntity.class))).thenReturn(newsEntity);
        when(newsRepository.existsByNewsName(newsRequestDto.getNewsName()))
                .thenReturn(false);
        doNothing().when(settingService).updateSetting(any(AppUtils.SettingName.class), anySet());
        NewsEntity news = newsService.addNews(newsRequestDto);
        verify(newsRepository, times(1)).save(any(NewsEntity.class));
        verify(newsRepository, times(1)).existsByNewsName(anyString());
        assert news != null;
        assert news.getNewsId().equals(newsId);
    }


    @Test
    void testAddNews_Success_NoTags() {
        newsRequestDto.setTags(null);
        when(newsRepository.save(any(NewsEntity.class))).thenReturn(newsEntity);
        when(newsRepository.existsByNewsName(newsRequestDto.getNewsName()))
                .thenReturn(false);
        NewsEntity news = newsService.addNews(newsRequestDto);
        verify(newsRepository, times(1)).save(any(NewsEntity.class));
        verify(newsRepository, times(1)).existsByNewsName(anyString());
        assert news != null;
        assert news.getNewsId().equals(newsId);
    }

    @Test
    void testAddNews_Failure() {
        when(newsRepository.existsByNewsName(newsRequestDto.getNewsName()))
                .thenReturn(true);
        AppException ex = assertThrows(AppException.class, () ->
                newsService.addNews(newsRequestDto));
        assertEquals(AppUtils.Messages.NAME_EXISTS.getMessage(), ex.getMessage());
        verify(newsRepository, never()).save(any(NewsEntity.class));
    }

    @Test
    void testUpdateNews_Success() {
        when(newsRepository.save(any(NewsEntity.class))).thenReturn(newsEntity);
        when(newsRepository.findById(newsId)).thenReturn(Optional.of(newsEntity));
        NewsEntity news = newsService.updateNews(newsId, newsRequestDto);
        verify(newsRepository, times(1)).save(any(NewsEntity.class));
        assert news != null;
        assert news.getNewsId().equals(newsId);
    }

    @Test
    void testUpdateNews_Success_NoTags() {
        newsRequestDto.setTags(null);
        when(newsRepository.save(any(NewsEntity.class))).thenReturn(newsEntity);
        when(newsRepository.findById(newsId)).thenReturn(Optional.of(newsEntity));
        NewsEntity news = newsService.updateNews(newsId, newsRequestDto);
        verify(newsRepository, times(1)).save(any(NewsEntity.class));
        assert news != null;
        assert news.getNewsId().equals(newsId);
    }

    @Test
    void testUpdateNews_Success_IconStorageId_NoChange() {
        newsRequestDto.setNewsIconStorageId(newsIconStorageId);
        when(newsRepository.save(any(NewsEntity.class))).thenReturn(newsEntity);
        when(newsRepository.findById(newsId)).thenReturn(Optional.of(newsEntity));

        NewsEntity news = newsService.updateNews(newsId, newsRequestDto);

        verify(newsRepository, times(1)).save(any(NewsEntity.class));
        assert news != null;
        assert news.getNewsIconStorageId().equals(newsIconStorageId);
    }

    @Test
    void testUpdateNews_Success_BannerStorageId_NoChange() {
        newsRequestDto.setNewsBannerStorageId(newsBannerStorageId);
        when(newsRepository.save(any(NewsEntity.class))).thenReturn(newsEntity);
        when(newsRepository.findById(newsId)).thenReturn(Optional.of(newsEntity));

        NewsEntity news = newsService.updateNews(newsId, newsRequestDto);

        verify(newsRepository, times(1)).save(any(NewsEntity.class));
        assert news != null;
        assert news.getNewsBannerStorageId().equals(newsBannerStorageId);
    }

    @Test
    void testUpdateNews_Success_IconStorageId() {
        newsRequestDto.setNewsIconStorageId(updatedNewsIconStorageId);
        when(newsRepository.save(any(NewsEntity.class))).thenReturn(newsEntity);
        when(newsRepository.findById(newsId)).thenReturn(Optional.of(newsEntity));
        NewsEntity news = newsService.updateNews(newsId, newsRequestDto);

        verify(newsRepository, times(1)).save(any(NewsEntity.class));
        assert news != null;
        assert news.getNewsIconStorageId().equals(updatedNewsIconStorageId);
    }

    @Test
    void testUpdateNews_Success_BannerStorageId() {
        newsRequestDto.setNewsBannerStorageId(updatedNewsBannerStorageId);
        when(newsRepository.save(any(NewsEntity.class))).thenReturn(newsEntity);
        when(newsRepository.findById(newsId)).thenReturn(Optional.of(newsEntity));

        NewsEntity news = newsService.updateNews(newsId, newsRequestDto);

        verify(newsRepository, times(1)).save(any(NewsEntity.class));
        assert news != null;
        assert news.getNewsBannerStorageId().equals(updatedNewsBannerStorageId);
    }

    @Test
    void testUpdateNews_Success_IconStorageUrl_NoChange() {
        newsRequestDto.setNewsIconExternalUrl(updatedNewsIconExternalUrl);
        newsRequestDto.setNewsIconStorageId(null);
        newsEntity.setNewsIconExternalUrl(updatedNewsIconExternalUrl);
        when(newsRepository.save(any(NewsEntity.class))).thenReturn(newsEntity);
        when(newsRepository.findById(newsId)).thenReturn(Optional.of(newsEntity));

        NewsEntity news = newsService.updateNews(newsId, newsRequestDto);

        verify(newsRepository, times(1)).save(any(NewsEntity.class));
        assert news != null;
        assert news.getNewsIconExternalUrl().equals(updatedNewsIconExternalUrl);
    }

    @Test
    void testUpdateNews_Success_BannerStorageUrl_NoChange() {
        newsRequestDto.setNewsBannerExternalUrl(updatedNewsBannerExternalUrl);
        newsRequestDto.setNewsBannerStorageId(null);
        newsEntity.setNewsBannerExternalUrl(updatedNewsBannerExternalUrl);
        when(newsRepository.save(any(NewsEntity.class))).thenReturn(newsEntity);
        when(newsRepository.findById(newsId)).thenReturn(Optional.of(newsEntity));

        NewsEntity news = newsService.updateNews(newsId, newsRequestDto);

        verify(newsRepository, times(1)).save(any(NewsEntity.class));
        assert news != null;
        assert news.getNewsBannerExternalUrl().equals(updatedNewsBannerExternalUrl);
    }

    @Test
    void testUpdateNews_Success_IconStorageUrl() {
        newsRequestDto.setNewsIconExternalUrl(updatedNewsIconExternalUrl);
        newsRequestDto.setNewsIconStorageId(null);
        when(newsRepository.save(any(NewsEntity.class))).thenReturn(newsEntity);
        when(newsRepository.findById(newsId)).thenReturn(Optional.of(newsEntity));

        NewsEntity news = newsService.updateNews(newsId, newsRequestDto);

        verify(newsRepository, times(1)).save(any(NewsEntity.class));
        assert news != null;
        assert news.getNewsIconExternalUrl().equals(updatedNewsIconExternalUrl);
    }

    @Test
    void testUpdateNews_Success_BannerStorageUrl() {
        newsRequestDto.setNewsBannerExternalUrl(updatedNewsBannerExternalUrl);
        newsRequestDto.setNewsBannerStorageId(null);
        when(newsRepository.save(any(NewsEntity.class))).thenReturn(newsEntity);
        when(newsRepository.findById(newsId)).thenReturn(Optional.of(newsEntity));

        NewsEntity news = newsService.updateNews(newsId, newsRequestDto);

        verify(newsRepository, times(1)).save(any(NewsEntity.class));
        assert news != null;
        assert news.getNewsBannerExternalUrl().equals(updatedNewsBannerExternalUrl);
    }

    @Test
    void testPoemSearch() {
        when(newsRepository.search(anyString(), any(Pageable.class))).thenReturn(mockPage);
        Page<NewsEntity> news = newsService.getNews(0, 10, "CreatedAt", Sort.Direction.ASC, "");
        verify(newsRepository, times(1)).search(anyString(), any(Pageable.class));
        assert news != null;
        assert !news.getContent().isEmpty();
    }

    @Test
    void testDeleteNews() {
        newsEntity.setNewsBannerStorageId(newsBannerStorageId);
        when(newsRepository.findById(any(UUID.class))).thenReturn(Optional.of(newsEntity));
        doNothing().when(newsRepository).deleteById(any(UUID.class));
        doNothing().when(storageService).deleteStorageByIds(anySet());
        newsService.deleteNews(newsId);
        verify(newsRepository, times(1)).deleteById(any(UUID.class));
    }

    @Test
    void testDeleteNews_NoStorageId() {
        newsEntity.setNewsBannerStorageId(null);
        newsEntity.setNewsIconStorageId(null);
        when(newsRepository.findById(any(UUID.class))).thenReturn(Optional.of(newsEntity));
        doNothing().when(newsRepository).deleteById(any(UUID.class));
        newsService.deleteNews(newsId);
        verify(newsRepository, times(1)).deleteById(any(UUID.class));
    }

    @Test
    void testChangeNewsStatus_Active_storageUrl() {
        try {
            newsEntity.setNewsBannerStorageId(newsBannerStorageId);
            newsEntity.setNewsBannerExternalUrl(null);
            when(newsRepository.findById(any(UUID.class))).thenReturn(Optional.of(newsEntity));
            when(organizationService.getTopicName()).thenReturn("topic");
            doNothing().when(notificationService).sendNotificationToTopic(anyString(), anyString(), anyString(), anyString(), anyString());
            when(newsRepository.save(any(NewsEntity.class))).thenReturn(newsEntity);
            when(storageService.getStorageUrl(any(UUID.class))).thenReturn(newsIconExternalUrl);
            when(userService.getUserNameById(any(UUID.class))).thenReturn("username");
            newsService.changeNewsStatus(newsId, AppUtils.NewsStatus.ACTIVE);
            verify(newsRepository, times(1)).save(any(NewsEntity.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testChangeNewsStatus_Active_externalUrl() {
        try {
            newsEntity.setNewsBannerExternalUrl(newsBannerExternalUrl);
            when(newsRepository.findById(any(UUID.class))).thenReturn(Optional.of(newsEntity));
            when(organizationService.getTopicName()).thenReturn("topic");
            doNothing().when(notificationService).sendNotificationToTopic(anyString(), anyString(), anyString(), anyString(), anyString());
            when(newsRepository.save(any(NewsEntity.class))).thenReturn(newsEntity);
            when(storageService.getStorageUrl(any(UUID.class))).thenReturn("");
            when(userService.getUserNameById(any(UUID.class))).thenReturn("username");
            newsService.changeNewsStatus(newsId, AppUtils.NewsStatus.ACTIVE);
            verify(newsRepository, times(1)).save(any(NewsEntity.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testChangeNewsStatus_Inactive() {
        try {
            when(newsRepository.findById(any(UUID.class))).thenReturn(Optional.of(newsEntity));
            when(newsRepository.save(any(NewsEntity.class))).thenReturn(newsEntity);
            newsService.changeNewsStatus(newsId, AppUtils.NewsStatus.INACTIVE);
            verify(newsRepository, times(1)).save(any(NewsEntity.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGetTopNews() {
        when(newsRepository.getNewsEntitiesByNewsStatusOrderByCreatedAtDesc(any(AppUtils.NewsStatus.class))).thenReturn(List.of(newsEntity));
        List<NewsEntity> news = newsService.getTopNews();
        verify(newsRepository, times(1)).getNewsEntitiesByNewsStatusOrderByCreatedAtDesc(any(AppUtils.NewsStatus.class));
        assertNotNull(news);
    }

    @Test
    void testGetTop3News() {
        when(newsRepository.findTop3ByNewsStatusOrderByCreatedAtDesc(any(AppUtils.NewsStatus.class))).thenReturn(List.of(newsEntity));
        List<NewsEntity> news = newsService.getTop3News();
        verify(newsRepository, times(1)).findTop3ByNewsStatusOrderByCreatedAtDesc(any(AppUtils.NewsStatus.class));
        assertNotNull(news);
    }

    @Test
    void testUpdateViewCount() {
        when(newsRepository.findById(any(UUID.class))).thenReturn(Optional.of(newsEntity));
        newsService.updateViewCount(newsId);
        verify(newsRepository, times(1)).save(any(NewsEntity.class));
    }

    @Test
    void testUpdateLikeCount() {
        when(newsRepository.findById(any(UUID.class))).thenReturn(Optional.of(newsEntity));
        newsService.updateLikeCount(newsId);
        verify(newsRepository, times(1)).save(any(NewsEntity.class));
    }

    @Test
    void testToNewsResponseDto() {
        when(storageService.getSignedStorageUrlByIds(anyList())).thenReturn(Map.of(UUID.randomUUID(), "https://test.test/url"));
        List<NewsResponseDto> newsResponseDtoList = newsService.toNewsResponseDtos(List.of(newsEntity));
        assertNotNull(newsResponseDtoList);
        assert newsResponseDtoList.size() == 1;
    }

    @Test
    void testToNewsMobileResponseDto() {
        when(storageService.getSignedStorageUrlByIds(anyList())).thenReturn(Map.of(UUID.randomUUID(), "https://test.test/url"));
        List<NewsMobileResponseDto> newsMobileResponseDtoList = newsService.toNewsMobileResponseDto(List.of(newsEntity));
        assertNotNull(newsMobileResponseDtoList);
        assert newsMobileResponseDtoList.size() == 1;
    }
}
