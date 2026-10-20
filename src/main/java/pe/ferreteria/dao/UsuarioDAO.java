package pe.ferreteria.dao;

import java.util.Map;

/** Valida usuarios de prueba en memoria para la maqueta del curso. */
public class UsuarioDAO {
    private static final Map<String, String> USUARIOS_DEMO = Map.of("admin", "admin123");

    public boolean autenticar(String usuario, String clave) {
        return usuario != null && clave != null
                && clave.equals(USUARIOS_DEMO.get(usuario));
    }
}
