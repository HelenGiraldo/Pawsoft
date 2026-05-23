package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.enums.AppointmentStatus;
import co.edu.uniquindio.backendpawsoft.enums.PaymentStatus;
import co.edu.uniquindio.backendpawsoft.enums.Role;
import co.edu.uniquindio.backendpawsoft.model.*;
import co.edu.uniquindio.backendpawsoft.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Componente encargado de inicializar datos básicos del sistema al arrancar la aplicación.
 *
 * Su función principal es garantizar la existencia de un usuario administrador por defecto
 * en la base de datos, evitando su duplicación si ya existe un registro con el correo definido.
 *
 * Este componente se ejecuta automáticamente al iniciar el proyecto gracias a la interfaz
 * {@link CommandLineRunner}.
 *
 * Proyecto: Pawsoft
 * Universidad del Quindío
 * Materia: Software III
 *
 * Autoras:
 * - Valentina Porras Salazar
 * - Helen Xiomara Giraldo Libreros
 *
 * Profesor:
 * Raúl Yulbraynner Rivera Gálvez
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    /**
     * Repositorio de usuarios utilizado para consultar y crear el usuario administrador.
     */
    private final UserRepository userRepository;

    /**
     * Codificador de contraseñas para almacenar el hash de la contraseña del administrador.
     */
    private final PasswordEncoder passwordEncoder;

    private final MedicationCatalogRepository medicationCatalogRepository;

    private final PetRepository petRepository;

    private final AppointmentRepository appointmentRepository;

    private final MedicalRecordRepository medicalRecordRepository;

    private final PaymentRepository paymentRepository;

    /** Correo del admin — se lee desde application.properties (no hardcodeado en código). */
    @Value("${app.admin.email}")
    private String adminEmail;

    /** Contraseña inicial del admin — se lee desde application.properties. */
    @Value("${app.admin.password}")
    private String adminPassword;

    /**
     * Método ejecutado al iniciar la aplicación.
     *
     * Verifica si ya existe el usuario administrador por correo:
     * - Si NO existe: Lo crea con enabled = true
     * - Si existe pero está deshabilitado: Lo habilita automáticamente
     * - Si existe y está habilitado: No hace nada
     *
     * Esto garantiza que siempre haya un admin funcional al arrancar el servidor.
     *
     * @param args argumentos de línea de comandos (no se usan en esta implementación)
     */
    @Override
    public void run(String... args) {

        var existingAdmin = userRepository.findByEmail(adminEmail);

        if (existingAdmin.isEmpty()) {
            User admin = User.builder()
                    .name("Administrador")
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ROLE_ADMIN)
                    .primerAcceso(false)
                    .enabled(true)
                    .build();

            userRepository.save(admin);
            System.out.println("✅ Usuario ADMIN creado: " + adminEmail);
        } else {
            User admin = existingAdmin.get();

            if (!admin.isEnabled()) {
                admin.setEnabled(true);
                userRepository.save(admin);
                System.out.println("⚠️ Usuario ADMIN estaba deshabilitado - HABILITADO automáticamente: " + adminEmail);
            } else {
                System.out.println("ℹ️ Usuario ADMIN ya existe y está habilitado: " + adminEmail);
            }
        }

        var existingDemo = userRepository.findByEmail(demoEmail);

        if (existingDemo.isEmpty()) {
            User demo = User.builder()
                    .name("Demo Pawsoft")
                    .email(demoEmail)
                    .password(passwordEncoder.encode(demoPassword))
                    .role(Role.ROLE_CLIENTE)
                    .primerAcceso(false)
                    .enabled(true)
                    .build();

            userRepository.save(demo);
            System.out.println("✅ Usuario DEMO creado: " + demoEmail);
        }

        initDemoData();
        initMedications();
    }

    @Value("${app.demo.email}")
    private String demoEmail;

    @Value("${app.demo.password}")
    private String demoPassword;

    private void initMedications() {
        Object[][] meds = {
            {"Amoxicilina",      "Antibiótico de amplio espectro",          15000, "tableta"},
            {"Ampicilina",       "Antibiótico betalactámico",               12000, "ampolla"},
            {"Cefalexina",       "Antibiótico cefalosporina",               18000, "tableta"},
            {"Enrofloxacina",    "Antibiótico fluoroquinolona",             20000, "tableta"},
            {"Metronidazol",     "Antibiótico y antiparasitario",           10000, "tableta"},
            {"Doxiciclina",      "Antibiótico tetraciclina",                14000, "tableta"},
            {"Clindamicina",     "Antibiótico lincosamida",                 16000, "tableta"},
            {"Gentamicina",      "Antibiótico aminoglucósido",              22000, "ampolla"},
            {"Tramadol",         "Analgésico opioide",                      25000, "ampolla"},
            {"Meloxicam",        "Antiinflamatorio AINE",                   18000, "ml"},
            {"Carprofeno",       "Antiinflamatorio AINE",                   20000, "tableta"},
            {"Prednisona",       "Corticosteroide antiinflamatorio",        12000, "tableta"},
            {"Dexametasona",     "Corticosteroide potente",                 15000, "ampolla"},
            {"Furosemida",       "Diurético de asa",                        10000, "tableta"},
            {"Enalapril",        "Antihipertensivo IECA",                   14000, "tableta"},
            {"Omeprazol",        "Inhibidor bomba de protones",             12000, "tableta"},
            {"Metoclopramida",   "Antiemético procinético",                 10000, "ampolla"},
            {"Ondansetrón",      "Antiemético antagonista 5-HT3",           28000, "ampolla"},
            {"Ivermectina",      "Antiparasitario endectocida",             15000, "ml"},
            {"Prazicuantel",     "Antiparasitario cestocida",               12000, "tableta"},
            {"Fenbendazol",      "Antiparasitario benzimidazol",            10000, "tableta"},
            {"Insulina NPH",     "Insulina de acción intermedia",           45000, "UI"},
            {"Fenobarbital",     "Anticonvulsivante barbitúrico",           18000, "tableta"},
            {"Diazepam",         "Benzodiacepina ansiolítica",              20000, "ampolla"},
            {"Acepromazina",     "Tranquilizante fenotiacínico",            15000, "ml"},
            {"Ketamina",         "Anestésico disociativo",                  35000, "ml"},
            {"Vitamina B12",     "Suplemento vitamínico",                    8000, "ampolla"},
            {"Suero fisiológico","Solución isotónica NaCl 0.9%",            5000, "ml"},
            {"Ranitidina",       "Antiulceroso antagonista H2",             10000, "tableta"},
            {"Selamectina",      "Antiparasitario tópico",                  35000, "dosis"},
            {"Fipronil",         "Antiparasitario ectoparasiticida",        28000, "dosis"},
            {"Atenolol",         "Betabloqueante antihipertensivo",         12000, "tableta"},
            {"Hierro dextrano",  "Suplemento de hierro inyectable",        18000, "ampolla"},
            {"Calcio gluconato", "Suplemento de calcio inyectable",        15000, "ampolla"},
        };

        int nuevos = 0;
        for (Object[] m : meds) {
            String nombre = (String) m[0];
            if (!medicationCatalogRepository.existsByName(nombre)) {
                medicationCatalogRepository.save(
                    MedicationCatalog.builder()
                        .name(nombre)
                        .description((String) m[1])
                        .price(BigDecimal.valueOf((int) m[2]))
                        .unit((String) m[3])
                        .active(true)
                        .build()
                );
                nuevos++;
            }
        }
        if (nuevos > 0) System.out.println("✅ Medicamentos nuevos agregados al catálogo: " + nuevos);
    }

    /**
     * Inicializa datos de demostración COMPLETOS para la cuenta demo.
     * Crea citas pasadas con historiales médicos y pagos para la exposición.
     * SOLO PARA LA CUENTA DEMO - NO AFECTA OTRAS CUENTAS.
     * SE EJECUTA SIEMPRE para asegurar que los datos demo estén disponibles.
     */
    private void initDemoData() {
        // Buscar el usuario demo
        var demoUserOpt = userRepository.findByEmail(demoEmail);
        if (demoUserOpt.isEmpty()) {
            System.out.println("⚠️ Usuario demo no encontrado, omitiendo datos demo");
            return;
        }

        User demoUser = demoUserOpt.get();

        // Verificar si ya existen citas demo para este usuario - OPTIMIZADO
        long existingAppointments = appointmentRepository.countByClientId(demoUser.getId());

        if (existingAppointments >= 3) {
            System.out.println("ℹ️ Datos demo ya existen para " + demoEmail + " (" + existingAppointments + " citas)");
            return;
        }

        // Buscar un veterinario - OPTIMIZADO
        var vetOpt = userRepository.findFirstByRole(Role.ROLE_VETERINARIO);

        if (vetOpt.isEmpty()) {
            System.out.println("⚠️ No hay veterinarios, omitiendo datos demo");
            return;
        }

        User vet = vetOpt.get();

        // Buscar o crear mascota demo
        var pets = petRepository.findByOwnerEmail(demoEmail);
        Pet demoPet;

        if (pets.isEmpty()) {
            demoPet = Pet.builder()
                    .name("Rocky")
                    .species("Perro")
                    .breed("Golden Retriever")
                    .birthDate("2020-05-15")
                    .sex("Macho")
                    .ownerEmail(demoEmail)
                    .build();
            demoPet = petRepository.save(demoPet);
            System.out.println("✅ Mascota demo creada: " + demoPet.getName());
        } else {
            demoPet = pets.get(0);
        }

        // ═══════════════════════════════════════════════════════════════
        // CITA 1: Hace 30 días - Vacunación (COMPLETADA con historial y pago)
        // ═══════════════════════════════════════════════════════════════
        Appointment cita1 = Appointment.builder()
                .date(LocalDate.now().minusDays(30))
                .time(LocalTime.of(9, 0))
                .reason("Vacunación anual")
                .status(AppointmentStatus.COMPLETED)
                .client(demoUser)
                .vet(vet)
                .pet(demoPet)
                .build();
        cita1 = appointmentRepository.save(cita1);

        // Historial médico para cita 1
        MedicalRecord record1 = MedicalRecord.builder()
                .appointment(cita1)
                .pet(demoPet)
                .vet(vet)
                .peso(28.5)
                .temperatura(38.2)
                .frecuenciaCardiaca(90)
                .frecuenciaRespiratoria(25)
                .observacionesGenerales("Mascota en buen estado general, activa y alerta")
                .diagnosticoPrincipal("Chequeo preventivo - Vacunación")
                .diagnosticoCliente("Rocky está sano. Se aplicó vacuna antirrábica")
                .vacunasAplicadas("[{\"nombre\":\"Antirrábica\",\"lote\":\"VAC-2024-001\"}]")
                .indicacionesCliente("Observar por 24h. Evitar baños por 3 días")
                .costoMedicamentos(BigDecimal.ZERO)
                .costoTotal(new BigDecimal("45000"))
                .creadoEn(LocalDateTime.now().minusDays(30))
                .build();
        medicalRecordRepository.save(record1);

        // Pago para cita 1
        Payment pago1 = Payment.builder()
                .appointmentId(cita1.getId())
                .clientName(demoUser.getName())
                .clientEmail(demoUser.getEmail())
                .petName(demoPet.getName())
                .vetName(vet.getName())
                .appointmentDate(cita1.getDate())
                .appointmentTime(cita1.getTime())
                .concept("Vacunación antirrábica")
                .baseAmount(new BigDecimal("45000"))
                .amount(new BigDecimal("45000"))
                .status(PaymentStatus.PAID)
                .paymentDate(LocalDateTime.now().minusDays(30))
                .receivedBy("recepcion@pawsoft.com")
                .receivedByName("Recepcionista Demo")
                .createdAt(LocalDateTime.now().minusDays(30))
                .build();
        paymentRepository.save(pago1);

        // ═══════════════════════════════════════════════════════════════
        // CITA 2: Hace 15 días - Consulta general (COMPLETADA con historial y pago)
        // ═══════════════════════════════════════════════════════════════
        Appointment cita2 = Appointment.builder()
                .date(LocalDate.now().minusDays(15))
                .time(LocalTime.of(14, 30))
                .reason("Consulta por vómito")
                .status(AppointmentStatus.COMPLETED)
                .client(demoUser)
                .vet(vet)
                .pet(demoPet)
                .build();
        cita2 = appointmentRepository.save(cita2);

        // Historial médico para cita 2
        MedicalRecord record2 = MedicalRecord.builder()
                .appointment(cita2)
                .pet(demoPet)
                .vet(vet)
                .peso(28.3)
                .temperatura(38.5)
                .frecuenciaCardiaca(95)
                .frecuenciaRespiratoria(28)
                .observacionesGenerales("Mascota presenta episodios de vómito. Leve deshidratación")
                .diagnosticoPrincipal("Gastroenteritis leve")
                .diagnosticoCliente("Rocky tiene una infección estomacal leve")
                .medicamentos("[{\"nombre\":\"Metoclopramida\",\"dosis\":\"10mg\",\"via\":\"inyectable\"}]")
                .medicamentosRecetados("[{\"nombre\":\"Omeprazol\",\"dosis\":\"20mg\",\"frecuencia\":\"cada 12h\",\"duracion\":\"5 días\"}]")
                .indicacionesCliente("Dieta blanda por 3 días. Mucha agua. Regresar si empeora")
                .costoMedicamentos(new BigDecimal("15000"))
                .costoTotal(new BigDecimal("75000"))
                .creadoEn(LocalDateTime.now().minusDays(15))
                .build();
        medicalRecordRepository.save(record2);

        // Pago para cita 2
        Payment pago2 = Payment.builder()
                .appointmentId(cita2.getId())
                .clientName(demoUser.getName())
                .clientEmail(demoUser.getEmail())
                .petName(demoPet.getName())
                .vetName(vet.getName())
                .appointmentDate(cita2.getDate())
                .appointmentTime(cita2.getTime())
                .concept("Consulta general + medicamentos")
                .baseAmount(new BigDecimal("60000"))
                .amount(new BigDecimal("75000"))
                .status(PaymentStatus.PAID)
                .paymentDate(LocalDateTime.now().minusDays(15))
                .receivedBy("recepcion@pawsoft.com")
                .receivedByName("Recepcionista Demo")
                .notes("Incluye medicamentos aplicados")
                .createdAt(LocalDateTime.now().minusDays(15))
                .build();
        paymentRepository.save(pago2);

        // ═══════════════════════════════════════════════════════════════
        // CITA 3: Hace 7 días - Control post-tratamiento (NO_SHOW - no asistió)
        // ═══════════════════════════════════════════════════════════════
        Appointment cita3 = Appointment.builder()
                .date(LocalDate.now().minusDays(7))
                .time(LocalTime.of(10, 0))
                .reason("Control post-tratamiento")
                .status(AppointmentStatus.NO_SHOW)
                .client(demoUser)
                .vet(vet)
                .pet(demoPet)
                .build();
        appointmentRepository.save(cita3);

        System.out.println("✅ Datos demo completos creados para " + demoEmail);
        System.out.println("   - 3 citas (2 completadas con historial + pago, 1 no asistió)");
        System.out.println("   - 2 historiales médicos");
        System.out.println("   - 2 pagos registrados");
    }
}