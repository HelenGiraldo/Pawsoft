package co.edu.uniquindio.backendpawsoft.service;


import co.edu.uniquindio.backendpawsoft.dto.LoginRequest;
import co.edu.uniquindio.backendpawsoft.dto.LoginResponse;
import co.edu.uniquindio.backendpawsoft.dto.UserRequest;
import co.edu.uniquindio.backendpawsoft.exception.NotFoundException;
import co.edu.uniquindio.backendpawsoft.exception.UnauthorizedException;
import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.repository.UserRepository;
import jakarta.persistence.Column;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio encargado de la lógica de negocio relacionada con la entidad User.
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
 * </p>

 */

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;


    /**
     * Repositorio para acceso a datos de usuarios
     *
     */
    private final UserRepository userRepository;

    /**
     * Obtienen la lista completa de usuarios registrados
     *
     * @return lista de usuarios
     */

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Busca un usuario por su identificador
     *
     * @param id identificador del usuario
     * @return Optional con el usuario que si existe
     */

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    /**
     * Registra un nuevo usuario en el sistema
     * la contrasñea se encripta antes de almacenarse
     * <p>
     * Regla de negocio que se aplica:
     * -No se permite registrar un usuario con un correo ya existente
     *
     * @param userRequest objeto usuario a registrar
     * @return usuario guardado
     * @throws RuntimeException si el correo ya existe
     *
     */

    public User createUser(UserRequest userRequest) {
        if (userRepository.findByEmail(userRequest.getEmail()).isPresent()) {
            throw new RuntimeException("El correo ya está registrado");
        }

        User user = new User();
        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));

        return userRepository.save(user);
    }


    /**
     *
     * Elimina un usuario por su identificador
     *
     * @param id identificador del usuario a eliminar
     * @throws RuntimeException si no existe un usuario con el ID proporcionado
     *
     */
    public void deleteUser(Long id) {

        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado con ID: " + id);
        }
        userRepository.deleteById(id);
    }


    /**
     * Actualizar un usuario existente
     *
     * @param id          identificador único del usuario a actualizar
     * @param userUpdated objeto que contiene los nuevos datos del usuario
     * @return usuario actualizado con la información persistida en la base de datos
     * @throws RuntimeException si no existe un usuario con el ID proporcionado
     *
     */

    public User updateUser(Long id, UserRequest userUpdated) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        if (!isPasswordStrong(userUpdated.getPassword())) {
            throw new RuntimeException(
                    "La contraseña debe tener mínimo 8 caracteres, una mayúscula, un número y un carácter especial"
            );
        }
        existingUser.setName(userUpdated.getName());
        existingUser.setEmail(userUpdated.getEmail());
        existingUser.setPassword(passwordEncoder.encode(userUpdated.getPassword()));


        return userRepository.save(existingUser);

    }

    /**
     * Valida la fuerza de una contraseña según reglas de seguridad
     *
     * @param password Contraseña a validar
     * @return true si la contraseña cumple las reglas, false si no
     *
     */
    public boolean isPasswordStrong(String password) {
        String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        return password.matches(pattern);
    }

    /**
     * Carga un usuario por su correo electrónico para el proceso de autenticación.
     *
     * Este método es utilizado automáticamente por Spring Security
     * durante el proceso de validación de credenciales.
     *
     * Regla de negocio:
     * - Si el usuario no existe, se lanza excepción de autenticación.
     *
     * @param email correo electrónico del usuario
     * @return UserDetails con la información del usuario
     * @throws UsernameNotFoundException si no existe un usuario con ese correo
     */

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }




}









