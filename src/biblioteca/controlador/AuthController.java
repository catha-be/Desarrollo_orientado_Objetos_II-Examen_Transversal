package biblioteca.controlador;

import biblioteca.dao.UsuarioDAO;
import biblioteca.modelo.Usuario;
import biblioteca.util.Seguridad;
import biblioteca.util.Validador;

public class AuthController {
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    /**
     * Valida credenciales contra la BD (correo o RUT + contraseña). La contraseña puede estar guardada
     * como SHA-256 (script entregado) o en texto plano (scripts externos).
     */
    public Usuario autenticar(String identificador, String contrasena) {
        Validador.requerido(identificador, "Correo o RUT");
        Validador.requerido(contrasena, "Contraseña");
        Usuario u = usuarioDAO.buscarPorCorreoORut(identificador.trim())
                .orElseThrow(() -> new IllegalArgumentException("Usuario o contraseña incorrectos."));
        String guardada = u.getContrasena();
        if (!guardada.equals(contrasena) && !guardada.equalsIgnoreCase(Seguridad.sha256(contrasena))) {
            throw new IllegalArgumentException("Usuario o contraseña incorrectos.");
        }
        Sesion.iniciar(u);
        return u;
    }
}
