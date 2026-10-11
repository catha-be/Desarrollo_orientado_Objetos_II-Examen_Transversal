package biblioteca.dao;

import biblioteca.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Utilidades JDBC compartidas. Cada DAO concreto solo define SQL y el mapeo fila -> objeto. */
public abstract class BaseDAO<T> {

    protected abstract T mapear(ResultSet rs) throws SQLException;

    protected Connection conexion() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    protected List<T> consultar(String sql, Object... params) {
        try (PreparedStatement ps = conexion().prepareStatement(sql)) {
            asignar(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> lista = new ArrayList<>();
                while (rs.next()) lista.add(mapear(rs));
                return lista;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al consultar: " + e.getMessage(), e);
        }
    }

    protected int actualizarSql(String sql, Object... params) {
        synchronized (DatabaseConnection.LOCK) {
            try (PreparedStatement ps = conexion().prepareStatement(sql)) {
                asignar(ps, params);
                return ps.executeUpdate();
            } catch (SQLException e) {
                throw new DataAccessException(traducir(e), e);
            }
        }
    }

    protected int insertarSql(String sql, Object... params) {
        synchronized (DatabaseConnection.LOCK) {
            try (PreparedStatement ps = conexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                asignar(ps, params);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    return keys.next() ? keys.getInt(1) : 0;
                }
            } catch (SQLException e) {
                throw new DataAccessException(traducir(e), e);
            }
        }
    }

    private static void asignar(PreparedStatement ps, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object p = params[i];
            if (p instanceof LocalDate d) ps.setDate(i + 1, Date.valueOf(d));
            else if (p instanceof Enum<?> e) ps.setString(i + 1, e.name());
            else ps.setObject(i + 1, p);
        }
    }

    private static String traducir(SQLException e) {
        if (e instanceof SQLIntegrityConstraintViolationException) {
            String m = e.getMessage() == null ? "" : e.getMessage();
            if (m.contains("Duplicate")) return "Ya existe un registro con ese valor único (RUT, correo, ISBN o nombre).";
            if (m.contains("foreign key")) return "No se puede completar: el registro está relacionado con otros datos.";
        }
        return "Error de base de datos: " + e.getMessage();
    }
}
