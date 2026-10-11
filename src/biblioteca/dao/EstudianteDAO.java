package biblioteca.dao;

import biblioteca.modelo.Estudiante;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class EstudianteDAO extends BaseDAO<Estudiante> implements CrudDAO<Estudiante> {
    private static final String SELECT = "SELECT id, nombre, rut, curso, correo FROM estudiantes ";

    @Override
    protected Estudiante mapear(ResultSet rs) throws SQLException {
        return new Estudiante(rs.getInt("id"), rs.getString("nombre"), rs.getString("rut"),
                rs.getString("curso"), rs.getString("correo"));
    }

    @Override
    public int crear(Estudiante e) {
        return insertarSql("INSERT INTO estudiantes (nombre, rut, curso, correo) VALUES (?, ?, ?, ?)",
                e.getNombre(), e.getRut(), e.getCurso(), e.getCorreo());
    }

    @Override
    public Optional<Estudiante> buscarPorId(int id) {
        return consultar(SELECT + "WHERE id = ?", id).stream().findFirst();
    }

    public Optional<Estudiante> buscarPorRutOCorreo(String rut, String correo) {
        return consultar(SELECT + "WHERE rut = ? OR correo = ? LIMIT 1", rut, correo).stream().findFirst();
    }

    @Override
    public List<Estudiante> listar() {
        return consultar(SELECT + "ORDER BY nombre");
    }

    @Override
    public boolean actualizar(Estudiante e) {
        return actualizarSql("UPDATE estudiantes SET nombre = ?, rut = ?, curso = ?, correo = ? WHERE id = ?",
                e.getNombre(), e.getRut(), e.getCurso(), e.getCorreo(), e.getId()) > 0;
    }

    @Override
    public boolean eliminar(int id) {
        return actualizarSql("DELETE FROM estudiantes WHERE id = ?", id) > 0;
    }
}
