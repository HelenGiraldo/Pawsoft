package co.edu.uniquindio.backendpawsoft.dto;

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