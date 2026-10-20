package pe.ferreteria.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import pe.ferreteria.model.Usuario;

/** Acceso centralizado al usuario autenticado de la sesión. */
public final class SesionUtil {
    public static final String ATRIBUTO_USUARIO = "usuario";

    private SesionUtil() {
    }

    public static Usuario usuarioActual(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        return sesion == null ? null : (Usuario) sesion.getAttribute(ATRIBUTO_USUARIO);
    }
}
