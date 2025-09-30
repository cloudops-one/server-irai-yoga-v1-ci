package yoga.irai.server.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.authentication.entity.UserEntity;
import yoga.irai.server.authentication.entity.UserPrincipalEntity;
import yoga.irai.server.authentication.repository.UserRepository;
import yoga.irai.server.setting.SettingEntity;
import yoga.irai.server.setting.SettingService;

import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StorageServiceTest {

    @Mock
    private S3Client s3Client;
    @Mock
    private StorageRepository storageRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SettingService settingService;

    @InjectMocks
    private StorageService storageService;
    private UUID storageId;
    private StorageEntity storageEntity;

    private StorageEntity storageEntityPdf;

    private StorageEntity storageEntityTxt;
    private UUID userId1;
    private UUID userId2 ;
    private UserEntity userEntity;
    private UserPrincipalEntity principal;

    @BeforeEach
    void setUp() {
        storageId = UUID.randomUUID();
         storageEntity = StorageEntity.builder()
                .storageId(storageId)
                .extension("jpg")
                .build();
        storageEntityTxt = StorageEntity.builder()
                .storageId(UUID.randomUUID())
                .extension("txt")
                .build();
        userId1 = UUID.randomUUID();
        storageEntityPdf = StorageEntity.builder()
                .storageId(storageId)
                .extension("pdf")
                .createdBy(userId1)
                .build();
        userId2 = UUID.randomUUID();
         userEntity = UserEntity.builder()
                .userId(userId1)
                .orgId(UUID.randomUUID())
                 .userFirstName("Hilton")
                 .userLastName("Paul").build();
        principal = new UserPrincipalEntity(userEntity);
        storageService = spy(new StorageService(s3Client, settingService, userRepository, storageRepository));
        ReflectionTestUtils.setField(storageService, "bucket", "test-bucket");
        ReflectionTestUtils.setField(storageService, "directory", "dir/");
        ReflectionTestUtils.setField(storageService, "signatureDurationMinutes", "5");
        ReflectionTestUtils.setField(storageService, "region", "us-east-1");
        ReflectionTestUtils.setField(storageService, "accessKey", "access");
        ReflectionTestUtils.setField(storageService, "secretKey", "secret");
        ReflectionTestUtils.setField(storageService, "endpoint", "http://localhost:9000");
    }

    @Test
    void getStorageBucketFiles_shouldReturnFileNames() {
        ListObjectsV2Response listObjectsV2Response = ListObjectsV2Response.builder()
                .contents(S3Object.builder().key("dir/file1.txt").build(),
                        S3Object.builder().key("dir/file2.jpg").build())
                .build();
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(listObjectsV2Response);
        List<String> result = storageService.getStorageBucketFiles();
        assertThat(result).containsExactlyInAnyOrder("file1.txt", "file2.jpg");
        verify(s3Client).listObjectsV2(any(ListObjectsV2Request.class));
    }
    @Test
    void deleteStorageById_shouldReturnWhenIdIsNull() {
        storageService.deleteStorageById(null);
        verifyNoInteractions(storageRepository);
    }

    @Test
    void deleteStorage_shouldDeleteFromDbAndBucket() {
        String fileName = storageId + ".png";
        storageService.deleteStorage(fileName);
        verify(storageRepository).deleteById(UUID.fromString(storageId.toString()));
        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void deleteStorageById_shouldRemoveIfExists() {
        when(storageRepository.findById(storageId)).thenReturn(Optional.of(storageEntity));
        storageService.deleteStorageById(storageId);
        verify(storageRepository).deleteById(storageId);
        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }
    @Test
    void deleteStorageByIds_shouldReturnWhenSetIsNull() {
        storageService.deleteStorageByIds(null);
        storageService.deleteStorageByIds(Collections.emptySet());
        verifyNoInteractions(storageRepository);
    }
    @Test
    void deleteStorageByIds_shouldDeleteExistingEntitiesAndCallBucketDeletion() {
        Set<UUID> ids = Set.of(userId1, userId2);
        StorageEntity storageEntity1 = StorageEntity.builder().storageId(userId1).extension("txt").build();
        StorageEntity storageEntity2 = StorageEntity.builder().storageId(userId2).extension("jpg").build();
        when(storageRepository.findById(userId1)).thenReturn(Optional.of(storageEntity1));
        when(storageRepository.findById(userId2)).thenReturn(Optional.of(storageEntity2));
        storageService.deleteStorageByIds(ids);
        verify(storageService).deleteStorageBucketFile(userId1 + ".txt");
        verify(storageService).deleteStorageBucketFile(userId2 + ".jpg");
        verify(storageRepository).deleteAllById(ids);
    }
    @Test
    void deleteStorageByIds_shouldSkipNonExistingEntities() {
        Set<UUID> ids = Set.of(userId1, userId2);
        StorageEntity entity1 = StorageEntity.builder().storageId(userId1).extension("txt").build();
        when(storageRepository.findById(userId1)).thenReturn(Optional.of(entity1));
        when(storageRepository.findById(userId2)).thenReturn(Optional.empty());
        storageService.deleteStorageByIds(ids);
        verify(storageService).deleteStorageBucketFile(userId1 + ".txt");
        verify(storageService, never()).deleteStorageBucketFile(userId2 + ".jpg");
        verify(storageRepository).deleteAllById(ids);
    }
    @Test
    void getStorageUrl_shouldReturnNullWhenIdIsNullOrExtensionIsEmpty() {
        assertThat(storageService.getStorageUrl(null)).isNull();

        StorageEntity entity = StorageEntity.builder()
                .storageId(userId1)
                .extension(null)
                .build();
        when(storageRepository.findById(userId1)).thenReturn(Optional.of(entity));

        assertThat(storageService.getStorageUrl(userId1)).isNull();
    }
    @Test
    void getSignedStorageUrlByIds_shouldReturnSignedUrls() {
        StorageEntity e1 = StorageEntity.builder().storageId(userId1).extension("txt").build();
        when(storageRepository.findAllById(List.of(userId1))).thenReturn(List.of(e1));
        Map<UUID, String> result = storageService.getSignedStorageUrlByIds(List.of(userId1));
        assertThat(result).containsKey(userId1);
    }
    @Test
    void toStorageResponse_shouldMapEntities() {
        when(userRepository.findAllById(any())).thenReturn(List.of(userEntity));
        when(storageRepository.findById(storageId)).thenReturn(Optional.of(storageEntityPdf));
        List<StorageResponseDto> responses = storageService.toStorageResponse(List.of(storageEntityPdf));
        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().getStorageName()).endsWith(".pdf");
        assertThat(responses.getFirst().getCreatedByName()).isEqualTo("Hilton Paul");
    }

    @Test
    void uploadStorage_shouldSaveAndUploadFile() throws IOException {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(principal);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
        byte[] content = "Hello World".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", content);
        StorageRequestDto dto = StorageRequestDto.builder()
                .file(file)
                .moduleType(AppUtils.ModuleType.PROGRAM)
                .build();
        UUID storageId = UUID.randomUUID();
        StorageEntity savedEntity = StorageEntity.builder()
                .storageId(storageId)
                .extension("txt")
                .size((long) content.length)
                .contentType("text/plain")
                .build();
        when(storageRepository.save(any(StorageEntity.class))).thenReturn(savedEntity);
        StorageEntity result = storageService.uploadStorage(dto);
        verify(storageRepository).save(any(StorageEntity.class));
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        assertThat(result).isNotNull();
        assertThat(result.getStorageId()).isEqualTo(storageId);
        assertThat(result.getExtension()).isEqualTo("txt");
        assertThat(result.getSize()).isEqualTo(content.length);
        assertThat(result.getContentType()).isEqualTo("text/plain");
    }
    @Test
    void searchStorages_shouldReturnPageWithResults() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<StorageEntity> page = new PageImpl<>(List.of(storageEntityTxt), pageable, 1);
        when(storageRepository.search("keyword", pageable)).thenReturn(page);
        Page<StorageEntity> result = storageService.searchStorages(0, 10, "createdAt", Sort.Direction.DESC, "keyword");
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst()).isEqualTo(storageEntityTxt);
        verify(storageRepository).search("keyword", pageable);
    }

    @Test
    void searchStorages_shouldReturnEmptyPageWhenNoResults() {
        Pageable pageable = PageRequest.of(1, 5, Sort.by(Sort.Direction.ASC, "name"));
        Page<StorageEntity> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(storageRepository.search(null, pageable)).thenReturn(emptyPage);
        Page<StorageEntity> result = storageService.searchStorages(1, 5, "name", Sort.Direction.ASC, null);
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
        verify(storageRepository).search(null, pageable);
    }
    @Test
    void syncStorage_withNonEmptyStorageEntities_shouldProcessOrphans() {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(principal);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
        SecurityContextHolder.setContext(securityContext);
        when(storageRepository.findAll()).thenReturn(List.of(storageEntityTxt));
        UUID orphanId = UUID.randomUUID();
        String orphanFile = orphanId + ".jpg";
        doReturn(List.of(orphanFile)).when(storageService).getStorageBucketFiles();
        storageService.syncStorage();
        verify(storageRepository).saveAll(anyList());
        verify(settingService).updateSync(eq(AppUtils.Constants.STORAGE_SYNCED_AT), anyString());
    }

    @Test
    void getLastSyncTime_shouldReturnSettingValue() {
        SettingEntity settingEntity = SettingEntity.builder()
                .settingName(AppUtils.Constants.STORAGE_SYNCED_AT)
                .settingValue("2025-01-01T10:00:00")
                .build();
        when(settingService.getSettingBySettingName(AppUtils.Constants.STORAGE_SYNCED_AT))
                .thenReturn(settingEntity);
        String result = storageService.getLastSyncTime();
        assertThat(result).isEqualTo("2025-01-01T10:00:00");
        verify(settingService).getSettingBySettingName(AppUtils.Constants.STORAGE_SYNCED_AT);
    }
    @Test
    void syncStorage_shouldHandleBucketFileWithInvalidFormat_elseBranch() {
        when(storageRepository.findAll()).thenReturn(List.of(storageEntityTxt));
        String invalidFile = "invalid-file-name";
        doReturn(List.of(invalidFile)).when(storageService).getStorageBucketFiles();
        UserEntity userEntity = UserEntity.builder()
                .userId(UUID.randomUUID())
                .orgId(UUID.randomUUID()).build();
        UserPrincipalEntity principal = new UserPrincipalEntity(userEntity);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(principal);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
        storageService.syncStorage();
        verify(storageRepository).insertStorage(any(UUID.class), eq(""), eq(""), eq(0L),
                eq(AppUtils.Constants.STORAGE_BUCKET_ORPHANED), any(UUID.class));
        verify(settingService).updateSync(eq(AppUtils.Constants.STORAGE_SYNCED_AT), anyString());
    }

}
