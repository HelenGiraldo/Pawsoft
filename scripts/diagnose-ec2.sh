#!/bin/bash
# Script de diagnóstico para EC2 - Pawsoft Backend

echo "=== DIAGNÓSTICO PAWSOFT BACKEND EN EC2 ==="
echo ""

echo "1. Verificando si Java está instalado..."
java -version
echo ""

echo "2. Verificando procesos Java en ejecución..."
ps aux | grep java | grep -v grep
echo ""

echo "3. Verificando logs de la aplicación..."
if [ -f /home/ec2-user/app.log ]; then
    echo "Últimas 50 líneas del log:"
    tail -n 50 /home/ec2-user/app.log
else
    echo "No se encontró /home/ec2-user/app.log"
fi
echo ""

echo "4. Verificando archivo application.properties..."
if [ -f /home/ec2-user/application.properties ]; then
    echo "Archivo encontrado. Mostrando configuración (sin contraseñas):"
    grep -v "password\|secret\|key" /home/ec2-user/application.properties | head -20
else
    echo "No se encontró /home/ec2-user/application.properties"
fi
echo ""

echo "5. Verificando conectividad a base de datos..."
echo "Intentando conectar a la base de datos..."
# Extraer host de application.properties si existe
if [ -f /home/ec2-user/application.properties ]; then
    DB_HOST=$(grep "spring.datasource.url" /home/ec2-user/application.properties | cut -d'/' -f3 | cut -d':' -f1)
    if [ ! -z "$DB_HOST" ]; then
        echo "Probando conexión a $DB_HOST:3306..."
        nc -zv $DB_HOST 3306 2>&1
    fi
fi
echo ""

echo "6. Verificando puerto 8080..."
netstat -tuln | grep 8080
echo ""

echo "=== FIN DEL DIAGNÓSTICO ==="
