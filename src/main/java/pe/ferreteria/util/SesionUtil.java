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

    private static final String ATRIBUTO_CSRF = "csrfToken";
    private static final String ATRIBUTO_FLASH = "flashMensaje";

    /** Token anti-CSRF de la sesión; se crea la primera vez que se pide. */
    public static String tokenCsrf(HttpServletRequest request) {
        HttpSession sesion = request.getSession();
        Object token = sesion.getAttribute(ATRIBUTO_CSRF);
        if (token == null) {
            token = java.util.UUID.randomUUID().toString();
            sesion.setAttribute(ATRIBUTO_CSRF, token);
        }
        return token.toString();
    }

    /** Compara el token enviado con el de la sesión en tiempo constante. */
    public static boolean tokenCsrfValido(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        String enviado = request.getParameter("token");
        Object esperado = sesion == null ? null : sesion.getAttribute(ATRIBUTO_CSRF);
        return esperado != null && enviado != null && java.security.MessageDigest.isEqual(
                esperado.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8),
                enviado.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    /** Guarda un mensaje que se mostrará una sola vez tras la redirección. */
    public static void guardarMensaje(HttpServletRequest request, String mensaje) {
        request.getSession().setAttribute(ATRIBUTO_FLASH, mensaje);
    }

    /** Pasa el mensaje pendiente (si hay) al request como "exito" y lo borra de la sesión. */
    public static void publicarMensaje(HttpServletRequest request) {
        HttpSession sesion = request.getSession();
        Object mensaje = sesion.getAttribute(ATRIBUTO_FLASH);
        if (mensaje != null) {
            request.setAttribute("exito", mensaje);
            sesion.removeAttribute(ATRIBUTO_FLASH);
        }
    }
}
