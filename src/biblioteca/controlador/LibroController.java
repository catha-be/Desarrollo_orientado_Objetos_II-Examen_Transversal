package biblioteca.controlador;

import biblioteca.dao.LibroDAO;
import biblioteca.modelo.Libro;
import biblioteca.util.Validador;

import java.util.List;

public class LibroController {
    private final LibroDAO dao = new LibroDAO();

    public List<Libro> listar() { return dao.listar(); }

    public void crear(String titulo, String autor, String isbn, String editorial, String stock, int idCategoria) {
        dao.crear(armar(0, titulo, autor, isbn, editorial, stock, idCategoria));
    }

    public void modificar(int id, String titulo, String autor, String isbn, String editorial, String stock, int idCategoria) {
        if (!dao.actualizar(armar(id, titulo, autor, isbn, editorial, stock, idCategoria))) {
            throw new IllegalStateException("El libro ya no existe.");
        }
    }

    public void eliminar(int id) { dao.eliminar(id); }

    private Libro armar(int id, String titulo, String autor, String isbn, String editorial, String stock, int idCategoria) {
        Validador.requerido(titulo, "Título");
        Validador.requerido(autor, "Autor");
        Validador.requerido(isbn, "ISBN");
        Validador.requerido(editorial, "Editorial");
        int s = Validador.entero(stock, "Stock");
        if (s < 0) throw new IllegalArgumentException("El stock no puede ser negativo.");
        if (idCategoria <= 0) throw new IllegalArgumentException("Seleccione una categoría.");
        return new Libro(id, titulo.trim(), autor.trim(), isbn.trim(), editorial.trim(), s, idCategoria);
    }
}
