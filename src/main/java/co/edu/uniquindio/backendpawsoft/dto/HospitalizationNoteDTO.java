package co.edu.uniquindio.backendpawsoft.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para notas de evolución de hospitalizaciones.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HospitalizationNoteDTO {

    private Long id;

    private Long hospitalizationId;

    private Long vetId;

    private String vetName;

    private String note;

    private LocalDateTime createdAt;
}
