# Guía de Pruebas Unitarias - Backend Pawsoft

## Descripción General

Este directorio contiene todas las pruebas unitarias y de integración del proyecto Backend Pawsoft. Las pruebas están organizadas por capas siguiendo la estructura del código fuente.

## Estructura de Pruebas

```
src/test/java/co/edu/uniquindio/backendpawsoft/
├── service/              # Pruebas de servicios (lógica de negocio)
├── repository/           # Pruebas de repositorios (acceso a datos)
├── security/             # Pruebas de seguridad (JWT, autenticación)
├── model/                # Pruebas de modelos (entidades)
├── exception/            # Pruebas de manejo de excepciones
└── README_TESTS.md       # Este archivo
```

## Tecnologías Utilizadas

- **JUnit 5**: Framework principal de pruebas
- **Mockito**: Framework para crear mocks y stubs
- **Spring Boot Test**: Soporte para pruebas de Spring
- **H2 Database**: Base de datos en memoria para pruebas de integración
- **AssertJ**: Librería de aserciones (opcional)

## Tipos de Pruebas

### 1. Pruebas Unitarias de Servicios

Ubicación: `service/`

Validan la lógica de negocio de forma aislada usando mocks para las dependencias.

**Archivos:**
- `UserServiceTest.java` - Gestión de usuarios
- `AuthServiceTest.java` - Autenticación y autorización
- `AppointmentServiceTest.java` - Gestión de citas
- `PetServiceTest.java` - Gestión de mascotas
- `TwoFactorServiceTest.java` - Autenticación 2FA
- `PasswordResetServiceTest.java` - Recuperación de contraseña

### 2. Pruebas de Repositorios

Ubicación: `repository/`

Pruebas de integración que validan las operaciones de persistencia con base de datos H2.

**Archivos:**
- `UserRepositoryTest.java`
- `PetRepositoryTest.java`
- `AppointmentRepositoryTest.java`

### 3. Pruebas de Seguridad

Ubicación: `security/`

Validan la generación y validación de tokens JWT.

**Archivos:**
- `JwtServiceTest.java`

### 4. Pruebas de Modelos

Ubicación: `model/`

Validan el comportamiento de las entidades y su integración con Spring Security.

**Archivos:**
- `UserTest.java`
- `PetTest.java`
- `AppointmentTest.java`

### 5. Pruebas de Manejo de Excepciones

Ubicación: `exception/`

Validan que las excepciones se manejen correctamente.

**Archivos:**
- `GlobalExceptionHandlerTest.java`

## Ejecutar las Pruebas

### Ejecutar todas las pruebas

```bash
mvn test
```

### Ejecutar pruebas de una clase específica

```bash
mvn test -Dtest=UserServiceTest
```

### Ejecutar pruebas con cobertura

```bash
mvn test jacoco:report
```

El reporte de cobertura se generará en: `target/site/jacoco/index.html`

### Ejecutar solo pruebas unitarias (excluyendo integración)

```bash
mvn test -Dgroups="unit"
```

### Ejecutar solo pruebas de integración

```bash
mvn test -Dgroups="integration"
```

## Convenciones de Nomenclatura

### Nombres de Clases de Prueba
- Formato: `[ClaseAProbar]Test.java`
- Ejemplo: `UserServiceTest.java`

### Nombres de Métodos de Prueba
- Formato: `test[Funcionalidad][Escenario]`
- Ejemplo: `testCreateUserExitoso()`
- Usar `@DisplayName` para descripciones en español

### Estructura de un Método de Prueba (AAA Pattern)

```java
@Test
@DisplayName("Descripción clara del caso de prueba")
void testMetodo() {
    // Arrange (Preparar)
    // Configurar datos y mocks necesarios
    
    // Act (Actuar)
    // Ejecutar el método a probar
    
    // Assert (Verificar)
    // Validar los resultados esperados
}
```

## Cobertura de Pruebas

### Objetivos de Cobertura

- **Servicios**: > 80% de cobertura
- **Repositorios**: > 70% de cobertura
- **Modelos**: > 60% de cobertura
- **Seguridad**: > 85% de cobertura

### Áreas Cubiertas

✅ Creación y gestión de usuarios
✅ Autenticación con 2FA
✅ Gestión de citas médicas
✅ Gestión de mascotas
✅ Recuperación de contraseña
✅ Generación y validación de JWT
✅ Manejo de excepciones
✅ Validaciones de negocio
✅ Operaciones CRUD en repositorios

## Casos de Prueba Importantes

### Seguridad
- Validación de contraseñas fuertes
- Bloqueo de cuenta por intentos fallidos
- Expiración de tokens JWT
- Validación de códigos 2FA

### Lógica de Negocio
- Validación de horarios de citas
- Permisos de acceso a recursos
- Cancelación de citas
- Actualización de datos con validaciones

### Persistencia
- Consultas personalizadas
- Relaciones entre entidades
- Operaciones CRUD básicas

## Mejores Prácticas

1. **Independencia**: Cada prueba debe ser independiente y no depender del orden de ejecución
2. **Claridad**: Usar nombres descriptivos y `@DisplayName` en español
3. **Aislamiento**: Usar mocks para aislar la unidad bajo prueba
4. **Datos de Prueba**: Usar datos realistas pero no sensibles
5. **Limpieza**: Limpiar recursos después de cada prueba si es necesario
6. **Aserciones**: Usar aserciones específicas y descriptivas

## Solución de Problemas Comunes

### Error: "No qualifying bean of type"
- Verificar que los mocks estén correctamente anotados con `@Mock`
- Asegurar que la clase de prueba use `@ExtendWith(MockitoExtension.class)`

### Error: "NullPointerException en pruebas"
- Verificar que todos los campos necesarios estén inicializados en `@BeforeEach`
- Revisar que los mocks retornen valores apropiados

### Pruebas de repositorio fallan
- Verificar que `application-test.properties` esté configurado correctamente
- Asegurar que la clase use `@DataJpaTest`

## Contribuir con Nuevas Pruebas

Al agregar nuevas funcionalidades, seguir estos pasos:

1. Crear la clase de prueba correspondiente
2. Implementar pruebas para casos exitosos
3. Implementar pruebas para casos de error
4. Implementar pruebas para casos límite
5. Documentar con `@DisplayName` en español
6. Verificar cobertura de código

## Contacto

**Proyecto**: Pawsoft
**Universidad**: Universidad del Quindío
**Materia**: Software III

**Autoras**:
- Valentina Porras Salazar
- Helen Xiomara Giraldo Libreros

**Profesor**:
- Raúl Yulbraynner Rivera Gálvez
