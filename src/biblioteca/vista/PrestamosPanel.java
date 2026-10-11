package biblioteca.vista;

import biblioteca.controlador.EstudianteController;
import biblioteca.controlador.LibroController;
import biblioteca.controlador.PrestamoController;
import biblioteca.modelo.Estudiante;
import biblioteca.modelo.Libro;
import biblioteca.modelo.Prestamo;
import biblioteca.modelo.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Registro de préstamos y devoluciones. Las operaciones corren en hilos de fondo (la UI no se congela). */
public class PrestamosPanel extends JPanel implements MainFrame.Actualizable {
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final PrestamoController controller = new PrestamoController();
    private final LibroController libroController = new LibroController();
    private final EstudianteController estudianteController = new EstudianteController();
    private final Usuario usuario;

    private final JComboBox<Estudiante> cmbEstudiante = new JComboBox<>();
    private final JComboBox<Libro> cmbLibro = new JComboBox<>();
    private final JCheckBox chkPendientes = new JCheckBox("Solo pendientes", true);
    private final JProgressBar progreso = new JProgressBar();
    private final JLabel lblEstado = new JLabel(" ");
    private final DefaultTableModel modelo = Ui.modelo("ID", "Estudiante", "Libro", "Préstamo", "Vencimiento", "Devolución", "Estado");
    private final JTable tabla = Ui.tabla(modelo);
    private List<Prestamo> prestamos = List.of();
    private int operacionesEnCurso;

    public PrestamosPanel(Usuario usuario) {
        super(new BorderLayout(8, 8));
        this.usuario = usuario;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton btnPrestar = new JButton("Registrar préstamo");
        JButton btnDevolver = new JButton("Registrar devolución");
        btnPrestar.addActionListener(e -> prestar());
        btnDevolver.addActionListener(e -> devolver());
        chkPendientes.addActionListener(e -> mostrarTabla());

        JPanel norte = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        norte.setBorder(BorderFactory.createTitledBorder("Nuevo préstamo (plazo: " + Prestamo.DIAS_PRESTAMO + " días)"));
        norte.add(new JLabel("Estudiante:"));
        norte.add(cmbEstudiante);
        norte.add(new JLabel("Libro:"));
        norte.add(cmbLibro);
        norte.add(btnPrestar);
        if (usuario.esBibliotecario()) {
            JButton btnSim = new JButton("Prueba de concurrencia");
            btnSim.setToolTipText("Lanza préstamos simultáneos del libro elegido para verificar el control de stock");
            btnSim.addActionListener(e -> pruebaConcurrencia());
            norte.add(btnSim);
        }
        add(norte, BorderLayout.NORTH);

        add(new JScrollPane(tabla), BorderLayout.CENTER);

        progreso.setIndeterminate(true);
        progreso.setVisible(false);
        JPanel sur = new JPanel(new BorderLayout(8, 0));
        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT));
        izq.add(btnDevolver);
        izq.add(chkPendientes);
        sur.add(izq, BorderLayout.WEST);
        sur.add(lblEstado, BorderLayout.CENTER);
        sur.add(progreso, BorderLayout.EAST);
        add(sur, BorderLayout.SOUTH);

        actualizar();
    }

    private Estudiante estudianteActual() {
        return (Estudiante) cmbEstudiante.getSelectedItem();
    }

    private void prestar() {
        Estudiante est = estudianteActual();
        Libro libro = (Libro) cmbLibro.getSelectedItem();
        if (est == null || libro == null) {
            Ui.info(this, "Seleccione un estudiante y un libro.");
            return;
        }
        iniciarOperacion("Registrando préstamo de \"" + libro.getTitulo() + "\"...");
        controller.prestar(est.getId(), libro.getId(),
                p -> {
                    finalizarOperacion("Préstamo registrado. Vence el " + p.getFechaVencimiento().format(FECHA) + ".");
                    actualizar();
                },
                ex -> {
                    finalizarOperacion("No se pudo registrar el préstamo.");
                    Ui.error(this, ex);
                    actualizar();
                });
    }

    private void devolver() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Ui.info(this, "Seleccione un préstamo de la tabla.");
            return;
        }
        int id = (int) modelo.getValueAt(tabla.convertRowIndexToModel(fila), 0);
        iniciarOperacion("Registrando devolución...");
        controller.devolver(id,
                p -> {
                    String msg = p.estaAtrasado()
                            ? "Devolución registrada CON ATRASO de " + p.diasAtraso() + " día(s)."
                            : "Devolución registrada a tiempo.";
                    finalizarOperacion(msg);
                    if (p.estaAtrasado()) Ui.info(this, msg);
                    actualizar();
                },
                ex -> {
                    finalizarOperacion("No se pudo registrar la devolución.");
                    Ui.error(this, ex);
                    actualizar();
                });
    }

    /** Dispara un préstamo del mismo libro por cada estudiante, todos a la vez, y resume el resultado. */
    private void pruebaConcurrencia() {
        Libro libro = (Libro) cmbLibro.getSelectedItem();
        if (libro == null) {
            Ui.info(this, "Seleccione un libro.");
            return;
        }
        int total = cmbEstudiante.getItemCount();
        int[] resultados = new int[3]; // [0]=exitosos, [1]=rechazados, [2]=terminados
        StringBuilder detalle = new StringBuilder();
        int stockInicial = libro.getStock();
        for (int i = 0; i < total; i++) {
            Estudiante est = cmbEstudiante.getItemAt(i);
            iniciarOperacion("Prueba de concurrencia: " + total + " préstamos simultáneos...");
            controller.prestar(est.getId(), libro.getId(),
                    p -> terminarSimulacion(resultados, true, null, detalle, total, libro, stockInicial),
                    ex -> terminarSimulacion(resultados, false, ex.getMessage(), detalle, total, libro, stockInicial));
        }
    }

    private void terminarSimulacion(int[] r, boolean ok, String motivo, StringBuilder detalle,
                                    int total, Libro libro, int stockInicial) {
        finalizarOperacion("Prueba de concurrencia en curso...");
        if (ok) r[0]++; else { r[1]++; detalle.append("\n - ").append(motivo); }
        if (++r[2] == total) {
            actualizar();
            lblEstado.setText("Prueba finalizada.");
            Ui.info(this, "Libro: " + libro.getTitulo() + "\nStock inicial: " + stockInicial
                    + "\nPréstamos solicitados: " + total
                    + "\nPréstamos aceptados: " + r[0]
                    + "\nPréstamos rechazados: " + r[1]
                    + "\n\nEl stock nunca queda negativo: se aceptan como máximo " + stockInicial + "."
                    + (r[1] > 0 ? "\n\nMotivos:" + detalle : ""));
        }
    }

    private void iniciarOperacion(String mensaje) {
        operacionesEnCurso++;
        progreso.setVisible(true);
        lblEstado.setText(mensaje);
    }

    private void finalizarOperacion(String mensaje) {
        if (operacionesEnCurso > 0) operacionesEnCurso--;
        progreso.setVisible(operacionesEnCurso > 0);
        lblEstado.setText(mensaje);
    }

    private void mostrarTabla() {
        modelo.setRowCount(0);
        for (Prestamo p : prestamos) {
            if (chkPendientes.isSelected() && p.isDevuelto()) continue;
            modelo.addRow(new Object[]{p.getId(), p.getNombreEstudiante(), p.getTituloLibro(),
                    p.getFechaPrestamo().format(FECHA),
                    p.getFechaVencimiento().format(FECHA),
                    p.isDevuelto() ? p.getFechaDevolucion().format(FECHA) : "-",
                    p.getEstado()});
        }
    }

    @Override
    public void actualizar() {
        try {
            Object libroSel = cmbLibro.getSelectedItem();
            cmbLibro.removeAllItems();
            libroController.listar().stream().filter(Libro::hayStock).forEach(cmbLibro::addItem);
            if (libroSel instanceof Libro l) {
                for (int i = 0; i < cmbLibro.getItemCount(); i++) {
                    if (cmbLibro.getItemAt(i).getId() == l.getId()) cmbLibro.setSelectedIndex(i);
                }
            }

            cmbEstudiante.removeAllItems();
            if (usuario.esBibliotecario()) {
                estudianteController.listar().forEach(cmbEstudiante::addItem);
                prestamos = controller.listar();
            } else {
                // El estudiante solo opera sobre sus propios préstamos
                var propio = estudianteController.buscarDeUsuario(usuario);
                propio.ifPresent(cmbEstudiante::addItem);
                cmbEstudiante.setEnabled(false);
                prestamos = propio.map(e -> controller.listarPorEstudiante(e.getId())).orElse(List.of());
                if (propio.isEmpty()) lblEstado.setText("Su usuario no está asociado a un estudiante registrado.");
            }
            mostrarTabla();
        } catch (Exception ex) {
            Ui.error(this, ex);
        }
    }
}
