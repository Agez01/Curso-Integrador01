package pe.ferreteria.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pe.ferreteria.dao.UsuarioDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/** Procesa el acceso de usuarios de demostración. */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginServlet.class);
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String usuario = request.getParameter("usuario");
        String clave = request.getParameter("clave");

        if (usuarioDAO.autenticar(usuario, clave)) {
            request.getSession(true).setAttribute("usuario", usuario);
            LOGGER.info("Inicio de sesión correcto para el usuario '{}'.", usuario);
            response.sendRedirect(request.getContextPath() + "/dashboard.jsp");
            return;
        }

        LOGGER.warn("Inicio de sesión fallido para el usuario '{}'.",
                usuario == null || usuario.isBlank() ? "desconocido" : usuario);
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Usuario o clave incorrectos.");
    }
}
