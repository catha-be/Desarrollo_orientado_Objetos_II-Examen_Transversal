package biblioteca.controlador;

import biblioteca.dao.CategoriaDAO;
import biblioteca.modelo.Categoria;
import biblioteca.util.Validador;

import java.util.List;

public class CategoriaController {
    private final CategoriaDAO dao = new CategoriaDAO();

    public List<Categoria> listar() { return dao.listar(); }

    public void crear(String nombre) {
        Validador.requerido(nombre, "Nombre");
        dao.crear(new Categoria(0, nombre.trim()));
    }

    public void modificar(int id, String nombre) {
        Validador.requerido(nombre, "Nombre");
        dao.actualizar(new Categoria(id, nombre.trim()));
    }

    public void eliminar(int id) { dao.eliminar(id); }
}
