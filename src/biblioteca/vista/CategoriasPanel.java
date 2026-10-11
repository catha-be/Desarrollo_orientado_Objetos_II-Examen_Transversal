package biblioteca.vista;

import biblioteca.controlador.CategoriaController;
import biblioteca.modelo.Categoria;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CategoriasPanel extends JPanel implements MainFrame.Actualizable {
    private final CategoriaController controller = new CategoriaController();
    private final DefaultTableModel modelo = Ui.modelo("ID", "Nombre");
    private final JTable tabla = Ui.tabla(modelo);
    private final JTextField txtNombre = new JTextField(25);
    private List<Categoria> categorias = List.of();

    public CategoriasPanel() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JButton nuevo = new JButton("Registrar"), mod = new JButton("Modificar"), del = new JButton("Eliminar");
        nuevo.addActionListener(e -> accion(() -> controller.crear(txtNombre.getText())));
        mod.addActionListener(e -> {
            Categoria c = seleccionada();
            if (c != null) accion(() -> controller.modificar(c.getId(), txtNombre.getText()));
        });
        del.addActionListener(e -> {
            Categoria c = seleccionada();
            if (c != null && Ui.confirmar(this, "¿Eliminar la categoría \"" + c.getNombre() + "\"?")) {
                accion(() -> controller.eliminar(c.getId()));
            }
        });

        JPanel sur = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sur.setBorder(BorderFactory.createTitledBorder("Categoría"));
        sur.add(new JLabel("Nombre:"));
        sur.add(txtNombre);
        sur.add(nuevo);
        sur.add(mod);
        sur.add(del);
        add(sur, BorderLayout.SOUTH);

        tabla.getSelectionModel().addListSelectionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (!e.getValueIsAdjusting() && fila >= 0) {
                txtNombre.setText((String) modelo.getValueAt(tabla.convertRowIndexToModel(fila), 1));
            }
        });
        actualizar();
    }

    private Categoria seleccionada() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Ui.info(this, "Seleccione una categoría de la tabla.");
            return null;
        }
        int id = (int) modelo.getValueAt(tabla.convertRowIndexToModel(fila), 0);
        return categorias.stream().filter(c -> c.getId() == id).findFirst().orElse(null);
    }

    private void accion(Runnable r) {
        try {
            r.run();
            txtNombre.setText("");
            actualizar();
        } catch (Exception ex) {
            Ui.error(this, ex);
        }
    }

    @Override
    public void actualizar() {
        try {
            categorias = controller.listar();
            modelo.setRowCount(0);
            for (Categoria c : categorias) modelo.addRow(new Object[]{c.getId(), c.getNombre()});
        } catch (Exception ex) {
            Ui.error(this, ex);
        }
    }
}
