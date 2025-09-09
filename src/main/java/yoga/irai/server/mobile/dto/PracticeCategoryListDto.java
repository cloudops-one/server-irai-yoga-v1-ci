package yoga.irai.server.mobile.dto;

import java.util.UUID;

public interface PracticeCategoryListDto {
    UUID getPracticeCategoryId();
    String getPracticeCategoryName();
    UUID getPracticeCategoryIconStorageId();
    String getPracticeCategoryIconExternalUrl();
}
