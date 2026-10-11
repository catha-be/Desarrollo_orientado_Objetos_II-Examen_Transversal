package biblioteca.util;

import java.util.regex.Pattern;

/** Validaciones de formulario reutilizables. */
public final class Validador {
    private static final Pattern RUT = Pattern.compile("^\\d{1,2}\\.?\\d{3}\\.?\\d{3}-[\\dkK]$");
    private static final Pattern CORREO = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    private Validador() { }

    public static void requerido(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo '" + campo + "' es obligatorio.");
        }
    }

    public static void rut(String valor) {
        requerido(valor, "RUT");
        if (!RUT.matcher(valor.trim()).matches()) {
            throw new IllegalArgumentException("RUT inválido. Formato esperado: 12345678-9");
        }
    }

    public static void correo(String valor) {
        requerido(valor, "Correo");
        if (!CORREO.matcher(valor.trim()).matches()) {
            throw new IllegalArgumentException("Correo electrónico inválido.");
        }
    }

    public static int entero(String valor, String campo) {
        requerido(valor, campo);
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El campo '" + campo + "' debe ser un número entero.");
        }
    }
}
