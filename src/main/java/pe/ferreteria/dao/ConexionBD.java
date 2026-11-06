package pe.ferreteria.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Abre conexiones JDBC a PostgreSQL. La configuración se toma, en este orden, de las variables de
 * entorno DB_URL, DB_USER y DB_PASSWORD o del archivo db.properties del classpath (que no se sube a Git:
 * copia db.example.properties como db.properties).
 */
public final class ConexionBD {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConexionBD.class);
    private static volatile Properties configuracion;

    private ConexionBD() {
    }

    public static Connection abrir() {
        Properties config = configuracion();
        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(config.getProperty("db.url"),
                    config.getProperty("db.user"), config.getProperty("db.password"));
        } catch (ClassNotFoundException | SQLException exception) {
            LOGGER.error("No se pudo conectar a PostgreSQL ({}).", config.getProperty("db.url"), exception);
            throw new DatosException("No se pudo conectar a la base de datos.", exception);
        }
    }

    /** Indica si hay una base de datos configurada y alcanzable (usado por las pruebas de integración). */
    public static boolean disponible() {
        try (Connection conexion = abrir()) {
            return conexion.isValid(2);
        } catch (RuntimeException | SQLException exception) {
            return false;
        }
    }

    private static Properties configuracion() {
        if (configuracion == null) {
            synchronized (ConexionBD.class) {
                if (configuracion == null) {
                    configuracion = cargar();
                }
            }
        }
        return configuracion;
    }

    private static Properties cargar() {
        Properties propiedades = new Properties();
        try (InputStream entrada = ConexionBD.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (entrada != null) {
                propiedades.load(entrada);
            }
        } catch (IOException exception) {
            throw new DatosException("No se pudo leer db.properties.", exception);
        }
        sobrescribir(propiedades, "db.url", "DB_URL");
        sobrescribir(propiedades, "db.user", "DB_USER");
        sobrescribir(propiedades, "db.password", "DB_PASSWORD");
        if (propiedades.getProperty("db.url") == null || propiedades.getProperty("db.user") == null
                || propiedades.getProperty("db.password") == null) {
            throw new DatosException("Falta configurar la base de datos: copia db.example.properties "
                    + "como db.properties o define DB_URL, DB_USER y DB_PASSWORD.");
        }
        return propiedades;
    }

    private static void sobrescribir(Properties propiedades, String clave, String variable) {
        String valor = System.getenv(variable);
        if (valor != null && !valor.isBlank()) {
            propiedades.setProperty(clave, valor);
        }
    }
}
