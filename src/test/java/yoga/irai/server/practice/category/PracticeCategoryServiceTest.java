package yoga.irai.server.practice.category;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.exception.AppException;
import yoga.irai.server.mobile.dto.PracticeCategoryListDto;
import yoga.irai.server.storage.StorageService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PracticeCategoryServiceTest {

    @Mock
    private StorageService storageService;

    @Mock
    private PracticeCategoryRepository practiceCategoryRepository;

    @InjectMocks
    private PracticeCategoryService practiceCategoryService;

    private UUID practiceCategoryId;
    private UUID practiceCategoryIconStorageId;
    private UUID updatedPracticeCategoryIconStorageId;
    private String updatedPracticeCategoryIconExternalUrl;
    private PracticeCategoryEntity practiceCategoryEntity;
    private PracticeCategoryRequestDto practiceCategoryRequestDto;

    @BeforeEach
    void setUp() {
        practiceCategoryId = UUID.randomUUID();
        practiceCategoryIconStorageId = UUID.randomUUID();
        updatedPracticeCategoryIconStorageId = UUID.randomUUID();
        updatedPracticeCategoryIconExternalUrl = "https://test.test/iconUpdated";
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
    void testAddPracticeCategory() {
        when(practiceCategoryRepository.existsByPracticeCategoryName(anyString())).thenReturn(Boolean.FALSE);
        when(practiceCategoryRepository.save(any(PracticeCategoryEntity.class))).thenReturn(practiceCategoryEntity);
        PracticeCategoryEntity entity = practiceCategoryService.addPracticeCategory(practiceCategoryRequestDto);
        verify(practiceCategoryRepository, times(1)).existsByPracticeCategoryName(anyString());
        verify(practiceCategoryRepository, times(1)).save(any(PracticeCategoryEntity.class));
        assert entity != null;
    }

    @Test
    void testAddPracticeCategory_NameExists() {
        when(practiceCategoryRepository.existsByPracticeCategoryName(anyString())).thenReturn(Boolean.TRUE);
        AppException ex = assertThrows(AppException.class, () ->
                practiceCategoryService.addPracticeCategory(practiceCategoryRequestDto));
        assertEquals(AppUtils.Messages.NAME_EXISTS.getMessage(), ex.getMessage());
        verify(practiceCategoryRepository, never()).save(any(PracticeCategoryEntity.class));
    }

    @Test
    void testUpdatePractice_Success() {
        when(practiceCategoryRepository.save(any(PracticeCategoryEntity.class))).thenReturn(practiceCategoryEntity);
        when(practiceCategoryRepository.findById(practiceCategoryId)).thenReturn(Optional.of(practiceCategoryEntity));
        PracticeCategoryEntity practice = practiceCategoryService.updatePracticeCategory(practiceCategoryId, practiceCategoryRequestDto);
        verify(practiceCategoryRepository, times(1)).save(any(PracticeCategoryEntity.class));
        assert practice != null;
        assert practice.getPracticeCategoryId().equals(practiceCategoryId);
    }

    @Test
    void testUpdatePractice_Success_IconStorageId_NoChange() {
        practiceCategoryRequestDto.setPracticeCategoryIconStorageId(practiceCategoryIconStorageId);
        when(practiceCategoryRepository.save(any(PracticeCategoryEntity.class))).thenReturn(practiceCategoryEntity);
        when(practiceCategoryRepository.findById(practiceCategoryId)).thenReturn(Optional.of(practiceCategoryEntity));
        doNothing().when(storageService).deleteStorageById(any(UUID.class));
        PracticeCategoryEntity practice = practiceCategoryService.updatePracticeCategory(practiceCategoryId, practiceCategoryRequestDto);
        verify(practiceCategoryRepository, times(1)).save(any(PracticeCategoryEntity.class));
        assert practice != null;
        assert practice.getPracticeCategoryIconStorageId().equals(practiceCategoryIconStorageId);
    }

    @Test
    void testUpdatePractice_Success_IconStorageId() {
        practiceCategoryRequestDto.setPracticeCategoryIconStorageId(updatedPracticeCategoryIconStorageId);
        when(practiceCategoryRepository.save(any(PracticeCategoryEntity.class))).thenReturn(practiceCategoryEntity);
        when(practiceCategoryRepository.findById(practiceCategoryId)).thenReturn(Optional.of(practiceCategoryEntity));
        doNothing().when(storageService).deleteStorageById(any(UUID.class));
        PracticeCategoryEntity practice = practiceCategoryService.updatePracticeCategory(practiceCategoryId, practiceCategoryRequestDto);
        verify(practiceCategoryRepository, times(1)).save(any(PracticeCategoryEntity.class));
        assert practice != null;
        assert practice.getPracticeCategoryIconStorageId().equals(updatedPracticeCategoryIconStorageId);
    }

    @Test
    void testUpdatePractice_Success_IconStorageUrl_NoChange() {
        practiceCategoryRequestDto.setPracticeCategoryIconExternalUrl(updatedPracticeCategoryIconExternalUrl);
        practiceCategoryRequestDto.setPracticeCategoryIconStorageId(null);
        practiceCategoryEntity.setPracticeCategoryIconExternalUrl(updatedPracticeCategoryIconExternalUrl);
        when(practiceCategoryRepository.save(any(PracticeCategoryEntity.class))).thenReturn(practiceCategoryEntity);
        when(practiceCategoryRepository.findById(practiceCategoryId)).thenReturn(Optional.of(practiceCategoryEntity));
        PracticeCategoryEntity practice = practiceCategoryService.updatePracticeCategory(practiceCategoryId, practiceCategoryRequestDto);

        verify(practiceCategoryRepository, times(1)).save(any(PracticeCategoryEntity.class));
        assert practice != null;
        assert practice.getPracticeCategoryIconExternalUrl().equals(updatedPracticeCategoryIconExternalUrl);
    }

    @Test
    void testUpdatePractice_Success_IconStorageUrl() {
        practiceCategoryRequestDto.setPracticeCategoryIconExternalUrl(updatedPracticeCategoryIconExternalUrl);
        practiceCategoryRequestDto.setPracticeCategoryIconStorageId(null);
        when(practiceCategoryRepository.save(any(PracticeCategoryEntity.class))).thenReturn(practiceCategoryEntity);
        when(practiceCategoryRepository.findById(practiceCategoryId)).thenReturn(Optional.of(practiceCategoryEntity));
        PracticeCategoryEntity practice = practiceCategoryService.updatePracticeCategory(practiceCategoryId, practiceCategoryRequestDto);

        verify(practiceCategoryRepository, times(1)).save(any(PracticeCategoryEntity.class));
        assert practice != null;
        assert practice.getPracticeCategoryIconExternalUrl().equals(updatedPracticeCategoryIconExternalUrl);
    }

    @Test
    void testGetPracticeCategoryNameIds(){
        when(practiceCategoryRepository.getPracticeCategoryEntityByPracticeCategoryId(any(UUID.class))).thenReturn(practiceCategoryEntity);
        String practiceCategoryName = practiceCategoryService.getPracticeCategoryNameById(practiceCategoryId);
        verify(practiceCategoryRepository, times(1)).getPracticeCategoryEntityByPracticeCategoryId(any(UUID.class));
        assert practiceCategoryName != null;
    }

    @Test
    void testGetPracticeCategories_withoutKeyword(){
        Page<PracticeCategoryEntity> mockPage =new PageImpl<>(List.of(practiceCategoryEntity));
        when(practiceCategoryRepository.findAll(any(Pageable.class))).thenReturn(mockPage);
        Page<PracticeCategoryEntity> page = practiceCategoryService.getPracticeCategories(0,10,"CreatedAt", Sort.Direction.ASC,"");
        verify(practiceCategoryRepository, times(1)).findAll(any(Pageable.class));
        assert page != null;
        assert page.getContent().size() == 1;
    }

    @Test
    void testGetPracticeCategories_withKeyword(){
        Page<PracticeCategoryEntity> mockPage =new PageImpl<>(List.of(practiceCategoryEntity));
        when(practiceCategoryRepository.search(anyString(),any(Pageable.class))).thenReturn(mockPage);
        Page<PracticeCategoryEntity> page = practiceCategoryService.getPracticeCategories(0,10,"CreatedAt", Sort.Direction.ASC,"keyword");
        verify(practiceCategoryRepository, times(1)).search(anyString(),any(Pageable.class));
        assert page != null;
        assert page.getContent().size() == 1;
    }

    @Test
    void testGetPracticeCategoryDropdown_withoutKeyword(){
        PracticeCategoryDropdownDto mockDto = mock(PracticeCategoryDropdownDto.class);
        Page<PracticeCategoryDropdownDto> mockPage =new PageImpl<>(List.of(mockDto));
        when(practiceCategoryRepository.getDropdown(any(Pageable.class))).thenReturn(mockPage);
        Page<PracticeCategoryDropdownDto> page = practiceCategoryService.getPracticeCategoryDropdown(0,10,"CreatedAt", Sort.Direction.ASC,"");
        verify(practiceCategoryRepository, times(1)).getDropdown(any(Pageable.class));
        assert page != null;
        assert page.getContent().size() == 1;
    }

    @Test
    void testGetPracticeCategoryDropdown_withKeyword(){
        PracticeCategoryDropdownDto mockDto = mock(PracticeCategoryDropdownDto.class);
        Page<PracticeCategoryDropdownDto> mockPage =new PageImpl<>(List.of(mockDto));
        when(practiceCategoryRepository.getDropdownSearch(anyString(),any(Pageable.class))).thenReturn(mockPage);
        Page<PracticeCategoryDropdownDto> page = practiceCategoryService.getPracticeCategoryDropdown(0,10,"CreatedAt", Sort.Direction.ASC,"keyword");
        verify(practiceCategoryRepository, times(1)).getDropdownSearch(anyString(),any(Pageable.class));
        assert page != null;
        assert page.getContent().size() == 1;
    }

    @Test
    void testGetPracticeCategoryList(){
        PracticeCategoryListDto mockDto = mock(PracticeCategoryListDto.class);
        when(practiceCategoryRepository.getPracticeCategoryList()).thenReturn(List.of(mockDto));
        practiceCategoryService.getPracticeCategoryList();
        verify(practiceCategoryRepository, times(1)).getPracticeCategoryList();
    }

    @Test
    void testUpdatePracticeCategoryStatus(){
        when(practiceCategoryRepository.findById(any(UUID.class))).thenReturn(Optional.of(practiceCategoryEntity));
        practiceCategoryService.updatePracticeCategoryStatus(practiceCategoryId,AppUtils.PracticeCategoryStatus.ACTIVE);
        verify(practiceCategoryRepository,times(1)).save(any(PracticeCategoryEntity.class));
    }

    @Test
    void testGetCategoryNameIdByIds() {
        PracticeCategoryDropdownDto mockDto = mock(PracticeCategoryDropdownDto.class);
        when(mockDto.getPracticeCategoryId()).thenReturn(practiceCategoryId);
        when(mockDto.getPracticeCategoryName()).thenReturn("Algorithms");
        when(practiceCategoryRepository.findPracticeCategoryIconStorageIdByPracticeCategoryIdIn(anyList())).thenReturn(List.of(mockDto));
        Map<UUID,String> categoryMap = practiceCategoryService.getCategoryNameIdByIds(List.of(practiceCategoryId));
        verify(practiceCategoryRepository,times(1)).findPracticeCategoryIconStorageIdByPracticeCategoryIdIn(anyList());
        assert categoryMap != null;
        assert categoryMap.size() == 1;
    }

    @Test
    void testDeletePracticeCategory(){
        when(practiceCategoryRepository.findById(any(UUID.class))).thenReturn(Optional.of(practiceCategoryEntity));
        doNothing().when(storageService).deleteStorageByIds(anySet());
        doNothing().when(practiceCategoryRepository).delete(any(PracticeCategoryEntity.class));
        practiceCategoryService.deletePracticeCategory(practiceCategoryId);
        verify(storageService,times(1)).deleteStorageByIds(anySet());
        verify(practiceCategoryRepository,times(1)).delete(any(PracticeCategoryEntity.class));
    }

    @Test
    void testDeletePracticeCategory_noStorageIds(){
        practiceCategoryEntity.setPracticeCategoryIconStorageId(null);
        when(practiceCategoryRepository.findById(any(UUID.class))).thenReturn(Optional.of(practiceCategoryEntity));
        doNothing().when(practiceCategoryRepository).delete(any(PracticeCategoryEntity.class));
        practiceCategoryService.deletePracticeCategory(practiceCategoryId);
        verify(practiceCategoryRepository,times(1)).delete(any(PracticeCategoryEntity.class));
    }
}
