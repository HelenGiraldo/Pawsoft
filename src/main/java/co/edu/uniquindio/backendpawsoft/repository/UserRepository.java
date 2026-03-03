package co.edu.uniquindio.backendpawsoft.repository;

import co.edu.uniquindio.backendpawsoft.enums.Role;
import co.edu.uniquindio.backendpawsoft.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio encargado de la persistencia de la entidad User.
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

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca un usuario por su correo electrónico
     *
     * Spring Data JPA genera automáticamente la consulta
     * basándose en el nombre del método
     *
     * @param email correo electronico del usuario
     * @return Optional con el usuario encontrado
     */

    Optional<User> findByEmail(String email);

    // Para listar veterinarios (y filtrar por rol en el admin)
    List<User> findByRole(Role role);

}
