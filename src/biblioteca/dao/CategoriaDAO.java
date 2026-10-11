package biblioteca.dao;

import biblioteca.modelo.Categoria;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class CategoriaDAO extends BaseDAO<Categoria> implements CrudDAO<Categoria> {

    @Override
    protected Categoria mapear(ResultSet rs) throws SQLException {
        return new Categoria(rs.getInt("id"), rs.getString("nombre"));
    }

    @Override
    public int crear(Categoria c) {
        return insertarSql("INSERT INTO categorias (nombre) VALUES (?)", c.getNombre());
    }

    @Override
    public Optional<Categoria> buscarPorId(int id) {
        return consultar("SELECT id, nombre FROM categorias WHERE id = ?", id).stream().findFirst();
    }

    @Override
    public List<Categoria> listar() {
        return consultar("SELECT id, nombre FROM categorias ORDER BY nombre");
    }

    @Override
    public boolean actualizar(Categoria c) {
        return actualizarSql("UPDATE categorias SET nombre = ? WHERE id = ?", c.getNombre(), c.getId()) > 0;
    }

    @Override
    public boolean eliminar(int id) {
        return actualizarSql("DELETE FROM categorias WHERE id = ?", id) > 0;
    }
}
