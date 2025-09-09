package yoga.irai.server.news;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.dto.AppResponseDto;

/**
 * Controller for managing news articles. Provides endpoints to add, update,
 * retrieve, and delete news articles.
 */
@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/news")
@Tag(name = "News", description = "APIs for managing news")
public class NewsController {
    private final NewsService newsService;

    /**
     * Adds a new news article.
     *
     * @param newsRequestDto
     *            the request DTO containing news details
     * @return ResponseEntity with the added news article
     */
    @PostMapping
    @Operation(summary = "Add News", description = "Add a new news article")
    public ResponseEntity<AppResponseDto<NewsResponseDto>> addNews(@Valid @RequestBody NewsRequestDto newsRequestDto) {
        NewsEntity newsEntity = newsService.addNews(newsRequestDto);
        NewsResponseDto newsResponseDto = newsService.toNewsResponseDto(newsEntity);
        return ResponseEntity.ok(AppResponseDto.<NewsResponseDto>builder().data(newsResponseDto)
                .message(AppUtils.Messages.ADD_SUCCESS.getMessage()).build());
    }

    /**
     * Updates an existing news article.
     *
     * @param newsId
     *            the ID of the news article to update
     * @param newsRequestDto
     *            the request DTO containing updated news details
     * @return ResponseEntity with the updated news article
     */
    @PutMapping("/{newsId}")
    @Operation(summary = "Update News", description = "Update an existing news article")
    public ResponseEntity<AppResponseDto<NewsResponseDto>> updateNews(@PathVariable UUID newsId,
            @Valid @RequestBody NewsRequestDto newsRequestDto) {
        NewsEntity newsEntity = newsService.updateNews(newsId, newsRequestDto);
        NewsResponseDto newsResponseDto = newsService.toNewsResponseDto(newsEntity);
        return ResponseEntity.ok(AppResponseDto.<NewsResponseDto>builder().data(newsResponseDto)
                .message(AppUtils.Messages.UPDATE_SUCCESS.getMessage()).build());
    }

    /**
     * Retrieves a news article by its ID.
     *
     * @param newsId
     *            the ID of the news article to retrieve
     * @return ResponseEntity with the retrieved news article
     */
    @GetMapping("/{newsId}")
    @Operation(summary = "Get News", description = "Retrieve a news article by its ID")
    public ResponseEntity<AppResponseDto<NewsResponseDto>> getNews(@PathVariable UUID newsId) {
        NewsEntity newsEntity = newsService.getNewsById(newsId);
        NewsResponseDto newsResponseDto = newsService.toNewsResponseDto(newsEntity);
        return ResponseEntity.ok(AppResponseDto.<NewsResponseDto>builder().data(newsResponseDto)
                .message(AppUtils.Messages.NEWS_FOUND.getMessage()).build());
    }

    /**
     * Retrieves a list of news articles with pagination and filtering options.
     *
     * @param pageNumber
     *            the page number to retrieve
     * @param pageSize
     *            the number of articles per page
     * @param sortBy
     *            the field to sort by
     * @param direction
     *            the sort direction (ASC or DESC)
     * @param keyword
     *            optional keyword to filter news articles
     * @return ResponseEntity with a list of news articles
     */
    @GetMapping
    @Operation(summary = "Get News List", description = "Retrieve a list of news articles with pagination and filtering options")
    public ResponseEntity<AppResponseDto<List<NewsResponseDto>>> getNewsList(
            @RequestParam(defaultValue = "0") int pageNumber, @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction,
            @RequestParam(required = false) String keyword) {
        Page<NewsEntity> newsPage = newsService.getNews(pageNumber, pageSize, sortBy, direction, keyword);
        List<NewsEntity> newsEntities = newsPage.getContent();
        return ResponseEntity.ok(AppResponseDto.<List<NewsResponseDto>>builder()
                .data(newsService.toNewsResponseDtos(newsEntities)).message(AppUtils.Messages.SEARCH_FOUND.getMessage())
                .pageable(AppResponseDto.buildPageable(newsPage, sortBy, direction)).build());
    }

    /**
     * Deletes a news article by its ID.
     *
     * @param newsId
     *            the ID of the news article to delete
     * @return ResponseEntity with a success message
     */
    @DeleteMapping("/{newsId}")
    @Operation(summary = "Delete News", description = "Delete a news article by its ID")
    public ResponseEntity<AppResponseDto<Void>> deleteNews(@PathVariable UUID newsId) {
        newsService.deleteNews(newsId);
        return ResponseEntity
                .ok(AppResponseDto.<Void>builder().message(AppUtils.Messages.DELETE_SUCCESS.getMessage()).build());
    }

    /**
     * Changes the status of a news article.
     *
     * @param newsId
     *            the ID of the news article
     * @param status
     *            the new status to set
     * @return ResponseEntity with a success message
     */
    @PutMapping("/status/{newsId}")
    @Operation(summary = "Change News Status", description = "Change the status of a news article")
    public ResponseEntity<AppResponseDto<Void>> changeNewsStatus(@PathVariable UUID newsId,
            @RequestParam AppUtils.NewsStatus status) {
        newsService.changeNewsStatus(newsId, status);
        return ResponseEntity
                .ok(AppResponseDto.<Void>builder().message(AppUtils.Messages.UPDATE_SUCCESS.getMessage()).build());
    }

    /**
     * Retrieves the top news for the portal dashboard.
     *
     * @return a response audit containing a list of top news
     */
    @GetMapping("/dashboard/portal")
    @Operation(summary = "Get Portal Dashboard news", description = "Retrieve the top news for the portal dashboard. Returns a list of top events.")
    public ResponseEntity<AppResponseDto<List<NewsResponseDto>>> getPortalDashboard() {
        return ResponseEntity.ok(AppResponseDto.<List<NewsResponseDto>>builder()
                .data(newsService.toNewsResponseDtos(newsService.getTopNews())).build());
    }
}
