# Configuración de Application Properties

## Instrucciones de Configuración

1. **Copia el archivo de ejemplo:**
   ```bash
   cp application.properties.example application.properties
   ```

2. **Configura las credenciales:**
   Edita `application.properties` y reemplaza los siguientes valores:

   - `YOUR_DB_HOST`, `YOUR_DB_NAME`, `YOUR_DB_USERNAME`, `YOUR_DB_PASSWORD`: Credenciales de tu base de datos MySQL
   - `YOUR_JWT_SECRET_KEY_BASE64_ENCODED_MIN_256_BITS`: Clave secreta para JWT (mínimo 256 bits, codificada en Base64)
   - `YOUR_EMAIL@gmail.com`, `YOUR_APP_PASSWORD`: Credenciales de Gmail para envío de correos
   - `YOUR_RECAPTCHA_SECRET_KEY`: Clave secreta de Google reCAPTCHA
   - `YOUR_GROQ_API_KEY`: Clave API de Groq para el chatbot
   - `admin@example.com`, `ChangeThisPassword123!`: Credenciales del administrador inicial
   - `https://your-frontend-url.com`: URL de tu frontend en producción

## Seguridad

⚠️ **IMPORTANTE**: 
- **NUNCA** subas el archivo `application.properties` a Git
- El archivo ya está incluido en `.gitignore`
- Solo comparte credenciales a través de canales seguros (gestores de secretos, variables de entorno)

## Variables de Entorno (Alternativa)

Para producción, considera usar variables de entorno en lugar de `application.properties`:

```bash
export SPRING_DATASOURCE_URL=jdbc:mysql://...
export SPRING_DATASOURCE_USERNAME=...
export SPRING_DATASOURCE_PASSWORD=...
export JWT_SECRET=...
# etc.
```

## Generación de JWT Secret

Para generar una clave JWT segura:

```bash
# Opción 1: OpenSSL
openssl rand -base64 64

# Opción 2: Node.js
node -e "console.log(require('crypto').randomBytes(64).toString('base64'))"
```
