# Guía para Solucionar el Backend en EC2

## Problema Identificado
El backend está crasheando porque no puede conectarse a la base de datos MySQL de Clever Cloud.

## Solución Paso a Paso

### 1. Conectarse a EC2
```bash
ssh -i pawsoft-key.pem ec2-user@3.137.150.192
```

### 2. Verificar el estado actual
```bash
# Ver los logs del error
tail -f /home/ec2-user/app.log

# Ver si hay algún proceso Java corriendo
ps aux | grep java
```

### 3. Actualizar application.properties

Edita el archivo de configuración:
```bash
nano /home/ec2-user/application.properties
```

Asegúrate de que tenga la configuración correcta de Clever Cloud:

```properties
# Database Configuration (Clever Cloud)
spring.datasource.url=jdbc:mysql://TU_HOST_CLEVER_CLOUD:3306/TU_DB_NAME?createDatabaseIfNotExist=true&useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=TU_USUARIO_CLEVER_CLOUD
spring.datasource.password=TU_PASSWORD_CLEVER_CLOUD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JWT Configuration
jwt.secret=TU_JWT_SECRET
jwt.expiration=3600000

# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=helenx.giraldol@uqvirtual.edu.co
spring.mail.password=TU_APP_PASSWORD_GMAIL
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Application URLs
app.frontend.url=https://d62s9ba36azh1.cloudfront.net
app.backend.url=https://api.pawsoft.online

# reCAPTCHA Configuration
recaptcha.secret=TU_RECAPTCHA_SECRET
recaptcha.verify-url=https://www.google.com/recaptcha/api/siteverify

# Groq API Configuration
groq.api.key=TU_GROQ_API_KEY
groq.api.url=https://api.groq.com/openai/v1/chat/completions

# Server Configuration
server.address=0.0.0.0
server.port=8080
```

### 4. Verificar conectividad a Clever Cloud

Prueba la conexión a la base de datos:
```bash
# Instalar mysql client si no está instalado
sudo yum install mysql -y

# Probar conexión
mysql -h TU_HOST_CLEVER_CLOUD -u TU_USUARIO -p
```

### 5. Verificar Security Group de Clever Cloud

En el panel de Clever Cloud, asegúrate de que:
- La base de datos permite conexiones desde la IP de EC2: `3.137.150.192`
- O permite conexiones desde cualquier IP (0.0.0.0/0) si es necesario

### 6. Reiniciar la aplicación

```bash
# Matar el proceso Java actual si existe
pkill -f "java.*pawsoft"

# Iniciar la aplicación
cd /home/ec2-user
nohup java -jar pawsoft-backend.jar > app.log 2>&1 &

# Verificar que inició correctamente
tail -f app.log
```

### 7. Verificar que el backend responde

Desde tu máquina local:
```bash
curl http://3.137.150.192:8080/actuator/health
```

Debería responder:
```json
{"status":"UP"}
```

### 8. Configurar DNS (si aún no está configurado)

En tu proveedor de DNS (Route 53, Cloudflare, etc.):
- Crear registro A: `api.pawsoft.online` → `3.137.150.192`
- Esperar propagación DNS (puede tomar hasta 48 horas, pero usualmente 5-10 minutos)

### 9. Verificar Security Group de EC2

En AWS Console → EC2 → Security Groups → `launch-wizard-1`:

Reglas de entrada necesarias:
- Puerto 8080: TCP, Source: 0.0.0.0/0 (HTTP desde cualquier origen)
- Puerto 22: TCP, Source: Tu IP (SSH)
- Puerto 443: TCP, Source: 0.0.0.0/0 (HTTPS si usas certificado SSL)

## Comandos Útiles

```bash
# Ver logs en tiempo real
tail -f /home/ec2-user/app.log

# Ver procesos Java
ps aux | grep java

# Matar proceso Java
pkill -f "java.*pawsoft"

# Verificar puerto 8080
netstat -tuln | grep 8080

# Probar endpoint local
curl http://localhost:8080/actuator/health
```

## Notas Importantes

1. **Clever Cloud IP Whitelist**: Asegúrate de que Clever Cloud permite conexiones desde `3.137.150.192`
2. **SSL/TLS**: Si Clever Cloud requiere SSL, usa `useSSL=true` en la URL de conexión
3. **Firewall**: Verifica que no haya firewall en EC2 bloqueando conexiones salientes a Clever Cloud
4. **Credenciales**: Verifica que las credenciales de Clever Cloud sean correctas
