package biblioteca.dao;

/** Error de acceso a datos (envuelve SQLException). */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
