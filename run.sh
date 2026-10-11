#!/bin/bash
# Ejecuta la aplicación (usa db.properties del directorio actual).
cd "$(dirname "$0")"
[ -f BibliotecaEscolar.jar ] || ./build.sh
CONN=$(ls lib/mysql-connector-j*.jar 2>/dev/null | head -1)
[ -z "$CONN" ] && { echo "Falta lib/mysql-connector-j-*.jar"; exit 1; }
java -cp "BibliotecaEscolar.jar:$CONN" biblioteca.Main
