package biblioteca.dao;

import java.util.List;
import java.util.Optional;

/** Contrato CRUD común a todos los DAO. */
public interface CrudDAO<T> {
    /** Inserta y devuelve el id generado. */
    int crear(T entidad);

    Optional<T> buscarPorId(int id);

    List<T> listar();

    boolean actualizar(T entidad);

    boolean eliminar(int id);
}
