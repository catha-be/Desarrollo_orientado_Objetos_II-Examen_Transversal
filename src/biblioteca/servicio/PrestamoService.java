package biblioteca.servicio;

import biblioteca.dao.LibroDAO;
import biblioteca.dao.PrestamoDAO;
import biblioteca.modelo.Libro;
import biblioteca.modelo.Prestamo;
import biblioteca.util.DatabaseConnection;

import javax.swing.SwingUtilities;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Lógica de negocio de préstamos y devoluciones.
 *
 * Concurrencia:
 *  - registrarPrestamoAsync ejecuta el proceso en un hilo de fondo (pool), sin congelar la interfaz.
 *  - registrarPrestamo y registrarDevolucion son synchronized: solo un hilo a la vez modifica el stock,
 *    lo que evita condiciones de carrera. Además la operación es una transacción (commit/rollback) y el
 *    descuento de stock es atómico en SQL (WHERE stock > 0).
 */
public class PrestamoService {
    private static final PrestamoService INSTANCIA = new PrestamoService();

    private final LibroDAO libroDAO = new LibroDAO();
    private final PrestamoDAO prestamoDAO = new PrestamoDAO();
    private final ExecutorService pool = Executors.newFixedThreadPool(4, r -> {
        Thread t = new Thread(r, "prestamo-worker");
        t.setDaemon(true);
        return t;
    });

    private PrestamoService() { }

    public static PrestamoService getInstance() {
        return INSTANCIA;
    }

    /** Ejecuta el préstamo en segundo plano y notifica el resultado en el hilo de Swing (EDT). */
    public void registrarPrestamoAsync(int idEstudiante, int idLibro,
                                       Consumer<Prestamo> alExito, Consumer<Exception> alError) {
        pool.submit(() -> {
            try {
                Prestamo p = registrarPrestamo(idEstudiante, idLibro);
                SwingUtilities.invokeLater(() -> alExito.accept(p));
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> alError.accept(e));
            }
        });
    }

    public void registrarDevolucionAsync(int idPrestamo, Consumer<Prestamo> alExito, Consumer<Exception> alError) {
        pool.submit(() -> {
            try {
                Prestamo p = registrarDevolucion(idPrestamo);
                SwingUtilities.invokeLater(() -> alExito.accept(p));
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> alError.accept(e));
            }
        });
    }

    /** Registra el préstamo, descuenta el stock y deja el historial (fila en prestamos), todo en una transacción. */
    public synchronized Prestamo registrarPrestamo(int idEstudiante, int idLibro) {
        synchronized (DatabaseConnection.LOCK) {
            return enTransaccion(() -> {
                Libro libro = libroDAO.buscarPorId(idLibro)
                        .orElseThrow(() -> new IllegalStateException("El libro no existe."));
                if (!libro.hayStock()) {
                    throw new IllegalStateException("Sin stock disponible de \"" + libro.getTitulo() + "\".");
                }
                if (prestamoDAO.tienePrestamoActivo(idEstudiante, idLibro)) {
                    throw new IllegalStateException("El estudiante ya tiene este libro en préstamo.");
                }
                if (!libroDAO.descontarStock(idLibro)) {
                    throw new IllegalStateException("Sin stock disponible de \"" + libro.getTitulo() + "\".");
                }
                Prestamo p = Prestamo.nuevo(idEstudiante, idLibro);
                p.setId(prestamoDAO.crear(p));
                p.setTituloLibro(libro.getTitulo());
                return p;
            });
        }
    }

    /** Registra la devolución, repone el stock y deja constancia del atraso (Prestamo.estaAtrasado). */
    public synchronized Prestamo registrarDevolucion(int idPrestamo) {
        synchronized (DatabaseConnection.LOCK) {
            return enTransaccion(() -> {
                Prestamo p = prestamoDAO.buscarPorId(idPrestamo)
                        .orElseThrow(() -> new IllegalStateException("El préstamo no existe."));
                if (p.isDevuelto()) {
                    throw new IllegalStateException("El préstamo ya fue devuelto.");
                }
                LocalDate hoy = LocalDate.now();
                if (!prestamoDAO.marcarDevuelto(idPrestamo, hoy)) {
                    throw new IllegalStateException("El préstamo ya fue devuelto.");
                }
                libroDAO.incrementarStock(p.getIdLibro());
                p.setDevuelto(true);
                p.setFechaDevolucion(hoy);
                return p;
            });
        }
    }

    private interface Operacion<R> {
        R ejecutar();
    }

    private <R> R enTransaccion(Operacion<R> op) {
        Connection c;
        try {
            c = DatabaseConnection.getInstance().getConnection();
            c.setAutoCommit(false);
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo iniciar la transacción: " + e.getMessage(), e);
        }
        try {
            R resultado = op.ejecutar();
            c.commit();
            return resultado;
        } catch (RuntimeException e) {
            try { c.rollback(); } catch (SQLException ignorada) { /* se reporta el error original */ }
            throw e;
        } catch (SQLException e) {
            throw new IllegalStateException("Error al confirmar la transacción: " + e.getMessage(), e);
        } finally {
            try { c.setAutoCommit(true); } catch (SQLException ignorada) { /* conexión se reabre si es necesario */ }
        }
    }
}
