package biblioteca.modelo;

/** Roles del sistema. Cada rol decide qué módulos puede usar. */
public enum Rol {
    BIBLIOTECARIO(true),
    ESTUDIANTE(false);

    private final boolean gestionTotal;

    Rol(boolean gestionTotal) {
        this.gestionTotal = gestionTotal;
    }

    public boolean tieneGestionTotal() {
        return gestionTotal;
    }

    public static Rol desdeTexto(String texto) {
        return valueOf(texto.trim().toUpperCase());
    }
}
