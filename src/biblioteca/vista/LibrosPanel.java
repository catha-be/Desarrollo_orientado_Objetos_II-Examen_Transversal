package biblioteca.vista;

import biblioteca.controlador.CategoriaController;
import biblioteca.controlador.LibroController;
import biblioteca.modelo.Categoria;
import biblioteca.modelo.Libro;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class LibrosPanel extends JPanel implements MainFrame.Actualizable {
    private final LibroController controller = new LibroController();
    private final CategoriaController categorias = new CategoriaController();

    private final DefaultTableModel modelo = Ui.modelo("ID", "Título", "Autor", "ISBN", "Editorial", "Categoría", "Stock");
    private final JTable tabla = Ui.tabla(modelo);
    private final JTextField txtTitulo = new JTextField(20), txtAutor = new JTextField(20),
            txtIsbn = new JTextField(20), txtEditorial = new JTextField(20), txtStock = new JTextField(20);
    private final JComboBox<Categoria> cmbCategoria = new JComboBox<>();
    private List<Libro> libros = List.of();

    public LibrosPanel(boolean soloLectura) {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        if (!soloLectura) {
            JPanel form = new JPanel(new GridBagLayout());
            form.setBorder(BorderFactory.createTitledBorder("Datos del libro"));
            Ui.fila(form, 0, "Título:", txtTitulo);
            Ui.fila(form, 1, "Autor:", txtAutor);
            Ui.fila(form, 2, "ISBN:", txtIsbn);
            Ui.fila(form, 3, "Editorial:", txtEditorial);
            Ui.fila(form, 4, "Categoría:", cmbCategoria);
            Ui.fila(form, 5, "Stock:", txtStock);

            JButton nuevo = new JButton("Registrar"), mod = new JButton("Modificar"),
                    del = new JButton("Eliminar"), limpiar = new JButton("Limpiar");
            nuevo.addActionListener(e -> accion(() -> controller.crear(txtTitulo.getText(), txtAutor.getText(),
                    txtIsbn.getText(), txtEditorial.getText(), txtStock.getText(), idCategoria())));
            mod.addActionListener(e -> {
                Libro l = seleccionado();
                if (l != null) accion(() -> controller.modificar(l.getId(), txtTitulo.getText(), txtAutor.getText(),
                        txtIsbn.getText(), txtEditorial.getText(), txtStock.getText(), idCategoria()));
            });
            del.addActionListener(e -> {
                Libro l = seleccionado();
                if (l != null && Ui.confirmar(this, "¿Eliminar \"" + l.getTitulo() + "\"?")) {
                    accion(() -> controller.eliminar(l.getId()));
                }
            });
            limpiar.addActionListener(e -> limpiar());

            JPanel sur = new JPanel(new BorderLayout());
            sur.add(form, BorderLayout.CENTER);
            sur.add(Ui.botonera(nuevo, mod, del, limpiar), BorderLayout.SOUTH);
            add(sur, BorderLayout.SOUTH);

            tabla.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) cargarFormulario();
            });
        }
        actualizar();
    }

    private int idCategoria() {
        Categoria c = (Categoria) cmbCategoria.getSelectedItem();
        return c == null ? 0 : c.getId();
    }

    private Libro seleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Ui.info(this, "Seleccione un libro de la tabla.");
            return null;
        }
        int id = (int) modelo.getValueAt(tabla.convertRowIndexToModel(fila), 0);
        return libros.stream().filter(l -> l.getId() == id).findFirst().orElse(null);
    }

    private void cargarFormulario() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;
        int id = (int) modelo.getValueAt(tabla.convertRowIndexToModel(fila), 0);
        libros.stream().filter(l -> l.getId() == id).findFirst().ifPresent(l -> {
            txtTitulo.setText(l.getTitulo());
            txtAutor.setText(l.getAutor());
            txtIsbn.setText(l.getIsbn());
            txtEditorial.setText(l.getEditorial());
            txtStock.setText(String.valueOf(l.getStock()));
            for (int i = 0; i < cmbCategoria.getItemCount(); i++) {
                if (cmbCategoria.getItemAt(i).getId() == l.getIdCategoria()) cmbCategoria.setSelectedIndex(i);
            }
        });
    }

    private void limpiar() {
        tabla.clearSelection();
        txtTitulo.setText(""); txtAutor.setText(""); txtIsbn.setText(""); txtEditorial.setText(""); txtStock.setText("");
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
            libros = controller.listar();
            modelo.setRowCount(0);
            for (Libro l : libros) {
                modelo.addRow(new Object[]{l.getId(), l.getTitulo(), l.getAutor(), l.getIsbn(),
                        l.getEditorial(), l.getNombreCategoria(), l.getStock()});
            }
            cmbCategoria.removeAllItems();
            categorias.listar().forEach(cmbCategoria::addItem);
        } catch (Exception ex) {
            Ui.error(this, ex);
        }
    }
}
