package co.edu.uniquindio.backendpawsoft.migration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Migración para eliminar la restricción única problemática en appointments
 * que impide crear citas en horarios donde ya existen citas canceladas.
 */
@Component
public class AppointmentConstraintMigration implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        try {
            // Verificar si la restricción existe antes de intentar eliminarla
            String checkConstraint = """
                SELECT COUNT(*) FROM information_schema.table_constraints 
                WHERE constraint_name = 'UKeddbd7cdeq1jdtgxy2py35ys6' 
                AND table_name = 'appointments'
                """;
            
            Integer constraintExists = jdbcTemplate.queryForObject(checkConstraint, Integer.class);
            
            if (constraintExists != null && constraintExists > 0) {
                // Eliminar la restricción única problemática
                jdbcTemplate.execute("ALTER TABLE appointments DROP INDEX UKeddbd7cdeq1jdtgxy2py35ys6");
                System.out.println("✅ Restricción única problemática eliminada exitosamente");
            } else {
                System.out.println("ℹ️ La restricción única ya fue eliminada anteriormente");
            }
            
        } catch (Exception e) {
            System.err.println("⚠️ Error durante la migración de restricción: " + e.getMessage());
            // No lanzar excepción para que la app siga funcionando
        }
    }
}