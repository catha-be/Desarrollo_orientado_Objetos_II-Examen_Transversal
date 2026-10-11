package biblioteca.vista;

import biblioteca.controlador.EstudianteController;
import biblioteca.controlador.ReporteController;
import biblioteca.dao.ReporteDAO;
import biblioteca.modelo.Estudiante;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ReportesPanel extends JPanel implements MainFrame.Actualizable {
    private final ReporteController controller = new ReporteController();
    private final EstudianteController estudiantes = new EstudianteController();
    private final DefaultTableModel modelo = new DefaultTableModel();
    private final JTable tabla = Ui.tabla(modelo);
    private final JComboBox<Estudiante> cmbEstudiante = new JComboBox<>();
    private final JLabel lblTitulo = new JLabel("Seleccione un reporte");

    public ReportesPanel() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton masPrestados = new JButton("Libros más prestados");
        JButton enPrestamo = new JButton("Libros actualmente en préstamo");
        JButton historial = new JButton("Historial del estudiante");
        masPrestados.addActionListener(e -> mostrar("Libros más prestados (top 10)", controller::librosMasPrestados));
        enPrestamo.addActionListener(e -> mostrar("Libros actualmente en préstamo", controller::librosEnPrestamo));
        historial.addActionListener(e -> {
            Estudiante s = (Estudiante) cmbEstudiante.getSelectedItem();
            if (s == null) {
                Ui.info(this, "Seleccione un estudiante.");
                return;
            }
            mostrar("Historial de " + s.getNombre(), () -> controller.historialPorEstudiante(s.getId()));
        });

        JPanel norte = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        norte.add(masPrestados);
        norte.add(enPrestamo);
        norte.add(new JLabel("   Estudiante:"));
        norte.add(cmbEstudiante);
        norte.add(historial);

        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 14f));
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.add(norte, BorderLayout.NORTH);
        cabecera.add(lblTitulo, BorderLayout.SOUTH);

        add(cabecera, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        actualizar();
    }

    private void mostrar(String titulo, java.util.function.Supplier<ReporteDAO.Resultado> consulta) {
        try {
            ReporteDAO.Resultado r = consulta.get();
            modelo.setDataVector(new Object[0][0], r.columnas);
            r.filas.forEach(f -> modelo.addRow(f));
            lblTitulo.setText(titulo + " - " + r.filas.size() + " registro(s)");
        } catch (Exception ex) {
            Ui.error(this, ex);
        }
    }

    @Override
    public void actualizar() {
        try {
            cmbEstudiante.removeAllItems();
            estudiantes.listar().forEach(cmbEstudiante::addItem);
        } catch (Exception ex) {
            Ui.error(this, ex);
        }
    }
}
