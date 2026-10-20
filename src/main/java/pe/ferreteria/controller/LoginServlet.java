package pe.ferreteria.controller;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.ferreteria.dao.DaoFactory;
import pe.ferreteria.dao.UsuarioDAO;
import pe.ferreteria.model.Usuario;
import pe.ferreteria.util.SesionUtil;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/** Muestra el formulario de acceso y autentica al usuario. */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginServlet.class);
    private static final int MAX_INTENTOS = 5;

    /** Intentos fallidos por usuario; se olvidan solos a los 5 minutos (protección contra fuerza bruta). */
    private final Cache<String, Integer> intentosFallidos = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES).build();
    private final UsuarioDAO usuarioDAO = DaoFactory.usuarioDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (SesionUtil.usuarioActual(request) != null) {
            response.sendRedirect(request.getContextPath() + "/dashboard.jsp");
            return;
        }
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String nombre = StringUtils.trimToEmpty(request.getParameter("usuario"));
        String clave = StringUtils.defaultString(request.getParameter("clave"));

        if (nombre.isEmpty() || clave.isEmpty()) {
            mostrarError(request, response, "Ingresa usuario y clave.");
            return;
        }
        if (intentosFallidos.asMap().getOrDefault(nombre, 0) >= MAX_INTENTOS) {
            LOGGER.warn("Usuario '{}' bloqueado temporalmente por demasiados intentos.", nombre);
            mostrarError(request, response, "Demasiados intentos. Espera 5 minutos.");
            return;
        }

        Optional<Usuario> usuario = usuarioDAO.autenticar(nombre, clave);
        if (usuario.isPresent()) {
            intentosFallidos.invalidate(nombre);
            // Nueva sesión para evitar fijación de sesión.
            if (request.getSession(false) != null) {
                request.changeSessionId();
            }
            request.getSession(true).setAttribute(SesionUtil.ATRIBUTO_USUARIO, usuario.get());
            LOGGER.info("Inicio de sesión correcto: usuario='{}', rol={}.", nombre, usuario.get().getRol());
            response.sendRedirect(request.getContextPath() + "/dashboard.jsp");
            return;
        }

        intentosFallidos.asMap().merge(nombre, 1, Integer::sum);
        LOGGER.warn("Inicio de sesión fallido para el usuario '{}'.", nombre);
        mostrarError(request, response, "Usuario o clave incorrectos.");
    }

    private void mostrarError(HttpServletRequest request, HttpServletResponse response, String mensaje)
            throws ServletException, IOException {
        request.setAttribute("error", mensaje);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }
}
