package pe.ferreteria.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.ferreteria.model.Usuario;
import pe.ferreteria.util.SesionUtil;

import java.io.IOException;

/** Cierra la sesión del usuario. */
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogoutServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Usuario usuario = SesionUtil.usuarioActual(request);
        HttpSession sesion = request.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }
        if (usuario != null) {
            LOGGER.info("Cierre de sesión: usuario='{}'.", usuario.getNombre());
        }
        response.sendRedirect(request.getContextPath() + "/login");
    }
}
