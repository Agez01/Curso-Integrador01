package pe.ferreteria.dao;

/** Error de acceso a la base de datos (conexión o consulta). Los Servlets lo traducen a un mensaje amigable. */
public class DatosException extends RuntimeException {
    public DatosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public DatosException(String mensaje) {
        super(mensaje);
    }
}
