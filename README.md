# Sistema de Gestión de Biblioteca Escolar

Aplicación de escritorio en Java (Swing/JFrame + JDBC + MySQL). Examen Final Transversal – Desarrollo Orientado a Objetos II.

## Arquitectura
| Capa | Paquete | Contenido |
|---|---|---|
| Modelo | `biblioteca.modelo` | `Persona` (abstracta) → `Usuario`, `Estudiante`; `Libro`, `Categoria`, `Prestamo`, `Rol` |
| DAO | `biblioteca.dao` | `CrudDAO<T>` (interfaz), `BaseDAO<T>` (abstracta), un DAO por entidad, `ReporteDAO` |
| Servicio | `biblioteca.servicio` | `PrestamoService` (hilos + `synchronized` + transacción) |
| Controlador | `biblioteca.controlador` | un controlador por módulo + `Sesion` |
| Vista | `biblioteca.vista` | `LoginFrame`, `MainFrame` (menú de navegación) y un panel por módulo |
| Util | `biblioteca.util` | `DatabaseConnection` (**Singleton**), `Validador`, `Seguridad` |

- **MVC / DAO / Singleton**: ver tabla. `DatabaseConnection.getInstance()` entrega la única conexión JDBC.
- **POO**: herencia (`Usuario`/`Estudiante` extienden `Persona`), polimorfismo (`getTipo()`), clase abstracta (`Persona`, `BaseDAO`), interfaz (`CrudDAO`).
- **Concurrencia**: `PrestamoService.registrarPrestamoAsync` ejecuta préstamo + descuento de stock + historial en un hilo de fondo; los métodos `registrarPrestamo`/`registrarDevolucion` son `synchronized`, transaccionales y el descuento de stock es atómico (`WHERE stock > 0`). El botón **Prueba de concurrencia** (bibliotecario) lanza préstamos simultáneos del mismo libro para demostrar que el stock no queda inconsistente.
- **Vencimiento / atraso**: plazo de 7 días (`Prestamo.DIAS_PRESTAMO`); en `prestamos.fecha_devolucion` se guarda el vencimiento mientras está pendiente y la fecha real al devolver. El atraso se muestra en la tabla y se avisa al devolver.

## Puesta en marcha
1. Ejecutar en MySQL, en orden, `sql/PRY2203_EFT_S9_Script_crea_tablas_biblioteca.sql` (crea la base `biblioteca`) y luego `sql/PRY2203_EFT_S9_Script_poblado_tablas_biblioteca.sql`.
2. Ajustar `db.properties` (URL, usuario, clave).
3. IntelliJ IDEA: abrir la carpeta como proyecto Maven (`pom.xml` descarga MySQL Connector/J) y ejecutar `biblioteca.Main`.
   Sin Maven: copiar `mysql-connector-j-*.jar` a `lib/`, luego `./build.sh` y `./run.sh`.

## Usuarios de prueba (datos del script oficial)
| Rol | Usuario | Contraseña |
|---|---|---|
| Bibliotecario | antonia@correo.cl | clave123 |
| Estudiante | carlos@correo.cl | clave123 |

Se puede ingresar con correo o RUT. Los estudiantes nuevos reciben una cuenta con su RUT como clave inicial (guardada como SHA-256; el login acepta contraseñas en texto plano o SHA-256).
