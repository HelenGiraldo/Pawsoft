package co.edu.uniquindio.backendpawsoft.service;


import co.edu.uniquindio.backendpawsoft.dto.LoginRequest;
import co.edu.uniquindio.backendpawsoft.dto.LoginResponse;
import co.edu.uniquindio.backendpawsoft.exception.NotFoundException;
import co.edu.uniquindio.backendpawsoft.exception.UnauthorizedException;
import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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
public class UserService {

    private final BCryptPasswordEncoder passwordEncoder;


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

    public List<User> getAllUsers(){
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
     *
     * Regla de negocio que se aplica:
     * -No se permite registrar un usuario con un correo ya existente
     *
     * @param user objeto usuario a registrar
     * @return usuario guardado
     * @throws RuntimeException si el correo ya existe
     *
     */

    public User createUser(User user){

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (userRepository.findByEmail(user.getEmail()).isPresent()){
            throw new RuntimeException("El correo ya está registrado");
        }

        return userRepository.save(user);

    }

    /**
     *
     * Elimina un usuario por su identificador
     * @param id identificador del usuario a eliminar
     * @throws  RuntimeException si no existe un usuario con el ID proporcionado
     *
     */
    public void deleteUser(Long id){

        if (!userRepository.existsById(id)){
            throw new RuntimeException("Usuario no encontrado con ID: " + id);
        }
        userRepository.deleteById(id);
    }


    /**
     * Actualizar un usuario existente
     *
     * @param id identificador único del usuario a actualizar
     * @param  userUpdated objeto que contiene los nuevos datos del usuario
     * @return usuario actualizado con la información persistida en la base de datos
     * @throws RuntimeException si no existe un usuario con el ID proporcionado
     *
     */

    public User updateUser(Long id, User userUpdated){

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        existingUser.setName(userUpdated.getName());
        existingUser.setEmail(userUpdated.getEmail());
        existingUser.setPassword(passwordEncoder.encode(userUpdated.getPassword()));


        return userRepository.save(existingUser);

    }



}

