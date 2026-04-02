# Manual de Operaciones — PawSoft

**Proyecto:** PawSoft — Sistema de gestión veterinaria  
**Universidad:** Universidad del Quindío  
**Programa:** Ingeniería de Sistemas y Computación  
**Materia:** Software III  
**Autoras:** Valentina Porras Salazar · Helen Xiomara Giraldo Libreros  
**Profesor:** Raúl Yulbraynner Rivera Gálvez  

---

## 1. Ambientes del Sistema

### 1.1 Ambiente Local (Desarrollo)

**Propósito:** Desarrollo y pruebas locales por parte del equipo.

**Infraestructura:**
- Backend: `http://localhost:8080`
- Frontend: `http://localhost:4200`
- Base de datos: MySQL local

**Configuración:**
- Variables de entorno en `application-local.properties`
- Base de datos local con datos de prueba
- Credenciales de desarrollo para servicios externos

### 1.2 Ambiente de Producción

**Propósito:** Sistema en operación para usuarios finales.

**Infraestructura:**
- Backend: Azure App Service (`pawsoft-backend.azurewebsites.net`)
- Frontend: Netlify (`pawsoft.online`)
- Base de datos: Clever Cloud MySQL 8.0

**Configuración:**
- Variables de entorno en Azure App Service
- Certificados SSL configurados automáticamente
- Credenciales de producción para servicios externos
- Backups automáticos habilitados en Clever Cloud

---

## 2. Proceso de Despliegue

### 2.1 Despliegue de Backend

**Pasos:**

1. Compilar el proyecto:
```bash
cd backendPawsoft
./mvnw clean package -DskipTests
```

2. Desplegar a Azure App Service:
```bash
az webapp deploy --resource-group pawsoft-rg --name pawsoft-backend --src-path target/backendpawsoft-0.0.1-SNAPSHOT.jar
```

3. Verificar que el servicio esté corriendo:
```bash
az webapp show --resource-group pawsoft-rg --name pawsoft-backend --query state
```

4. Ver logs en tiempo real:
```bash
az webapp log tail --resource-group pawsoft-rg --name pawsoft-backend
```

### 2.2 Despliegue de Frontend

**Pasos:**

1. Compilar el proyecto:
```bash
cd Front-end-pawsoft
npm run build
```

2. Desplegar a Netlify:
```bash
netlify deploy --prod --dir=dist/pawsoft
```

O mediante integración continua con Git (recomendado):
- Push a la rama principal
- Netlify despliega automáticamente

### 2.3 Migraciones de Base de Datos

**Antes de desplegar cambios que afecten la base de datos:**

1. Conectarse a la base de datos de producción:
```bash
mysql -h bjupuy...clever-cloud.com -u [user] -p
```

2. Ejecutar el script de migración:
```sql
USE pawsoft;
-- Ejecutar ALTER TABLE u otros comandos necesarios
```

3. Verificar que la migración se aplicó correctamente:
```sql
DESCRIBE nombre_tabla;
```

4. Desplegar el backend con los cambios de código

---

## 3. Procedimientos de Respaldo

### 3.1 Backup Automático

**Configuración actual:**

Los backups son gestionados automáticamente por Clever Cloud:
- Frecuencia: Diaria
- Retención: Según plan contratado
- Ubicación: Infraestructura de Clever Cloud

**Backup manual adicional (opcional):**

Script de backup local:
```bash
#!/bin/bash
FECHA=$(date +%Y%m%d_%H%M%S)
ARCHIVO="./backups/pawsoft_$FECHA.sql"
mysqldump -h bjupuy...clever-cloud.com -u [user] -p[PASSWORD] pawsoft > "$ARCHIVO"
find ./backups -name "*.sql" -mtime +7 -delete
echo "Backup completado: $ARCHIVO"
```

**Ejecución:** Configurar cron local si se desea backup adicional  
**Retención:** 7 días (configurable)

### 3.2 Verificar Backups

**Backups automáticos de Clever Cloud:**
- Acceder al panel de Clever Cloud
- Ir a la sección de backups de la base de datos
- Ver historial y descargar si es necesario

**Backups manuales locales (si configurados):**
```bash
ls -lh ./backups/
tail -20 ./backups/backup.log
```

### 3.3 Backup Manual

Si se necesita un backup inmediato antes de un cambio crítico:

```bash
mysqldump -h bjupuy...clever-cloud.com -u [user] -p pawsoft > backup_manual_$(date +%Y%m%d).sql
```

---

## 4. Procedimientos de Recuperación

### 4.1 Restaurar Base de Datos desde Backup

**Escenario:** La base de datos se corrompió o se eliminaron datos por error.

**Pasos:**

1. Descargar el backup desde Clever Cloud o usar backup local

2. Conectarse a la base de datos:
```bash
mysql -h bjupuy...clever-cloud.com -u [user] -p
```

3. Restaurar la base de datos:
```bash
mysql -h bjupuy...clever-cloud.com -u [user] -p pawsoft < backup_20260401.sql
```

4. Verificar que los datos se restauraron correctamente:
```bash
mysql -h bjupuy...clever-cloud.com -u [user] -p
USE pawsoft;
SELECT COUNT(*) FROM users;
SELECT COUNT(*) FROM appointments;
```

5. Reiniciar el backend en Azure:
```bash
az webapp restart --resource-group pawsoft-rg --name pawsoft-backend
```

**Tiempo estimado de recuperación:** 5-10 minutos dependiendo del tamaño del backup.

### 4.2 Recuperación ante Fallo del Backend

**Escenario:** El backend dejó de responder o está caído.

**Pasos:**

1. Verificar estado del servicio en Azure:
```bash
az webapp show --resource-group pawsoft-rg --name pawsoft-backend --query state
```

2. Ver logs de error:
```bash
az webapp log tail --resource-group pawsoft-rg --name pawsoft-backend
```

3. Reiniciar el servicio:
```bash
az webapp restart --resource-group pawsoft-rg --name pawsoft-backend
```

4. Si el problema persiste, verificar:
   - Conexión a la base de datos Clever Cloud
   - Variables de entorno en Azure App Service
   - Cuota de recursos del plan gratuito

5. Como último recurso, redesplegar la última versión estable

### 4.3 Recuperación ante Fallo de Base de Datos

**Escenario:** La base de datos no responde.

**Pasos:**

1. Verificar estado en el panel de Clever Cloud

2. Verificar conectividad:
```bash
telnet bjupuy...clever-cloud.com 3306
```

3. Si Clever Cloud reporta problemas, esperar resolución automática

4. Si el problema persiste, contactar soporte de Clever Cloud

5. Mientras tanto, restaurar desde el backup más reciente

---

## 5. Monitoreo del Sistema

### 5.1 Azure Application Insights

**Configuración:** Monitoreo integrado con Azure App Service.

**Azure Portal:**
- URL: https://portal.azure.com
- Navegar a: App Services → pawsoft-backend → Application Insights

**Métricas disponibles:**
- Tiempo de respuesta de endpoints
- Cantidad de requests por endpoint
- Errores HTTP (4xx, 5xx)
- Uso de memoria y CPU
- Disponibilidad del servicio

### 5.2 Logs de Aplicación

**Backend (Azure):**
- Logs en tiempo real: `az webapp log tail --resource-group pawsoft-rg --name pawsoft-backend`
- Logs históricos: Acceder desde Azure Portal → App Services → Log stream

**Base de datos (Clever Cloud):**
- Logs disponibles en el panel de Clever Cloud
- Sección: Logs & Metrics

### 5.3 Auditoría de Acciones

**Base de datos:** Tabla `audit_log`

**Consultar acciones recientes:**
```sql
SELECT * FROM audit_logs ORDER BY created_at DESC LIMIT 50;
```

**Consultar errores de login:**
```sql
SELECT * FROM audit_logs WHERE action = 'USER_LOGIN_FAILED' ORDER BY created_at DESC;
```

**Consultar acciones de un usuario:**
```sql
SELECT * FROM audit_logs WHERE user_id = [ID] ORDER BY created_at DESC;
```

---

## 6. Registro de Incidentes

### 6.1 Sistema de Auditoría

El sistema registra automáticamente en la tabla `audit_log`:

- Intentos de login fallidos
- Creación, modificación y cancelación de citas
- Inicio y cancelación de atención médica
- Registro y confirmación de pagos
- Cambios en precios de servicios
- Cambios en perfil de usuario

Cada registro incluye:
- Usuario que ejecutó la acción
- Tipo de acción
- Entidad afectada
- Timestamp
- Dirección IP (cuando aplica)

### 6.2 Logs de Errores

**Consultar errores del backend:**
```bash
ssh -i pawsoft-key.pem ec2-user@3.135.224.139
sudo journalctl -u pawsoft-backend -p err --since "1 hour ago"
```

**Consultar errores del backup:**
```bash
grep -i error /home/ec2-user/backups/backup.log
```

---

## 7. Mantenimiento

### 7.1 Mantenimiento Correctivo

**Proceso:**

1. Identificar el problema (logs, reportes de usuarios, monitoreo)
2. Reproducir el error en ambiente local
3. Desarrollar y probar la corrección
4. Desplegar en producción siguiendo el proceso estándar
5. Verificar que el problema se resolvió
6. Documentar la corrección en el sistema de auditoría

**Ejemplos recientes:**
- Corrección de error al iniciar cita (columna status muy pequeña)
- Fix de scroll en login
- Corrección de filtros de citas por estado

### 7.2 Mantenimiento Evolutivo

**Proceso:**

1. Documentar el requerimiento
2. Diseñar la solución
3. Implementar en ambiente local
4. Ejecutar pruebas
5. Desplegar en producción
6. Monitorear comportamiento post-despliegue

**Ejemplos recientes:**
- Sistema de refresh tokens
- Estado IN_PROGRESS para citas
- Subida de fotos en registros médicos
- Cancelación de atención iniciada

### 7.3 Limpieza de Datos

**Tokens expirados:**

El sistema limpia automáticamente tokens expirados diariamente a las 2:00 AM mediante un job programado en `RefreshTokenService`.

**Limpieza manual si es necesario:**
```sql
DELETE FROM refresh_tokens WHERE expires_at < NOW();
DELETE FROM refresh_tokens WHERE revoked = true AND created_at < DATE_SUB(NOW(), INTERVAL 30 DAY);
```

---

## 8. Estabilidad Operativa

### 8.1 Indicadores de Estabilidad

**Disponibilidad:**
- Objetivo: 99% uptime
- Medición: Monitoreo con Prometheus + Grafana

**Tiempo de respuesta:**
- Objetivo: < 500ms para endpoints críticos
- Medición: Métricas de Prometheus + Grafana

**Tasa de errores:**
- Objetivo: < 1% de requests con error 5xx
- Medición: Logs de aplicación y Prometheus + Grafana

### 8.2 Procedimientos de Contingencia

**Si el backend no responde:**
1. Verificar estado en Azure Portal
2. Revisar logs con `az webapp log tail`
3. Reiniciar el servicio con `az webapp restart`
4. Si persiste, redesplegar última versión estable

**Si la base de datos falla:**
1. Verificar estado en panel de Clever Cloud
2. Verificar conectividad desde local
3. Esperar recuperación automática de Clever Cloud
4. Si es necesario, restaurar desde backup

**Si el frontend no carga:**
1. Verificar estado en Netlify Dashboard
2. Verificar que el build se completó correctamente
3. Revisar logs de despliegue en Netlify
4. Si es necesario, redesplegar frontend

---

## 9. Contactos de Soporte

**Equipo de desarrollo:**
- Valentina Porras Salazar
- Helen Xiomara Giraldo Libreros

**Correo de soporte:**
- pawsoft.vet@gmail.com

**Infraestructura:**
- Azure Support (plan gratuito incluye soporte básico)
- Clever Cloud Support (según plan contratado)
- Netlify Support (plan gratuito incluye soporte comunitario)

---

## 10. Checklist de Despliegue

Antes de cada despliegue a producción, verificar:

- [ ] Código compilado sin errores
- [ ] Tests unitarios ejecutados exitosamente
- [ ] Migraciones de base de datos aplicadas (si aplica)
- [ ] Variables de entorno actualizadas (si aplica)
- [ ] Backup manual ejecutado antes del despliegue
- [ ] Notificación al equipo sobre el despliegue
- [ ] Verificación post-despliegue (login, funcionalidades críticas)
- [ ] Monitoreo de logs durante 30 minutos post-despliegue

---

## 11. Procedimientos de Rollback

### 11.1 Rollback de Backend

Si el despliegue causa problemas:

1. Identificar la versión anterior estable en el historial de despliegues de Azure

2. Redesplegar versión anterior:
```bash
az webapp deployment source config-zip --resource-group pawsoft-rg --name pawsoft-backend --src previous-version.jar
```

3. O usar el portal de Azure:
   - Ir a Deployment Center
   - Seleccionar despliegue anterior
   - Hacer rollback

4. Si hubo migración de base de datos, restaurar desde backup

### 11.2 Rollback de Frontend

1. Revertir el commit en Git:
```bash
git revert HEAD
git push
```

2. Netlify desplegará automáticamente la versión revertida

O usar el panel de Netlify:
- Ir a Deploys
- Seleccionar despliegue anterior
- Hacer rollback

### 11.3 Rollback de Base de Datos

Si una migración causó problemas:

1. Restaurar desde el backup inmediatamente anterior:
```bash
mysql -h bjupuy...clever-cloud.com -u [user] -p pawsoft < backup_[FECHA].sql
```

2. Reiniciar el backend en Azure:
```bash
az webapp restart --resource-group pawsoft-rg --name pawsoft-backend
```

3. Verificar que el sistema funcione correctamente

---

## 12. Monitoreo y Alertas

### 12.1 Métricas de Azure Application Insights

**Azure Portal:** https://portal.azure.com  
**Recurso:** pawsoft-backend → Application Insights

**Métricas monitoreadas:**
- `http_server_requests_seconds` - Tiempo de respuesta por endpoint
- `jvm_memory_used_bytes` - Uso de memoria
- `jvm_threads_live` - Threads activos
- `http_server_requests_total` - Total de requests
- Disponibilidad del servicio

**Dashboard:** Disponible en Azure Portal  
**Actualización:** Tiempo real

### 12.2 Logs de Aplicación

**Ver logs en tiempo real:**
```bash
az webapp log tail --resource-group pawsoft-rg --name pawsoft-backend
```

**Ver logs de las últimas 24 horas:**
```bash
az webapp log download --resource-group pawsoft-rg --name pawsoft-backend
```

**Filtrar solo errores:**
Usar Azure Portal → App Services → Log stream → Filtrar por nivel ERROR

### 12.3 Verificación de Backups

**Verificar backups en Clever Cloud:**
- Acceder al panel de Clever Cloud
- Ir a la sección de la base de datos
- Ver historial de backups automáticos

**Verificar backups manuales locales (si configurados):**
```bash
ls -lh ./backups/*.sql
tail -50 ./backups/backup.log
```

---

## 13. Troubleshooting Común

### 13.1 Backend no inicia

**Síntomas:** El servicio no arranca o se detiene inmediatamente.

**Posibles causas:**
- Error de conexión a base de datos
- Puerto 8080 ya en uso
- Variables de entorno faltantes
- Error en el código

**Solución:**
```bash
az webapp log tail --resource-group pawsoft-rg --name pawsoft-backend
```

Revisar el error específico y corregir la configuración o código.

### 13.2 Errores 403 en el frontend

**Síntomas:** Usuarios reportan errores 403 al usar la aplicación.

**Posibles causas:**
- JWT expirado y refresh token no funcionando
- Usuario sin permisos para la acción
- CORS mal configurado

**Solución:**
1. Verificar que el interceptor de refresh token esté funcionando
2. Revisar logs del backend en Azure con `az webapp log tail`
3. Verificar configuración de CORS en `CorsConfig.java`

### 13.3 Base de datos lenta

**Síntomas:** Tiempos de respuesta altos en endpoints.

**Posibles causas:**
- Falta de índices en tablas
- Consultas N+1
- Conexiones no cerradas

**Solución:**
1. Revisar métricas en Azure Application Insights
2. Revisar métricas de Clever Cloud para la base de datos
3. Analizar queries lentas con `EXPLAIN`
4. Agregar índices si es necesario
5. Optimizar consultas JPA

### 13.4 Límites del Plan Gratuito

**Síntomas:** La aplicación deja de responder o se detiene.

**Posibles causas:**
- Azure App Service: Límite de 60 minutos de CPU por día en plan gratuito
- Clever Cloud: Límite de conexiones simultáneas
- Netlify: Límite de ancho de banda mensual

**Solución:**
1. Verificar uso de recursos en los paneles de cada servicio
2. Considerar upgrade a plan de pago si se exceden límites
3. Optimizar código para reducir uso de recursos

---

## 14. Seguridad Operativa

### 14.1 Rotación de Credenciales

**Frecuencia recomendada:** Cada 90 días

**Credenciales a rotar:**
- Contraseña de base de datos
- JWT secret
- API keys de servicios externos (Cloudinary, reCAPTCHA)
- Contraseña SMTP

**Proceso:**
1. Generar nuevas credenciales
2. Actualizar variables de entorno en servidor
3. Reiniciar el backend
4. Verificar que todo funcione correctamente

### 14.2 Actualización de Dependencias

**Frecuencia recomendada:** Mensual para parches de seguridad

**Proceso:**
```bash
./mvnw versions:display-dependency-updates
./mvnw versions:use-latest-releases
./mvnw clean test
```

Si los tests pasan, desplegar en producción.

### 14.3 Revisión de Logs de Auditoría

**Frecuencia recomendada:** Semanal

**Revisar:**
- Intentos de login fallidos repetidos (posible ataque)
- Acciones inusuales de usuarios
- Cambios en datos críticos (precios, usuarios staff)

```sql
SELECT * FROM audit_logs WHERE action = 'USER_LOGIN_FAILED' AND created_at > DATE_SUB(NOW(), INTERVAL 7 DAY);
```

---

## 15. Documentación de Cambios

Cada despliegue debe documentarse con:

- Fecha y hora del despliegue
- Versión desplegada (commit hash)
- Cambios incluidos (features, fixes)
- Migraciones de base de datos ejecutadas
- Problemas encontrados durante el despliegue
- Tiempo de downtime (si aplica)

Esta información se mantiene en el historial de commits de Git y en comunicación con el equipo.


---

## 16. Evidencia de Mantenimiento

### 16.1 Historial de Cambios

**Ubicación:** `CHANGELOG.md` en la raíz del proyecto

Contiene registro detallado de:
- Mantenimiento correctivo (fixes)
- Mantenimiento evolutivo (features)
- Migraciones de base de datos
- Actualizaciones de documentación

### 16.2 Commits de Git

**Convención de mensajes:**
- `fix:` - Correcciones de bugs
- `feat:` - Nuevas funcionalidades
- `docs:` - Cambios en documentación
- `refactor:` - Refactorizaciones de código

**Ver historial:**
```bash
git log --oneline --graph --all
```

### 16.3 Auditoría en Base de Datos

**Tabla:** `audit_logs`

**Consultar mantenimiento reciente:**
```sql
SELECT * FROM audit_logs 
WHERE created_at > DATE_SUB(NOW(), INTERVAL 30 DAY)
ORDER BY created_at DESC;
```

### 16.4 Logs de Sistema

**Backups:**
```bash
cat /home/ec2-user/backups/backup.log
```

**Aplicación:**
```bash
sudo journalctl -u pawsoft-backend --since "30 days ago"
```

### 16.5 Métricas de Estabilidad

**Azure Application Insights:** https://portal.azure.com

Visualización de:
- Uptime del sistema
- Tiempos de respuesta
- Tasa de errores
- Uso de recursos

