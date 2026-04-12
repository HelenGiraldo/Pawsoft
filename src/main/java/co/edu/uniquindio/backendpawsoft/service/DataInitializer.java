package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.enums.Role;
import co.edu.uniquindio.backendpawsoft.model.MedicationCatalog;
import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.repository.MedicationCatalogRepository;
import co.edu.uniquindio.backendpawsoft.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

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

        initMedications();
    }

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
}