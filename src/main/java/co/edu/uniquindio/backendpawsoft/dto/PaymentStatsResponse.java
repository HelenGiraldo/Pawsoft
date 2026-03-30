package co.edu.uniquindio.backendpawsoft.dto;

/**
 * DTO de respuesta con estadísticas de pagos: totales, ingresos por período
 * y desglose por concepto de servicio. Usado en el panel de administración.
 *
 * Proyecto: Pawsoft
 * Universidad del Quindío — Ingeniería de Sistemas y Computación — Software III
 * Autoras: Valentina Porras Salazar · Helen Xiomara Giraldo Libreros
 * Profesor: Raúl Yulbraynner Rivera Gálvez
 */
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class PaymentStatsResponse {

    private long       totalPaid;
    private long       totalPending;

    /** Ingresos cobrados hoy */
    private BigDecimal revenueToday;

    /** Ingresos cobrados en la semana actual (lun–dom) */
    private BigDecimal revenueThisWeek;

    /** Ingresos cobrados en el mes actual */
    private BigDecimal revenueThisMonth;

    /** Ingresos totales históricos */
    private BigDecimal revenueAllTime;

    /** Ingresos por concepto de servicio en el mes actual */
    private Map<String, BigDecimal> revenueByConceptThisMonth;

    /** Últimos 10 pagos confirmados */
    private List<PaymentResponse> recentPayments;
}