package yoga.irai.server.storage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.dto.AppResponseDto;

/**
 * StorageController handles file storage operations such as uploading and
 * downloading files.
 */

@Validated
@RestController
@RequestMapping("/storage")
@Tag(name = "Storage Management", description = "APIs for managing file storage, including uploading, deleting, and retrieving files.")
public class StorageController {

    @Value("${app.default.storage.files}")
    private String appDefaultStorageFiles;
    private final StorageService storageService;

    /**
     * Constructor for StorageController.
     *
     * @param storageService
     *            the service for handling storage operations
     */
    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    /**
     * Uploads a file for a user by their UUID.
     *
     * @param storageRequestDto
     *            the DTO containing the upload request details
     * @return a response audit containing the uploaded StorgeId UUID if successful,
     *         or an error message if not found
     */
    @PostMapping("/upload")
    @Operation(summary = "Uploads File", description = "Upload a file for a user by their UUID. "
            + "Returns the uploaded file storageId if successful, or an error message if not found.")
    public ResponseEntity<AppResponseDto<StorageResponseDto>> uploadStorage(
            @Valid @ModelAttribute StorageRequestDto storageRequestDto) throws IOException {
        AppResponseDto.AppResponseDtoBuilder<StorageResponseDto> builder = AppResponseDto.builder();
        StorageEntity storageEntity = storageService.uploadStorage(storageRequestDto);
        return ResponseEntity
                .ok(builder.data(StorageResponseDto.builder().storageId(storageEntity.getStorageId()).build())
                        .message(AppUtils.Messages.STORAGE_FILE_UPLOADED_SUCCESS_1_STORAGE_ID
                                .getMessage(storageEntity.getStorageId()))
                        .build());
    }

    /**
     * Deletes a file by its storageName.
     *
     * @param storageName
     *            the name of the storage file to delete
     * @return a response audit indicating success or failure
     */
    @DeleteMapping("/{storageName}")
    @Operation(summary = "Delete File", description = "Deletes a file by its storageName. Returns a success message if the file is deleted successfully.")
    public ResponseEntity<AppResponseDto<Void>> deleteStorage(@PathVariable String storageName) {
        AppResponseDto.AppResponseDtoBuilder<Void> builder = AppResponseDto.builder();
        if (appDefaultStorageFiles.contains(storageName)) {
            AppUtils.Messages.STORAGE_APP_DEFAULT_FILES_CANNOT_BE_DELETED_1_STORAGE_NAME.throwException(storageName);
        }
        storageService.deleteStorage(storageName);
        return ResponseEntity.ok(
                builder.message(AppUtils.Messages.STORAGE_FILE_DELETED_SUCCESS_1_NAME.getMessage(storageName)).build());
    }

    /**
     * Searches for storages based on the provided parameters.
     *
     * @param pageNumber
     *            - the page number to retrieve, starting from 0
     * @param pageSize
     *            - the number of items per page
     * @param sortBy-
     *            the field to sort by, default is "createdAt"
     * @param direction
     *            - the sort direction, default is "DESC"
     * @param keyword
     *            - the keyword to search for in storage files, optional
     * @return a response containing a list of storage files matching the keyword
     *         and pagination information
     */
    @GetMapping
    @Operation(summary = "Search Storages", description = "Search files in the storage by keyword. Returns a list of storage files matching the keyword.")
    public ResponseEntity<AppResponseDto<List<StorageResponseDto>>> searchStorages(
            @RequestParam(defaultValue = "0") int pageNumber, @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction,
            @RequestParam(required = false) String keyword) {
        Page<StorageEntity> storagePage = storageService.searchStorages(pageNumber, pageSize, sortBy, direction,
                keyword);
        List<StorageEntity> storageEntities = storagePage.getContent();
        return ResponseEntity.ok(AppResponseDto.<List<StorageResponseDto>>builder()
                .data(storageService.toStorageResponse(storageEntities))
                .message(AppUtils.Messages.STORAGE_SEARCHES_FOUND.getMessage())
                .pageable(AppResponseDto.buildPageable(storagePage, sortBy, direction)).build());
    }

    /**
     * Synchronizes the storage with the database.
     *
     * @return a response contains message indicating the synchronization status
     */
    @GetMapping("/sync")
    @Operation(summary = "Sync Storage", description = "Synchronizes the storage with the database. Returns a list of storage files that were synchronized.")
    public ResponseEntity<AppResponseDto<Void>> syncStorage() {
        storageService.syncStorage();
        return ResponseEntity.ok(
                AppResponseDto.<Void>builder().message(AppUtils.Messages.STORAGE_SYNCED_SUCCESS.getMessage()).build());
    }

    @GetMapping("/sync/updated/at")
    @Operation(summary = "Get Last Sync Time", description = "Retrieves the last synchronization time of the storage. Returns the timestamp of the last sync.")
    public ResponseEntity<AppResponseDto<String>> getLastSyncTime() {
        return ResponseEntity.ok(AppResponseDto.<String>builder().data(storageService.getLastSyncTime()).build());
    }
}
