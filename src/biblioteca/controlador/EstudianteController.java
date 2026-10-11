package biblioteca.controlador;

import biblioteca.dao.EstudianteDAO;
import biblioteca.dao.UsuarioDAO;
import biblioteca.modelo.Estudiante;
import biblioteca.modelo.Rol;
import biblioteca.modelo.Usuario;
import biblioteca.util.Seguridad;
import biblioteca.util.Validador;

import java.util.List;
import java.util.Optional;

public class EstudianteController {
    private final EstudianteDAO dao = new EstudianteDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public List<Estudiante> listar() { return dao.listar(); }

    /** Registra al estudiante y su cuenta de acceso (rol ESTUDIANTE, contraseña inicial = su RUT). */
    public void registrar(String nombre, String rut, String curso, String correo) {
        Estudiante e = armar(0, nombre, rut, curso, correo);
        dao.crear(e);
        if (usuarioDAO.buscarPorRut(e.getRut()).isEmpty()) {
            usuarioDAO.crear(new Usuario(0, e.getNombre(), e.getRut(), e.getCorreo(),
                    Seguridad.sha256(e.getRut()), Rol.ESTUDIANTE));
        }
    }

    public void modificar(int id, String nombre, String rut, String curso, String correo) {
        if (!dao.actualizar(armar(id, nombre, rut, curso, correo))) {
            throw new IllegalStateException("El estudiante ya no existe.");
        }
    }

    public void eliminar(Estudiante e) {
        dao.eliminar(e.getId());               // falla si tiene préstamos (FK): se conserva el historial
        usuarioDAO.eliminarPorRut(e.getRut());
    }

    /** Estudiante asociado al usuario autenticado (por RUT o correo). */
    public Optional<Estudiante> buscarDeUsuario(Usuario u) {
        return dao.buscarPorRutOCorreo(u.getRut(), u.getCorreo());
    }

    private Estudiante armar(int id, String nombre, String rut, String curso, String correo) {
        Validador.requerido(nombre, "Nombre");
        Validador.rut(rut);
        Validador.requerido(curso, "Curso");
        Validador.correo(correo);
        return new Estudiante(id, nombre.trim(), rut.trim(), curso.trim(), correo.trim());
    }
}
