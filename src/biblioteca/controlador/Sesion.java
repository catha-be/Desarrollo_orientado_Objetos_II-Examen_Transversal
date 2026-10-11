package biblioteca.controlador;

import biblioteca.modelo.Usuario;

/** Usuario autenticado de la aplicación. */
public final class Sesion {
    private static Usuario usuario;

    private Sesion() { }

    public static Usuario getUsuario() { return usuario; }
    public static void iniciar(Usuario u) { usuario = u; }
    public static void cerrar() { usuario = null; }
}
