package biblioteca.dao;

import biblioteca.modelo.Libro;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class LibroDAO extends BaseDAO<Libro> implements CrudDAO<Libro> {
    private static final String SELECT =
            "SELECT l.id, l.titulo, l.autor, l.isbn, l.editorial, l.stock, l.id_categoria, c.nombre AS categoria "
          + "FROM libros l JOIN categorias c ON c.id = l.id_categoria ";

    @Override
    protected Libro mapear(ResultSet rs) throws SQLException {
        Libro l = new Libro(rs.getInt("id"), rs.getString("titulo"), rs.getString("autor"), rs.getString("isbn"),
                rs.getString("editorial"), rs.getInt("stock"), rs.getInt("id_categoria"));
        l.setNombreCategoria(rs.getString("categoria"));
        return l;
    }

    @Override
    public int crear(Libro l) {
        return insertarSql("INSERT INTO libros (titulo, autor, isbn, editorial, stock, id_categoria) VALUES (?, ?, ?, ?, ?, ?)",
                l.getTitulo(), l.getAutor(), l.getIsbn(), l.getEditorial(), l.getStock(), l.getIdCategoria());
    }

    @Override
    public Optional<Libro> buscarPorId(int id) {
        return consultar(SELECT + "WHERE l.id = ?", id).stream().findFirst();
    }

    @Override
    public List<Libro> listar() {
        return consultar(SELECT + "ORDER BY l.titulo");
    }

    @Override
    public boolean actualizar(Libro l) {
        return actualizarSql("UPDATE libros SET titulo = ?, autor = ?, isbn = ?, editorial = ?, stock = ?, id_categoria = ? WHERE id = ?",
                l.getTitulo(), l.getAutor(), l.getIsbn(), l.getEditorial(), l.getStock(), l.getIdCategoria(), l.getId()) > 0;
    }

    @Override
    public boolean eliminar(int id) {
        return actualizarSql("DELETE FROM libros WHERE id = ?", id) > 0;
    }

    /** Descuento atómico: solo ocurre si queda stock. Devuelve false si no había ejemplares. */
    public boolean descontarStock(int idLibro) {
        return actualizarSql("UPDATE libros SET stock = stock - 1 WHERE id = ? AND stock > 0", idLibro) > 0;
    }

    public void incrementarStock(int idLibro) {
        actualizarSql("UPDATE libros SET stock = stock + 1 WHERE id = ?", idLibro);
    }
}
