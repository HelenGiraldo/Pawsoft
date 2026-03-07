//package co.edu.uniquindio.backendpawsoft.config;
//
//import co.edu.uniquindio.backendpawsoft.model.ServicePrice;
//import co.edu.uniquindio.backendpawsoft.repository.ServicePriceRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.stereotype.Component;
//
//import java.math.BigDecimal;
//import java.util.List;
//
///**
// * Carga los precios de servicios por defecto al arrancar la aplicación.
// *
// * Solo inserta si la tabla está vacía — nunca sobreescribe precios
// * que el admin ya haya modificado.
// *
// * Para cambiar los precios iniciales, modifica la lista {@code defaults}
// * o actualízalos directamente desde el panel de administración.
// */
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class ServicePriceInitializer implements ApplicationRunner {
//
//    private final ServicePriceRepository servicePriceRepository;
//
//    @Override
//    public void run(ApplicationArguments args) {
//        if (servicePriceRepository.count() > 0) {
//            return; // ya hay precios configurados, no tocar
//        }
//
//        log.info("Cargando precios de servicios por defecto...");
//
//        List<ServicePrice> defaults = List.of(
//                ServicePrice.builder()
//                        .serviceType("Consulta general")
//                        .displayName("Consulta general")
//                        .price(new BigDecimal("50000"))
//                        .description("Revisión general del estado de salud de la mascota")
//                        .active(true).build(),
//
//                ServicePrice.builder()
//                        .serviceType("Vacunación")
//                        .displayName("Vacunación")
//                        .price(new BigDecimal("35000"))
//                        .description("Aplicación de vacuna según esquema de vacunación")
//                        .active(true).build(),
//
//                ServicePrice.builder()
//                        .serviceType("Desparasitación")
//                        .displayName("Desparasitación")
//                        .price(new BigDecimal("30000"))
//                        .description("Tratamiento antiparasitario interno y/o externo")
//                        .active(true).build(),
//
//                ServicePrice.builder()
//                        .serviceType("Revisión")
//                        .displayName("Revisión / Control")
//                        .price(new BigDecimal("40000"))
//                        .description("Seguimiento o control de tratamiento anterior")
//                        .active(true).build(),
//
//                ServicePrice.builder()
//                        .serviceType("Cirugía")
//                        .displayName("Cirugía")
//                        .price(new BigDecimal("250000"))
//                        .description("Procedimiento quirúrgico (precio base, puede variar)")
//                        .active(true).build(),
//
//                ServicePrice.builder()
//                        .serviceType("Urgencia")
//                        .displayName("Urgencia")
//                        .price(new BigDecimal("80000"))
//                        .description("Atención de emergencia fuera de horario o urgente")
//                        .active(true).build(),
//
//                ServicePrice.builder()
//                        .serviceType("Baño y grooming")
//                        .displayName("Baño y grooming")
//                        .price(new BigDecimal("45000"))
//                        .description("Baño, corte de pelo y arreglo estético")
//                        .active(true).build(),
//
//                ServicePrice.builder()
//                        .serviceType("Otro")
//                        .displayName("Otro servicio")
//                        .price(new BigDecimal("0"))
//                        .description("Servicio no listado — la recepcionista define el monto")
//                        .active(true).build()
//        );
//
//        servicePriceRepository.saveAll(defaults);
//        log.info("{} precios de servicios cargados correctamente.", defaults.size());
//    }
//}