package biblioteca.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Patrón Singleton: una única instancia administra la conexión JDBC a MySQL.
 * La configuración se lee de db.properties (directorio de ejecución o classpath).
 */
public final class DatabaseConnection {
    private static final String URL_POR_DEFECTO =
            "jdbc:mysql://localhost:3306/biblioteca?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    /** Candado global: serializa las operaciones que escriben o usan transacciones sobre la conexión compartida. */
    public static final Object LOCK = new Object();

    private static volatile DatabaseConnection instance;

    private final String url;
    private final String usuario;
    private final String clave;
    private Connection connection;

    private DatabaseConnection() {
        Properties p = cargarPropiedades();
        this.url = p.getProperty("db.url", URL_POR_DEFECTO);
        this.usuario = p.getProperty("db.user", "root");
        String env = System.getenv("BIBLIOTECA_DB_PASSWORD");
        this.clave = env != null ? env : p.getProperty("db.password", "");
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    /** Devuelve la conexión única; la reabre si se cerró o dejó de ser válida. */
    public synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed() || !connection.isValid(2)) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(url, usuario, clave);
            } catch (ClassNotFoundException e) {
                throw new SQLException("Falta el driver MySQL (mysql-connector-j.jar) en el classpath: agregue lib/ como librería del módulo.", e);
            } catch (SQLException e) {
                throw new SQLException("No se pudo conectar a la base de datos (" + url + "): " + e.getMessage(), e);
            }
        }
        return connection;
    }

    public synchronized void cerrar() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignorada) {
                // se cierra al salir de la aplicación
            }
            connection = null;
        }
    }

    private static Properties cargarPropiedades() {
        Properties p = new Properties();
        Path archivo = Path.of("db.properties");
        try {
            if (Files.exists(archivo)) {
                try (InputStream in = Files.newInputStream(archivo)) {
                    p.load(in);
                }
            } else {
                try (InputStream in = DatabaseConnection.class.getResourceAsStream("/db.properties")) {
                    if (in != null) p.load(in);
                }
            }
        } catch (IOException e) {
            System.err.println("No se pudo leer db.properties, se usan valores por defecto: " + e.getMessage());
        }
        return p;
    }
}
