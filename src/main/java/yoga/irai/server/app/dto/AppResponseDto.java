package yoga.irai.server.app.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Map;
import lombok.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

@Data
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppResponseDto<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 3467890123456789L;

    private String message;
    private transient T data;
    private Pageable pageable;
    private String errorMessage;
    private Map<String, String> errors;
    @Builder.Default
    private ZonedDateTime timestamp = ZonedDateTime.now();

    @Data
    @Builder
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Pageable implements Serializable {
        private int pageNumber;
        private int pageSize;
        private int totalPages;
        private long totalElements;
        private String sortBy;
        private String sortDirection;
    }
    public static Pageable buildPageable(Page<?> page, String sortBy, Sort.Direction direction) {
        return AppResponseDto.Pageable.builder().pageNumber(page.getNumber()).pageSize(page.getSize())
                .totalPages(page.getTotalPages()).totalElements(page.getTotalElements()).sortBy(sortBy)
                .sortDirection(direction.name()).build();
    }
}
