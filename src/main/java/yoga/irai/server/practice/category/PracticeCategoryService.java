package yoga.irai.server.practice.category;

import jakarta.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.exception.AppException;
import yoga.irai.server.mobile.dto.PracticeCategoryListDto;
import yoga.irai.server.storage.StorageService;

/**
 * Service class for managing practice categories.
 */
@Service
@AllArgsConstructor
public class PracticeCategoryService {

    private final PracticeCategoryRepository practiceCategoryRepository;
    private final StorageService storageService;

    /**
     * Adds a new practice category.
     *
     * @param practiceCategoryRequestDto
     *            the request DTO containing category details
     * @return the saved PracticeCategoryEntity
     */
    public PracticeCategoryEntity addPracticeCategory(@Valid PracticeCategoryRequestDto practiceCategoryRequestDto) {
        if (practiceCategoryRepository
                .existsByPracticeCategoryName(practiceCategoryRequestDto.getPracticeCategoryName())) {
            throw new AppException(AppUtils.Messages.NAME_EXISTS.getMessage());
        }
        PracticeCategoryEntity practiceCategoryEntity = AppUtils.map(practiceCategoryRequestDto,
                PracticeCategoryEntity.class);
        practiceCategoryEntity.setPracticeCategoryId(null);
        practiceCategoryEntity
                .setPracticeCategoryIconStorageId(practiceCategoryRequestDto.getPracticeCategoryIconStorageId());
        return practiceCategoryRepository.save(practiceCategoryEntity);
    }

    /**
     * Updates an existing practice category.
     *
     * @param categoryId
     *            the ID of the category to update
     * @param practiceCategoryRequestDto
     *            the request DTO containing updated category details
     * @return the updated PracticeCategoryEntity
     */
    @Transactional
    public PracticeCategoryEntity updatePracticeCategory(UUID categoryId,
            PracticeCategoryRequestDto practiceCategoryRequestDto) {
        PracticeCategoryEntity practiceCategoryEntity = getPracticeCategoryById(categoryId);
        if (ObjectUtils.isEmpty(practiceCategoryEntity.getPracticeCategoryIconExternalUrl())
                && ObjectUtils.isNotEmpty(practiceCategoryRequestDto.getPracticeCategoryIconStorageId())
                && !practiceCategoryRequestDto.getPracticeCategoryIconStorageId()
                        .equals(practiceCategoryEntity.getPracticeCategoryIconStorageId())) {
            storageService.deleteStorageById(practiceCategoryEntity.getPracticeCategoryIconStorageId());
        }
        AppUtils.map(practiceCategoryRequestDto, practiceCategoryEntity);
        practiceCategoryEntity.setPracticeCategoryId(categoryId);

        if (ObjectUtils.isNotEmpty(practiceCategoryRequestDto.getPracticeCategoryIconStorageId())) {
            practiceCategoryEntity
                    .setPracticeCategoryIconStorageId(practiceCategoryRequestDto.getPracticeCategoryIconStorageId());
            practiceCategoryEntity.setPracticeCategoryIconExternalUrl(null);
        } else {
            practiceCategoryEntity.setPracticeCategoryIconStorageId(null);
            practiceCategoryEntity.setPracticeCategoryIconExternalUrl(
                    practiceCategoryRequestDto.getPracticeCategoryIconExternalUrl());
        }

        return practiceCategoryRepository.save(practiceCategoryEntity);

    }

    /**
     * Retrieves a practice category by its ID.
     *
     * @param categoryId
     *            the ID of the category to retrieve
     * @return the PracticeCategoryEntity
     */
    public PracticeCategoryEntity getPracticeCategoryById(UUID categoryId) {
        return practiceCategoryRepository.findById(categoryId)
                .orElseThrow(() -> AppUtils.Messages.PRACTICE_CATEGORY_NOT_FOUND.getException(categoryId));

    }

    /**
     * Retrieves the name of a practice category by its ID.
     *
     * @param categoryId
     *            the ID of the category
     * @return the name of the practice category
     */
    public String getPracticeCategoryNameById(UUID categoryId) {
        return (practiceCategoryRepository.getPracticeCategoryEntityByPracticeCategoryId(categoryId))
                .getPracticeCategoryName();
    }

    /**
     * Retrieves all practice categories with pagination and sorting.
     *
     * @param pageNumber
     *            the page number to retrieve
     * @param pageSize
     *            the number of items per page
     * @param sortBy
     *            the field to sort by
     * @param direction
     *            the direction of sorting (ASC or DESC)
     * @param keyword
     *            optional search keyword for filtering categories
     * @return a Page of PracticeCategoryEntity
     */
    public Page<PracticeCategoryEntity> getPracticeCategories(int pageNumber, int pageSize, String sortBy,
            Sort.Direction direction, String keyword) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortBy));
        Page<PracticeCategoryEntity> practiceCategoryPage;
        if (StringUtils.isEmpty(keyword)) {
            practiceCategoryPage = practiceCategoryRepository.findAll(pageable);
        } else {
            practiceCategoryPage = practiceCategoryRepository.search(keyword, pageable);
        }
        return practiceCategoryPage;
    }

    /**
     * Retrieves a paginated list of practice categories for dropdowns, optionally
     * filtered by a keyword.
     *
     * @param pageNumber
     *            the page number to retrieve
     * @param pageSize
     *            the number of items per page
     * @param sortBy
     *            the field to sort by
     * @param direction
     *            the direction of sorting (ASC or DESC)
     * @param keyword
     *            optional search keyword for filtering categories
     * @return a Page of PracticeCategoryDropdownDto
     */
    public Page<PracticeCategoryDropdownDto> getPracticeCategoryDropdown(int pageNumber, int pageSize, String sortBy,
            Sort.Direction direction, String keyword) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortBy));
        Page<PracticeCategoryDropdownDto> practiceCategoryPage;
        if (StringUtils.isEmpty(keyword)) {
            practiceCategoryPage = practiceCategoryRepository.getDropdown(pageable);
        } else {
            practiceCategoryPage = practiceCategoryRepository.getDropdownSearch(keyword, pageable);
        }
        return practiceCategoryPage;
    }

    /**
     * Retrieves a paginated list of practice categories
     *
     * @return a Page of PracticeCategoryListDto
     */
    public List<PracticeCategoryListDto> getPracticeCategoryList() {
        return practiceCategoryRepository.getPracticeCategoryList();
    }

    /**
     * Deletes a practice category by its ID.
     *
     * @param categoryId
     *            the ID of the category to delete
     */
    public void updatePracticeCategoryStatus(UUID categoryId, AppUtils.PracticeCategoryStatus status) {
        PracticeCategoryEntity practiceCategoryEntity = getPracticeCategoryById(categoryId);
        practiceCategoryEntity.setPracticeCategoryStatus(status);
        practiceCategoryRepository.save(practiceCategoryEntity);
    }

    /**
     * Retrieves a map of category IDs to their names for the given list of category
     * IDs.
     *
     * @param categoryIds
     *            the list of category IDs to retrieve names for
     * @return a map where keys are category IDs and values are category names
     */
    public Map<UUID, String> getCategoryNameIdByIds(List<UUID> categoryIds) {
        return practiceCategoryRepository.findPracticeCategoryIconStorageIdByPracticeCategoryIdIn(categoryIds).stream()
                .collect(Collectors.toMap(PracticeCategoryDropdownDto::getPracticeCategoryId,
                        PracticeCategoryDropdownDto::getPracticeCategoryName));
    }

    /**
     * Deletes a practice category by its ID.
     *
     * @param categoryId
     *            the ID of the category to delete
     */
    public void deletePracticeCategory(UUID categoryId) {
        PracticeCategoryEntity practiceCategoryEntity = getPracticeCategoryById(categoryId);
        Set<UUID> storageIds = Stream.of(practiceCategoryEntity.getPracticeCategoryIconStorageId())
                .filter(Objects::nonNull).collect(Collectors.toSet());
        if (ObjectUtils.isNotEmpty(storageIds)) {
            storageService.deleteStorageByIds(storageIds);
        }
        practiceCategoryRepository.delete(getPracticeCategoryById(categoryId));
    }
}
