package biblioteca.vista;

import biblioteca.controlador.EstudianteController;
import biblioteca.modelo.Estudiante;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class EstudiantesPanel extends JPanel implements MainFrame.Actualizable {
    private final EstudianteController controller = new EstudianteController();
    private final DefaultTableModel modelo = Ui.modelo("ID", "Nombre", "RUT", "Curso", "Correo");
    private final JTable tabla = Ui.tabla(modelo);
    private final JTextField txtNombre = new JTextField(20), txtRut = new JTextField(20),
            txtCurso = new JTextField(20), txtCorreo = new JTextField(20);
    private List<Estudiante> estudiantes = List.of();

    public EstudiantesPanel() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Datos del estudiante (se crea también su acceso al sistema; clave inicial = RUT)"));
        Ui.fila(form, 0, "Nombre:", txtNombre);
        Ui.fila(form, 1, "RUT (12345678-9):", txtRut);
        Ui.fila(form, 2, "Curso:", txtCurso);
        Ui.fila(form, 3, "Correo:", txtCorreo);

        JButton nuevo = new JButton("Registrar"), mod = new JButton("Modificar"),
                del = new JButton("Eliminar"), limpiar = new JButton("Limpiar");
        nuevo.addActionListener(e -> accion(() -> controller.registrar(txtNombre.getText(), txtRut.getText(),
                txtCurso.getText(), txtCorreo.getText())));
        mod.addActionListener(e -> {
            Estudiante s = seleccionado();
            if (s != null) accion(() -> controller.modificar(s.getId(), txtNombre.getText(), txtRut.getText(),
                    txtCurso.getText(), txtCorreo.getText()));
        });
        del.addActionListener(e -> {
            Estudiante s = seleccionado();
            if (s != null && Ui.confirmar(this, "¿Eliminar a " + s.getNombre() + "?")) accion(() -> controller.eliminar(s));
        });
        limpiar.addActionListener(e -> limpiar());

        JPanel sur = new JPanel(new BorderLayout());
        sur.add(form, BorderLayout.CENTER);
        sur.add(Ui.botonera(nuevo, mod, del, limpiar), BorderLayout.SOUTH);
        add(sur, BorderLayout.SOUTH);

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarFormulario();
        });
        actualizar();
    }

    private Estudiante seleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Ui.info(this, "Seleccione un estudiante de la tabla.");
            return null;
        }
        int id = (int) modelo.getValueAt(tabla.convertRowIndexToModel(fila), 0);
        return estudiantes.stream().filter(s -> s.getId() == id).findFirst().orElse(null);
    }

    private void cargarFormulario() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;
        int id = (int) modelo.getValueAt(tabla.convertRowIndexToModel(fila), 0);
        estudiantes.stream().filter(s -> s.getId() == id).findFirst().ifPresent(s -> {
            txtNombre.setText(s.getNombre());
            txtRut.setText(s.getRut());
            txtCurso.setText(s.getCurso());
            txtCorreo.setText(s.getCorreo());
        });
    }

    private void limpiar() {
        tabla.clearSelection();
        txtNombre.setText(""); txtRut.setText(""); txtCurso.setText(""); txtCorreo.setText("");
    }

    private void accion(Runnable r) {
        try {
            r.run();
            limpiar();
            actualizar();
        } catch (Exception ex) {
            Ui.error(this, ex);
        }
    }

    @Override
    public void actualizar() {
        try {
            estudiantes = controller.listar();
            modelo.setRowCount(0);
            for (Estudiante s : estudiantes) {
                modelo.addRow(new Object[]{s.getId(), s.getNombre(), s.getRut(), s.getCurso(), s.getCorreo()});
            }
        } catch (Exception ex) {
            Ui.error(this, ex);
        }
    }
}
