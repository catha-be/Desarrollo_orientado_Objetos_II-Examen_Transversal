package biblioteca.vista;

import biblioteca.controlador.Sesion;
import biblioteca.modelo.Usuario;
import biblioteca.util.DatabaseConnection;

import javax.swing.*;
import java.awt.*;

/** Ventana principal: menú de navegación + paneles por módulo, según el rol. */
public class MainFrame extends JFrame {
    private final CardLayout cards = new CardLayout();
    private final JPanel contenido = new JPanel(cards);

    public MainFrame(Usuario usuario) {
        super("Biblioteca Escolar - " + usuario.getNombre() + " [" + usuario.getRol() + "]");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 640);
        setLocationRelativeTo(null);

        boolean admin = usuario.esBibliotecario();
        JMenuBar barra = new JMenuBar();

        // Libros: bibliotecario gestiona; estudiante solo consulta
        agregar(barra, admin ? "Libros" : "Consultar libros", "libros", new LibrosPanel(!admin));
        if (admin) {
            agregar(barra, "Estudiantes", "estudiantes", new EstudiantesPanel());
            agregar(barra, "Categorías", "categorias", new CategoriasPanel());
        }
        agregar(barra, "Préstamos y devoluciones", "prestamos", new PrestamosPanel(usuario));
        if (admin) {
            agregar(barra, "Reportes", "reportes", new ReportesPanel());
        }

        barra.add(Box.createHorizontalGlue());
        JMenu sesion = new JMenu("Sesión");
        JMenuItem cerrar = new JMenuItem("Cerrar sesión");
        cerrar.addActionListener(e -> {
            Sesion.cerrar();
            dispose();
            new LoginFrame().setVisible(true);
        });
        JMenuItem salir = new JMenuItem("Salir");
        salir.addActionListener(e -> {
            DatabaseConnection.getInstance().cerrar();
            System.exit(0);
        });
        sesion.add(cerrar);
        sesion.add(salir);
        barra.add(sesion);

        setJMenuBar(barra);
        add(contenido);
    }

    private void agregar(JMenuBar barra, String titulo, String clave, JPanel panel) {
        contenido.add(panel, clave);
        JMenu menu = new JMenu(titulo);
        // Un JMenu sin ítems actúa como botón de navegación.
        menu.addMenuListener(new javax.swing.event.MenuListener() {
            @Override public void menuSelected(javax.swing.event.MenuEvent e) {
                cards.show(contenido, clave);
                if (panel instanceof Actualizable a) a.actualizar();
                menu.setSelected(false);
            }
            @Override public void menuDeselected(javax.swing.event.MenuEvent e) { }
            @Override public void menuCanceled(javax.swing.event.MenuEvent e) { }
        });
        barra.add(menu);
    }

    /** Paneles que recargan sus datos al mostrarse. */
    interface Actualizable {
        void actualizar();
    }
}
