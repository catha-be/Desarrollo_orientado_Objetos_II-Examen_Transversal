package biblioteca;

import biblioteca.util.DatabaseConnection;
import biblioteca.vista.LoginFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignorada) {
            // se usa el aspecto por defecto
        }
        SwingUtilities.invokeLater(() -> {
            try {
                DatabaseConnection.getInstance().getConnection(); // verifica la conexión al inicio
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        e.getMessage() + "\n\nRevise db.properties y que MySQL esté activo.",
                        "Sin conexión a la base de datos", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            new LoginFrame().setVisible(true);
        });
    }
}
