package co.edu.uniquindio.backendpawsoft;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Clase principal del sistema Backend Pawsoft.
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
 *
 */

@EnableScheduling
@SpringBootApplication
public class BackendPawsoftApplication {

    /**
     * Método principal que arranca la aplicación.
     *
     * @param args argumentos de ejecución
     */


    public static void main(String[] args) {
        SpringApplication.run(BackendPawsoftApplication.class, args);
    }

}
