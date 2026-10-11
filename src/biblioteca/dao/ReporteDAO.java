package biblioteca.dao;

import biblioteca.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Consultas de solo lectura para los reportes. Devuelve datos tabulares genéricos. */
public class ReporteDAO {

    public static class Resultado {
        public final String[] columnas;
        public final List<Object[]> filas = new ArrayList<>();

        Resultado(String[] columnas) {
            this.columnas = columnas;
        }
    }

    public Resultado librosMasPrestados() {
        return ejecutar(new String[]{"Título", "Autor", "Categoría", "Veces prestado"},
                "SELECT l.titulo, l.autor, c.nombre, COUNT(p.id) AS total "
              + "FROM libros l JOIN categorias c ON c.id = l.id_categoria JOIN prestamos p ON p.id_libro = l.id "
              + "GROUP BY l.id, l.titulo, l.autor, c.nombre ORDER BY total DESC, l.titulo LIMIT 10");
    }

    public Resultado historialPorEstudiante(int idEstudiante) {
        return ejecutar(new String[]{"Título", "Fecha préstamo", "Vencimiento", "Fecha devolución", "Estado"},
                "SELECT l.titulo, p.fecha_prestamo, DATE_ADD(p.fecha_prestamo, INTERVAL 7 DAY), "
              + "CASE WHEN p.devuelto THEN p.fecha_devolucion ELSE NULL END, "
              + "CASE WHEN p.devuelto THEN IF(p.fecha_devolucion > DATE_ADD(p.fecha_prestamo, INTERVAL 7 DAY), 'Devuelto con atraso', 'Devuelto') "
              + "     ELSE IF(CURDATE() > DATE_ADD(p.fecha_prestamo, INTERVAL 7 DAY), 'ATRASADO', 'Vigente') END "
              + "FROM prestamos p JOIN libros l ON l.id = p.id_libro "
              + "WHERE p.id_estudiante = ? ORDER BY p.fecha_prestamo DESC", idEstudiante);
    }

    public Resultado librosEnPrestamo() {
        return ejecutar(new String[]{"Título", "Estudiante", "Curso", "Fecha préstamo", "Vencimiento", "Estado"},
                "SELECT l.titulo, e.nombre, e.curso, p.fecha_prestamo, p.fecha_devolucion, "
              + "IF(CURDATE() > p.fecha_devolucion, 'ATRASADO', 'Vigente') "
              + "FROM prestamos p JOIN libros l ON l.id = p.id_libro JOIN estudiantes e ON e.id = p.id_estudiante "
              + "WHERE p.devuelto = FALSE ORDER BY p.fecha_devolucion");
    }

    private Resultado ejecutar(String[] columnas, String sql, Object... params) {
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                Resultado r = new Resultado(columnas);
                while (rs.next()) {
                    Object[] fila = new Object[columnas.length];
                    for (int i = 0; i < fila.length; i++) fila[i] = rs.getObject(i + 1);
                    r.filas.add(fila);
                }
                return r;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al generar el reporte: " + e.getMessage(), e);
        }
    }
}
