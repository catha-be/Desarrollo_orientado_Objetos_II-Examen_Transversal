package biblioteca.controlador;

import biblioteca.dao.PrestamoDAO;
import biblioteca.modelo.Prestamo;
import biblioteca.servicio.PrestamoService;

import java.util.List;
import java.util.function.Consumer;

public class PrestamoController {
    private final PrestamoDAO dao = new PrestamoDAO();
    private final PrestamoService servicio = PrestamoService.getInstance();

    public List<Prestamo> listar() { return dao.listar(); }

    public List<Prestamo> listarPorEstudiante(int idEstudiante) { return dao.listarPorEstudiante(idEstudiante); }

    public void prestar(int idEstudiante, int idLibro, Consumer<Prestamo> ok, Consumer<Exception> error) {
        servicio.registrarPrestamoAsync(idEstudiante, idLibro, ok, error);
    }

    public void devolver(int idPrestamo, Consumer<Prestamo> ok, Consumer<Exception> error) {
        servicio.registrarDevolucionAsync(idPrestamo, ok, error);
    }
}
