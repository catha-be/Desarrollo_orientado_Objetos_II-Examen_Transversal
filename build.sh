#!/bin/bash
# Compila y empaqueta BibliotecaEscolar.jar (requiere JDK 17+).
# Coloque mysql-connector-j-*.jar en la carpeta lib/ (https://dev.mysql.com/downloads/connector/j/).
set -e
cd "$(dirname "$0")"
rm -rf out && mkdir out
javac --release 17 -encoding UTF-8 -d out $(find src -name '*.java')
printf 'Main-Class: biblioteca.Main\nClass-Path: lib/mysql-connector-j-26.7.0.jar\n' > out/manifest.txt
jar cfm BibliotecaEscolar.jar out/manifest.txt -C out biblioteca
echo "Generado BibliotecaEscolar.jar"
