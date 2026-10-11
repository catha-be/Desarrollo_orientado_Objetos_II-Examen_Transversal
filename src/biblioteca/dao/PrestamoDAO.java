package biblioteca.dao;

import biblioteca.modelo.Prestamo;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class PrestamoDAO extends BaseDAO<Prestamo> implements CrudDAO<Prestamo> {
    private static final String SELECT =
            "SELECT p.id, p.id_estudiante, p.id_libro, p.fecha_prestamo, p.fecha_devolucion, p.devuelto, "
          + "l.titulo, e.nombre AS estudiante "
          + "FROM prestamos p JOIN libros l ON l.id = p.id_libro JOIN estudiantes e ON e.id = p.id_estudiante ";

    @Override
    protected Prestamo mapear(ResultSet rs) throws SQLException {
        Prestamo p = new Prestamo();
        p.setId(rs.getInt("id"));
        p.setIdEstudiante(rs.getInt("id_estudiante"));
        p.setIdLibro(rs.getInt("id_libro"));
        p.setFechaPrestamo(rs.getDate("fecha_prestamo").toLocalDate());
        Date dev = rs.getDate("fecha_devolucion");
        p.setFechaDevolucion(dev == null ? null : dev.toLocalDate());
        p.setDevuelto(rs.getBoolean("devuelto"));
        p.setTituloLibro(rs.getString("titulo"));
        p.setNombreEstudiante(rs.getString("estudiante"));
        return p;
    }

    @Override
    public int crear(Prestamo p) {
        return insertarSql("INSERT INTO prestamos (id_estudiante, id_libro, fecha_prestamo, fecha_devolucion, devuelto) VALUES (?, ?, ?, ?, ?)",
                p.getIdEstudiante(), p.getIdLibro(), p.getFechaPrestamo(), p.getFechaDevolucion(), p.isDevuelto());
    }

    @Override
    public Optional<Prestamo> buscarPorId(int id) {
        return consultar(SELECT + "WHERE p.id = ?", id).stream().findFirst();
    }

    @Override
    public List<Prestamo> listar() {
        return consultar(SELECT + "ORDER BY p.devuelto, p.fecha_prestamo DESC");
    }

    public List<Prestamo> listarPorEstudiante(int idEstudiante) {
        return consultar(SELECT + "WHERE p.id_estudiante = ? ORDER BY p.devuelto, p.fecha_prestamo DESC", idEstudiante);
    }

    public boolean tienePrestamoActivo(int idEstudiante, int idLibro) {
        return !consultar(SELECT + "WHERE p.id_estudiante = ? AND p.id_libro = ? AND p.devuelto = FALSE",
                idEstudiante, idLibro).isEmpty();
    }

    @Override
    public boolean actualizar(Prestamo p) {
        return actualizarSql("UPDATE prestamos SET id_estudiante = ?, id_libro = ?, fecha_prestamo = ?, fecha_devolucion = ?, devuelto = ? WHERE id = ?",
                p.getIdEstudiante(), p.getIdLibro(), p.getFechaPrestamo(), p.getFechaDevolucion(), p.isDevuelto(), p.getId()) > 0;
    }

    /** Marca como devuelto y deja la fecha real de devolución. Solo afecta préstamos pendientes. */
    public boolean marcarDevuelto(int id, LocalDate fechaReal) {
        return actualizarSql("UPDATE prestamos SET devuelto = TRUE, fecha_devolucion = ? WHERE id = ? AND devuelto = FALSE",
                fechaReal, id) > 0;
    }

    @Override
    public boolean eliminar(int id) {
        return actualizarSql("DELETE FROM prestamos WHERE id = ?", id) > 0;
    }
}
