package yoga.irai.server.storage;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.dto.AppResponseDto;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageControllerTest {
    @Mock
    private StorageService storageService;

    @InjectMocks
    private StorageController storageController;
    private StorageRequestDto storageRequestDto;

    @BeforeEach
    void setUp() {
        storageRequestDto = StorageRequestDto.builder().build();
        storageController = new StorageController(storageService);
        ReflectionTestUtils.setField(storageController, "appDefaultStorageFiles", "default.txt,sample.png");
    }

    @Test
    void testUploadStorage() throws IOException {
        UUID storageId = UUID.randomUUID();
        StorageEntity entity = new StorageEntity();
        entity.setStorageId(storageId);

        when(storageService.uploadStorage(any(StorageRequestDto.class)))
                .thenReturn(entity);

        ResponseEntity<AppResponseDto<StorageResponseDto>> response =
                storageController.uploadStorage(storageRequestDto);
        assert response.getStatusCode() == HttpStatus.OK;

        Assertions.assertNotNull(response.getBody());
        assertEquals(storageId, response.getBody().getData().getStorageId());
        assertTrue(response.getBody().getMessage().contains(storageId.toString()));

        verify(storageService, times(1)).uploadStorage(storageRequestDto);
    }

    @Test
    void testDeleteStorage() {
        String storageName = "sample.txt";
        ReflectionTestUtils.setField(storageController, "appDefaultStorageFiles", "default.txt,other.txt");

        doNothing().when(storageService).deleteStorage(storageName);

        ResponseEntity<AppResponseDto<Void>> response =
                storageController.deleteStorage(storageName);
        assert response.getStatusCode() == HttpStatus.OK;
        Assertions.assertNotNull(response.getBody());
        assertEquals(AppUtils.Messages.STORAGE_FILE_DELETED_SUCCESS_1_NAME.getMessage(storageName),
                response.getBody().getMessage());

        verify(storageService, times(1)).deleteStorage(storageName);
    }
    @Test
    void testDeleteStorage_DefaultFile_ThrowsException() {
        String storageName = "default1.txt";

        // inject app default files that contain storageName
        ReflectionTestUtils.setField(storageController, "appDefaultStorageFiles", "default1.txt,default2.png");

        Exception exception = assertThrows(
                RuntimeException.class,
                () -> storageController.deleteStorage(storageName)
        );

        assertTrue(exception.getMessage().contains(storageName));
        verify(storageService, never()).deleteStorage(any());
    }
    @Test
    void testSearchStorages() {
        StorageEntity entity = new StorageEntity();
        entity.setStorageId(UUID.randomUUID());

        when(storageService.searchStorages(anyInt(), anyInt(), anyString(), any(Sort.Direction.class), any()))
                .thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1));

        when(storageService.toStorageResponse(anyList()))
                .thenReturn(List.of(StorageResponseDto.builder().storageId(entity.getStorageId()).build()));

        ResponseEntity<AppResponseDto<List<StorageResponseDto>>> response =
                storageController.searchStorages(0, 10, "createdAt", Sort.Direction.DESC, null);
        assert response.getStatusCode() == HttpStatus.OK;

        Assertions.assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getData().size());
        assertEquals(entity.getStorageId(), response.getBody().getData().getFirst().getStorageId());
        assertEquals(AppUtils.Messages.STORAGE_SEARCHES_FOUND.getMessage(),
                response.getBody().getMessage());

        verify(storageService, times(1)).searchStorages(0, 10, "createdAt", Sort.Direction.DESC, null);
    }

    @Test
    void testSyncStorage() {
        doNothing().when(storageService).syncStorage();

        ResponseEntity<AppResponseDto<Void>> response = storageController.syncStorage();
        assert response.getStatusCode() == HttpStatus.OK;

        Assertions.assertNotNull(response.getBody());
        assertEquals(AppUtils.Messages.STORAGE_SYNCED_SUCCESS.getMessage(),
                response.getBody().getMessage());

        verify(storageService, times(1)).syncStorage();
    }

    @Test
    void testGetLastSyncTime() {
        String lastSyncTime = "2025-09-29T10:15:30";
        when(storageService.getLastSyncTime()).thenReturn(lastSyncTime);

        ResponseEntity<AppResponseDto<String>> response = storageController.getLastSyncTime();
        assert response.getStatusCode() == HttpStatus.OK;

        Assertions.assertNotNull(response.getBody());
        assertEquals(lastSyncTime, response.getBody().getData());

        verify(storageService, times(1)).getLastSyncTime();
    }
}
