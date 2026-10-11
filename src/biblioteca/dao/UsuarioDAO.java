package biblioteca.dao;

import biblioteca.modelo.Rol;
import biblioteca.modelo.Usuario;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class UsuarioDAO extends BaseDAO<Usuario> implements CrudDAO<Usuario> {
    private static final String SELECT = "SELECT id, nombre, rut, correo, `contraseña`, rol FROM usuarios ";

    @Override
    protected Usuario mapear(ResultSet rs) throws SQLException {
        return new Usuario(rs.getInt("id"), rs.getString("nombre"), rs.getString("rut"),
                rs.getString("correo"), rs.getString("contraseña"), Rol.desdeTexto(rs.getString("rol")));
    }

    /** Busca por correo o RUT; la comparación de contraseña la hace el controlador. */
    public Optional<Usuario> buscarPorCorreoORut(String identificador) {
        return consultar(SELECT + "WHERE correo = ? OR rut = ? LIMIT 1", identificador, identificador)
                .stream().findFirst();
    }

    public Optional<Usuario> buscarPorRut(String rut) {
        return consultar(SELECT + "WHERE rut = ?", rut).stream().findFirst();
    }

    @Override
    public int crear(Usuario u) {
        return insertarSql("INSERT INTO usuarios (nombre, rut, correo, `contraseña`, rol) VALUES (?, ?, ?, ?, ?)",
                u.getNombre(), u.getRut(), u.getCorreo(), u.getContrasena(), u.getRol());
    }

    @Override
    public Optional<Usuario> buscarPorId(int id) {
        return consultar(SELECT + "WHERE id = ?", id).stream().findFirst();
    }

    @Override
    public List<Usuario> listar() {
        return consultar(SELECT + "ORDER BY nombre");
    }

    @Override
    public boolean actualizar(Usuario u) {
        return actualizarSql("UPDATE usuarios SET nombre = ?, rut = ?, correo = ?, `contraseña` = ?, rol = ? WHERE id = ?",
                u.getNombre(), u.getRut(), u.getCorreo(), u.getContrasena(), u.getRol(), u.getId()) > 0;
    }

    @Override
    public boolean eliminar(int id) {
        return actualizarSql("DELETE FROM usuarios WHERE id = ?", id) > 0;
    }

    public boolean eliminarPorRut(String rut) {
        return actualizarSql("DELETE FROM usuarios WHERE rut = ? AND rol = 'ESTUDIANTE'", rut) > 0;
    }
}
