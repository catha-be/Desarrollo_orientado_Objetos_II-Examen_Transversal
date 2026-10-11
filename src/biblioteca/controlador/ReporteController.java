package biblioteca.controlador;

import biblioteca.dao.ReporteDAO;

public class ReporteController {
    private final ReporteDAO dao = new ReporteDAO();

    public ReporteDAO.Resultado librosMasPrestados() { return dao.librosMasPrestados(); }
    public ReporteDAO.Resultado historialPorEstudiante(int idEstudiante) { return dao.historialPorEstudiante(idEstudiante); }
    public ReporteDAO.Resultado librosEnPrestamo() { return dao.librosEnPrestamo(); }
}
