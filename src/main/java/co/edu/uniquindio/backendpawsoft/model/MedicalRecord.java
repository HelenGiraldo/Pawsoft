package co.edu.uniquindio.backendpawsoft.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa el registro médico de una consulta veterinaria.
 *
 * Almacena el examen físico, diagnóstico, tratamiento, medicamentos,
 * vacunas aplicadas y el próximo control programado.
 *
 * Proyecto: Pawsoft — Software III
 * Autoras: Valentina Porras · Helen Xiomara Giraldo
 */
@Entity
@Table(name = "medical_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Cita asociada a este registro médico. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    /** Mascota atendida. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    /** Veterinario que realizó la consulta. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vet_id", nullable = false)
    private User vet;

    // ── Examen físico ──────────────────────────────────────────────────────
    private Double peso;
    private Double temperatura;
    private Integer frecuenciaCardiaca;
    private Integer frecuenciaRespiratoria;

    @Column(length = 1000)
    private String observacionesGenerales;

    // ── Diagnóstico ────────────────────────────────────────────────────────
    @Column(length = 500)
    private String diagnosticoPrincipal;

    @Column(length = 500)
    private String diagnosticoSecundario;

    @Column(length = 1000)
    private String notasClinicas;

    // ── Tratamiento ────────────────────────────────────────────────────────
    /** JSON serializado de medicamentos usados durante el procedimiento (solo vet). */
    @Column(columnDefinition = "TEXT")
    private String medicamentos;

    @Column(length = 1000)
    private String indicaciones;

    // ── Resumen para el cliente ────────────────────────────────────────────
    /** Diagnóstico en lenguaje simple para el propietario. */
    @Column(length = 500)
    private String diagnosticoCliente;

    /** JSON serializado de medicamentos recetados para tratar en casa. */
    @Column(columnDefinition = "TEXT")
    private String medicamentosRecetados;

    @Column(length = 1000)
    private String indicacionesCliente;

    // ── Vacunas y controles ────────────────────────────────────────────────
    /** JSON serializado de las vacunas aplicadas. */
    @Column(columnDefinition = "TEXT")
    private String vacunasAplicadas;

    private LocalDate proximoControlFecha;

    @Column(length = 300)
    private String proximoControlMotivo;

    // ── Antecedentes (prellenado de hoja maestra, editable) ────────────────
    /** Alergias detectadas en ESTA consulta. */
    @Column(columnDefinition = "TEXT")
    private String allergiesFound;

    /** Condiciones detectadas en ESTA consulta. */
    @Column(columnDefinition = "TEXT")
    private String conditionsFound;

    /** Antecedentes quirúrgicos relevantes. */
    @Column(columnDefinition = "TEXT")
    private String surgicalNote;

    /** Medicamentos activos al momento de la consulta. */
    @Column(columnDefinition = "TEXT")
    private String currentMedsNote;

    /** Estado de ánimo / comportamiento de la mascota. */
    @Column(length = 255)
    private String moodBehavior;

    // ── Archivos adjuntos ──────────────────────────────────────────────────
    /** JSON serializado de URLs de fotos (radiografías, análisis, etc.) subidas a Cloudinary. */
    @Column(columnDefinition = "TEXT")
    private String fotosAdjuntas;

    // ── Costos ─────────────────────────────────────────────────────────────
    /** Costo total de los medicamentos aplicados en consulta. */
    @Column(name = "costo_medicamentos", precision = 12, scale = 2)
    private BigDecimal costoMedicamentos;

    /** Precio base del servicio + costo de medicamentos. */
    @Column(name = "costo_total", precision = 12, scale = 2)
    private BigDecimal costoTotal;

    // ── Auditoría ──────────────────────────────────────────────────────────
    @Column(nullable = false)
    private LocalDateTime creadoEn;

    private LocalDateTime actualizadoEn;

    @PrePersist
    protected void onCreate() {
        creadoEn = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        actualizadoEn = LocalDateTime.now();
    }
}