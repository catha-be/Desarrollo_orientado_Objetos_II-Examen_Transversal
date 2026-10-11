package biblioteca.vista;

import biblioteca.controlador.AuthController;
import biblioteca.modelo.Usuario;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private final AuthController controller = new AuthController();
    private final JTextField txtUsuario = new JTextField(20);
    private final JPasswordField txtClave = new JPasswordField(20);
    private final JButton btnIngresar = new JButton("Ingresar");

    public LoginFrame() {
        super("Biblioteca Escolar - Inicio de sesión");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JLabel titulo = new JLabel("Biblioteca Escolar", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 22f));
        titulo.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        Ui.fila(form, 0, "Correo o RUT:", txtUsuario);
        Ui.fila(form, 1, "Contraseña:", txtClave);

        JPanel sur = new JPanel();
        sur.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        sur.add(btnIngresar);

        add(titulo, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
        add(sur, BorderLayout.SOUTH);

        btnIngresar.addActionListener(e -> ingresar());
        txtClave.addActionListener(e -> ingresar());
        getRootPane().setDefaultButton(btnIngresar);

        pack();
        setMinimumSize(new Dimension(380, getHeight()));
        setLocationRelativeTo(null);
    }

    private void ingresar() {
        try {
            Usuario u = controller.autenticar(txtUsuario.getText(), new String(txtClave.getPassword()));
            new MainFrame(u).setVisible(true);
            dispose();
        } catch (Exception ex) {
            Ui.error(this, ex);
            txtClave.setText("");
        }
    }
}
