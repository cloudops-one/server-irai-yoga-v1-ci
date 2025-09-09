package yoga.irai.server.program.section;

import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;
import lombok.*;
import yoga.irai.server.app.audit.Auditable;

@Data
@Entity
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "program_section")
@EqualsAndHashCode(callSuper = true)
public class SectionEntity extends Auditable implements Serializable {

    @Serial
    private static final long serialVersionUID = 15837197757558426L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "section_id", nullable = false)
    private UUID sectionId;

    @Column(name = "program_id", nullable = false)
    private UUID programId;

    @Column(name = "section_name", nullable = false)
    private String sectionName;

    @Column(name = "section_description", columnDefinition = "text")
    private String sectionDescription;

    @Builder.Default
    @Column(name = "number_of_lessons", nullable = false)
    private Integer numberOfLessons = 0;

    @Column(name = "section_order", nullable = false)
    private Integer sectionOrder;
}
