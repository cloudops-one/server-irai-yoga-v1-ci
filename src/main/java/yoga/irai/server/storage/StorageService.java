package yoga.irai.server.storage;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.authentication.entity.UserEntity;
import yoga.irai.server.authentication.repository.UserRepository;
import yoga.irai.server.setting.SettingService;

/**
 * Service for handling file storage operations with S3-compatible storage.
 */
@Service
@RequiredArgsConstructor
public class StorageService {

    @Value("${digitalocean.spaces.endpoint}")
    private String endpoint;

    @Value("${digitalocean.spaces.access-key}")
    private String accessKey;

    @Value("${digitalocean.spaces.secret-key}")
    private String secretKey;

    @Value("${digitalocean.spaces.region}")
    private String region;

    @Value("${digitalocean.spaces.bucket}")
    private String bucket;

    @Value("${digitalocean.spaces.bucket.directory}")
    private String directory;

    @Value("${digitalocean.spaces.signature.duration.minutes}")
    private String signatureDurationMinutes;

    private final S3Client s3Client;
    private final SettingService settingService;
    private final UserRepository userRepository;
    private final StorageRepository storageRepository;

    /**
     * Uploads a file to the storage bucket and saves its metadata in the database.
     *
     * @param storageRequestDto
     *            the DTO containing the file and metadata
     * @return the saved StorageEntity with metadata
     * @throws IOException
     *             if an error occurs during file upload
     */
    @Transactional
    public StorageEntity uploadStorage(StorageRequestDto storageRequestDto) throws IOException {
        MultipartFile file = storageRequestDto.getFile();
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());

        StorageEntity storage = StorageEntity.builder()
                .tags(String.join(",", "module:" + storageRequestDto.getModuleType().toString(),
                        "orgId:" + AppUtils.getPrincipalOrgId().toString(),
                        "userId:" + AppUtils.getPrincipalUserId().toString()))
                .extension(extension).size(file.getSize()).contentType(file.getContentType()).build();
        StorageEntity storageEntity = storageRepository.save(storage);

        String key = storageEntity.getStorageId() + "." + extension;
        s3Client.putObject(
                PutObjectRequest.builder().bucket(bucket).key(directory + key).contentType(file.getContentType())
                        .contentDisposition("inline")
                        .metadata(Map.of("module", storageRequestDto.getModuleType().toString(), "orgId",
                                AppUtils.getPrincipalOrgId().toString(), "userId",
                                AppUtils.getPrincipalUserId().toString()))
                        .build(),
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        return storageEntity;
    }

    /**
     * Retrieves a list of file names in the storage bucket directory.
     *
     * @return list of file names in the storage bucket
     */
    public List<String> getStorageBucketFiles() {
        ListObjectsV2Response response = s3Client
                .listObjectsV2(ListObjectsV2Request.builder().bucket(bucket).prefix(directory).build());
        return response.contents().stream().map(S3Object::key).filter(key -> !key.equals(directory))
                .map(key -> key.substring(directory.length())).toList();
    }

    /**
     * Deletes a file from the storage bucket by its storageName.
     *
     * @param storageName
     *            the name of the storage file to delete
     */
    @Transactional
    public void deleteStorage(String storageName) {
        storageRepository.deleteById(UUID.fromString(FilenameUtils.getBaseName(storageName)));
        deleteStorageBucketFile(storageName);
    }

    /**
     * Deletes a file from the storage bucket by its storageId.
     *
     * @param storageId
     *            the UUID of the storage audit to delete
     */
    @Transactional
    public void deleteStorageById(UUID storageId) {
        if (ObjectUtils.isEmpty(storageId)) {
            return;
        }
        StorageEntity storageEntity = storageRepository.findById(storageId).orElse(null);
        if (storageEntity != null) {
            storageRepository.deleteById(storageId);
            deleteStorageBucketFile(storageId + "." + storageEntity.getExtension());
        }
    }

    @Transactional
    public void deleteStorageByIds(Set<UUID> storageIds) {
        if (ObjectUtils.isEmpty(storageIds)) {
            return;
        }
        for (UUID storageId : storageIds) {
            storageRepository.findById(storageId).ifPresent(
                    storageEntity -> deleteStorageBucketFile(storageId + "." + storageEntity.getExtension()));
        }
        storageRepository.deleteAllById(storageIds);
    }

    /**
     * Deletes a file from the storage bucket by its storageName.
     *
     * @param storageName
     *            the name of the storage file to delete
     */
    public void deleteStorageBucketFile(String storageName) {
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(directory + storageName).build());
    }

    /**
     * Retrieves the storage URL for a given storageId.
     *
     * @param storageId
     *            the UUID of the storage audit
     * @return the signed URL for accessing the file, or null if not found
     */
    public String getStorageUrl(UUID storageId) {
        if (ObjectUtils.isEmpty(storageId)) {
            return null;
        }
        StorageEntity storageEntity = storageRepository.findById(storageId).orElse(null);
        if (storageEntity != null && ObjectUtils.isNotEmpty(storageEntity.getExtension())) {
            return getSignedStorageUrl(storageId + "." + storageEntity.getExtension());
        }
        return null;
    }

    /**
     * Generates a signed URL for accessing a file in the storage bucket.
     *
     * @param storageName
     *            the name of the file to generate the URL for
     * @return the signed URL as a string
     */
    public String getSignedStorageUrl(String storageName) {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        try (S3Presigner s3Presigner = S3Presigner.builder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .endpointOverride(java.net.URI.create(endpoint)).region(Region.of(region)).build()) {
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(Long.parseLong(signatureDurationMinutes)))
                    .getObjectRequest(b -> b.bucket(bucket).key(directory + storageName)).build();
            return s3Presigner.presignGetObject(presignRequest).url().toString();
        }
    }

    /**
     * Retrieves a map of signed storage URLs for a list of storage IDs.
     *
     * @param list
     *            the list of UUIDs representing storage IDs
     * @return a map where keys are storage IDs and values are signed URLs
     */
    public Map<UUID, String> getSignedStorageUrlByIds(List<UUID> list) {
        List<StorageEntity> storageEntities = storageRepository.findAllById(list);
        return storageEntities.stream().collect(Collectors.toMap(StorageEntity::getStorageId,
                storage -> getSignedStorageUrl(storage.getStorageId() + "." + storage.getExtension())));
    }

    /**
     * Searches for storage files based on a keyword and returns a paginated result.
     *
     * @param pageNumber
     *            the page number to retrieve
     * @param pageSize
     *            the number of items per page
     * @param sortBy
     *            the field to sort by
     * @param direction
     *            the direction of sorting (ascending or descending)
     * @param keyword
     *            the keyword to search for in storage metadata
     * @return a paginated list of StorageEntity objects matching the search
     *         criteria
     */
    public Page<StorageEntity> searchStorages(int pageNumber, int pageSize, String sortBy, Sort.Direction direction,
            String keyword) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortBy));
        return storageRepository.search(keyword, pageable);
    }

    /**
     * Retrieves a map of usernames by their IDs.
     *
     * @param list
     *            the list of UUIDs representing user IDs
     * @return a map where keys are user IDs and values are concatenated first and
     *         last names
     */
    public Map<UUID, String> getUserNamesByIds(List<UUID> list) {
        List<UserEntity> usersEntities = userRepository.findAllById(list);
        return usersEntities.stream().collect(Collectors.toMap(UserEntity::getUserId,
                user -> user.getUserFirstName() + " " + user.getUserLastName()));
    }

    /**
     * Converts a list of StorageEntity objects to a list of StorageResponseDto
     * objects.
     *
     * @param storageEntities
     *            the list of StorageEntity objects to convert
     * @return a list of StorageResponseDto objects with additional metadata
     */
    public List<StorageResponseDto> toStorageResponse(List<StorageEntity> storageEntities) {
        Map<UUID, String> userNamesByIds = getUserNamesByIds(
                storageEntities.stream().map(StorageEntity::getStorageId).toList());

        return storageEntities.stream().map(storageEntity -> {
            StorageResponseDto storageResponseDto = AppUtils.map(storageEntity, StorageResponseDto.class);
            storageResponseDto.setStorageUrl(getStorageUrl(storageEntity.getStorageId()));
            storageResponseDto.setStorageName(storageEntity.getStorageId() + "." + storageEntity.getExtension());
            storageResponseDto.setCreatedByName(userNamesByIds.get(storageEntity.getCreatedBy()));
            return storageResponseDto;
        }).toList();
    }

    /**
     * Retrieves a list of orphaned storage files that are not present in the
     * storage bucket.
     *
     * @param storageEntities
     *            the list of storage response DTOs
     * @param storageBucketFiles
     *            the list of files in the storage bucket
     * @return a list of StorageResponseDto objects representing orphaned files
     */
    private List<StorageEntity> getStorageTableOrphanDtos(List<StorageEntity> storageEntities,
            List<String> storageBucketFiles) {
        return storageEntities.stream().filter(entity -> {
            String fileName = entity.getStorageId() + "." + entity.getExtension();
            return !storageBucketFiles.contains(fileName);
        }).map(entity -> {
            entity.setTags(AppUtils.Constants.STORAGE_TABLE_ORPHANED);
            return entity;
        }).toList();
    }

    /**
     * Retrieves a list of orphaned storage files that are not present in the
     * storage table.
     *
     * @param storageEntities
     *            the list of storage response DTOs
     * @param storageBucketFiles
     *            the list of files in the storage bucket
     */
    private void getStorageBucketOrphanDtos(List<StorageEntity> storageEntities, List<String> storageBucketFiles) {
        storageBucketFiles.forEach(storageName -> {
            String[] parts = storageName.split("\\.");
            if (parts.length == 2) {
                UUID storageId = UUID.fromString(parts[0]);
                String extension = parts[1];
                if (storageEntities.stream().noneMatch(
                        entity -> entity.getStorageId().equals(storageId) && entity.getExtension().equals(extension))) {
                    storageRepository.insertStorage(storageId, "", extension, 0L,
                            AppUtils.Constants.STORAGE_BUCKET_ORPHANED, AppUtils.getPrincipalUserId());
                }
            } else {
                storageRepository.insertStorage(UUID.randomUUID(), "", "", 0L,
                        AppUtils.Constants.STORAGE_BUCKET_ORPHANED, AppUtils.getPrincipalUserId());
            }
        });
    }

    /**
     * Synchronizes the storage by checking for orphaned files in both the storage
     * bucket and the storage table. It updates the storage table with orphaned
     * entries and removes any files that are no longer present in the bucket.
     */
    @Transactional
    public void syncStorage() {
        List<StorageEntity> storageEntities = storageRepository.findAll();
        List<String> storageBucketFiles = getStorageBucketFiles();
        getStorageBucketOrphanDtos(storageEntities, storageBucketFiles);
        List<StorageEntity> storageTableOrphans = getStorageTableOrphanDtos(storageEntities, storageBucketFiles);
        storageRepository.saveAll(storageTableOrphans);
        settingService.updateSync(AppUtils.Constants.STORAGE_SYNCED_AT, LocalDateTime.now().toString());
    }

    public String getLastSyncTime() {
        return settingService.getSettingBySettingName(AppUtils.Constants.STORAGE_SYNCED_AT).getSettingValue();
    }
}
