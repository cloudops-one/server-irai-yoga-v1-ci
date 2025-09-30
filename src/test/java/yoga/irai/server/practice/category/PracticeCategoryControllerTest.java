package yoga.irai.server.practice.category;

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
import yoga.irai.server.authentication.service.UserService;
import yoga.irai.server.storage.StorageService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PracticeCategoryControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private StorageService storageService;

    @Mock
    private PracticeCategoryService practiceCategoryService;

    @InjectMocks
    private PracticeCategoryController practiceCategoryController;

    private UUID practiceCategoryId;
    private PracticeCategoryEntity practiceCategoryEntity;
    private PracticeCategoryRequestDto practiceCategoryRequestDto;

    @BeforeEach
    void setUp() {
        practiceCategoryId = UUID.randomUUID();
        practiceCategoryEntity = PracticeCategoryEntity.builder()
                .practiceCategoryName("Test Practice Category")
                .practiceCategoryId(practiceCategoryId)
                .practiceCategoryIconStorageId(UUID.randomUUID())
                .build();
        practiceCategoryEntity.setCreatedBy(UUID.randomUUID());
        practiceCategoryEntity.setUpdatedBy(UUID.randomUUID());
        practiceCategoryRequestDto = PracticeCategoryRequestDto.builder()
                .practiceCategoryName(practiceCategoryEntity.getPracticeCategoryName())
                .practiceCategoryIconStorageId(practiceCategoryEntity.getPracticeCategoryIconStorageId())
                .build();
    }

    @Test
    void testAddPractice() {
        when(practiceCategoryService.addPracticeCategory(any(PracticeCategoryRequestDto.class))).thenReturn(practiceCategoryEntity);
        when(userService.getUserNameById(any(UUID.class))).thenReturn("userName");
        when(storageService.getStorageUrl(any(UUID.class))).thenReturn("https://test.test/file");
        ResponseEntity<AppResponseDto<PracticeCategoryResponseDto>> response = practiceCategoryController.addPracticeCategory(practiceCategoryRequestDto);
        verify(practiceCategoryService, times(1)).addPracticeCategory(any(PracticeCategoryRequestDto.class));
        assert response.getStatusCode() == HttpStatus.OK;
    }

    @Test
    void testAddPractice_ExternalUrl() {
        practiceCategoryRequestDto.setPracticeCategoryIconStorageId(null);
        practiceCategoryRequestDto.setPracticeCategoryIconExternalUrl("https://test.test/file");
        when(practiceCategoryService.addPracticeCategory(any(PracticeCategoryRequestDto.class))).thenReturn(practiceCategoryEntity);
        when(userService.getUserNameById(any(UUID.class))).thenReturn("userName");
        when(storageService.getStorageUrl(any(UUID.class))).thenReturn("https://test.test/file");
        ResponseEntity<AppResponseDto<PracticeCategoryResponseDto>> response = practiceCategoryController.addPracticeCategory(practiceCategoryRequestDto);
        verify(practiceCategoryService, times(1)).addPracticeCategory(any(PracticeCategoryRequestDto.class));
        assert response.getStatusCode() == HttpStatus.OK;
    }

    @Test
    void testUpdatePractice() {
        when(practiceCategoryService.updatePracticeCategory(any(UUID.class), any(PracticeCategoryRequestDto.class))).thenReturn(practiceCategoryEntity);
        when(userService.getUserNameById(any(UUID.class))).thenReturn("userName");
        when(storageService.getStorageUrl(any(UUID.class))).thenReturn("https://test.test/file");
        ResponseEntity<AppResponseDto<PracticeCategoryResponseDto>> response = practiceCategoryController.updatePracticeCategory(practiceCategoryId, practiceCategoryRequestDto);
        verify(practiceCategoryService, times(1)).updatePracticeCategory(any(UUID.class), any(PracticeCategoryRequestDto.class));
        assert response.getStatusCode() == HttpStatus.OK;
    }

    @Test
    void testGetPoemById() {
        when(practiceCategoryService.getPracticeCategoryById(any(UUID.class))).thenReturn(practiceCategoryEntity);
        when(userService.getUserNameById(any(UUID.class))).thenReturn("userName");
        when(storageService.getStorageUrl(any(UUID.class))).thenReturn("https://test.test/file");
        ResponseEntity<AppResponseDto<PracticeCategoryResponseDto>> response = practiceCategoryController.getPracticeCategoryById(practiceCategoryId);
        verify(practiceCategoryService, times(1)).getPracticeCategoryById(any(UUID.class));
        assert response.getStatusCode() == HttpStatus.OK;
    }

    @Test
    void testSearchPractice() {
        Page<PracticeCategoryEntity> practicePage = new PageImpl<>(List.of(practiceCategoryEntity));
        when(practiceCategoryService.getPracticeCategories(anyInt(), anyInt(), anyString(), any(), anyString())).thenReturn(practicePage);
        when(userService.getUserNamesByIds(anyList())).thenReturn(Map.of(practiceCategoryEntity.getCreatedBy(), "userName"));
        when(storageService.getSignedStorageUrlByIds(anyList())).thenReturn(Map.of(practiceCategoryEntity.getPracticeCategoryIconStorageId(), "https://test.test/file"));
        ResponseEntity<AppResponseDto<List<PracticeCategoryResponseDto>>> response = practiceCategoryController.getPracticeCategories(0, 10, "", Sort.Direction.ASC, "");
        verify(practiceCategoryService, times(1)).getPracticeCategories(anyInt(), anyInt(), anyString(), any(), anyString());
        assert response.getStatusCode() == HttpStatus.OK;
        assert response.getBody() != null;
        assert response.getBody().getData().size() == 1;
    }

    @Test
    void testSearchPractice_NoIconStorageId() {
        practiceCategoryEntity.setPracticeCategoryIconStorageId(null);
        practiceCategoryEntity.setCreatedBy(null);
        practiceCategoryEntity.setUpdatedBy(null);
        Page<PracticeCategoryEntity> practicePage = new PageImpl<>(List.of(practiceCategoryEntity));
        when(practiceCategoryService.getPracticeCategories(anyInt(), anyInt(), anyString(), any(), anyString())).thenReturn(practicePage);
        ResponseEntity<AppResponseDto<List<PracticeCategoryResponseDto>>> response = practiceCategoryController.getPracticeCategories(0, 10, "", Sort.Direction.ASC, "");
        verify(practiceCategoryService, times(1)).getPracticeCategories(anyInt(), anyInt(), anyString(), any(), anyString());
        assert response.getStatusCode() == HttpStatus.OK;
        assert response.getBody() != null;
        assert response.getBody().getData().size() == 1;
    }

    @Test
    void testDeletePractice() {
        doNothing().when(practiceCategoryService).deletePracticeCategory(any(UUID.class));
        ResponseEntity<AppResponseDto<Void>> response = practiceCategoryController.deletePracticeCategory(practiceCategoryId);
        verify(practiceCategoryService, times(1)).deletePracticeCategory(any(UUID.class));
        assert response.getStatusCode() == HttpStatus.OK;
        assert response.getBody() != null;
        assert response.getBody().getMessage().equals(AppUtils.Messages.DELETE_SUCCESS.getMessage());
    }

    @Test
    void testGetPracticeCategoriesDropDown(){
        PracticeCategoryDropdownDto mockDto = mock(PracticeCategoryDropdownDto.class);
        Page<PracticeCategoryDropdownDto> practiceCategoryPage = new PageImpl<>(List.of(mockDto));
        when(practiceCategoryService.getPracticeCategoryDropdown(anyInt(),anyInt(),anyString(),any(),anyString())).thenReturn(practiceCategoryPage);
        ResponseEntity<AppResponseDto<List<PracticeCategoryDropdownDto>>> response = practiceCategoryController.getPracticeCategoryDropdown(0,10,"",Sort.Direction.ASC,"");
        verify(practiceCategoryService,times(1)).getPracticeCategoryDropdown(anyInt(), anyInt(), anyString(), any(), anyString());
        assert response.getStatusCode() == HttpStatus.OK;
        assert response.getBody() != null;
        assert response.getBody().getData().size() == 1;
    }

    @Test
    void testChangeNewStatus() {
        doNothing().when(practiceCategoryService).updatePracticeCategoryStatus(any(UUID.class), any(AppUtils.PracticeCategoryStatus.class));
        ResponseEntity<AppResponseDto<Void>> response = practiceCategoryController.updatePracticeCategoryStatus(practiceCategoryId, AppUtils.PracticeCategoryStatus.ACTIVE);
        verify(practiceCategoryService, times(1)).updatePracticeCategoryStatus(any(UUID.class), any(AppUtils.PracticeCategoryStatus.class));
        assert response.getStatusCode() == HttpStatus.OK;
        assert response.getBody() != null;
        assert response.getBody().getMessage().equals(AppUtils.Messages.UPDATE_SUCCESS.getMessage());
    }
}
