package pe.ferreteria.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.ferreteria.dao.DaoFactory;
import pe.ferreteria.dao.DatosException;
import pe.ferreteria.model.Rol;
import pe.ferreteria.model.Usuario;
import pe.ferreteria.util.PasswordUtil;
import pe.ferreteria.util.SesionUtil;

import java.io.IOException;
import java.util.List;

/** CU-02: el administrador crea usuarios, asigna su rol y los bloquea o reactiva. */
@WebServlet("/usuarios")
public class UsuarioServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(UsuarioServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        SesionUtil.publicarMensaje(request);
        mostrar(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!SesionUtil.tokenCsrfValido(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Vuelve a abrir la pantalla e intenta nuevamente.");
            return;
        }
        Usuario actual = SesionUtil.usuarioActual(request);
        try {
            String accion = StringUtils.trimToEmpty(request.getParameter("accion"));
            String nombre = StringUtils.trimToEmpty(request.getParameter("nombre"));
            if ("crear".equals(accion)) {
                String clave = StringUtils.defaultString(request.getParameter("clave"));
                if (!nombre.matches("[A-Za-z0-9._-]{3,30}")) {
                    throw new IllegalArgumentException("El usuario debe tener 3 a 30 caracteres (letras, números, . _ -).");
                }
                if (clave.length() < 8 || clave.length() > 72) {
                    throw new IllegalArgumentException("La clave debe tener entre 8 y 72 caracteres.");
                }
                Rol rol;
                try {
                    rol = Rol.valueOf(StringUtils.trimToEmpty(request.getParameter("rol")));
                } catch (IllegalArgumentException exception) {
                    throw new IllegalArgumentException("Selecciona un rol válido.");
                }
                DaoFactory.usuarioDAO().crear(nombre, PasswordUtil.hashear(clave), rol);
                LOGGER.info("Usuario '{}' creado con rol {} por '{}'.", nombre, rol, actual.getNombre());
                SesionUtil.guardarMensaje(request, "Usuario '" + nombre + "' creado.");
            } else if ("bloquear".equals(accion) || "activar".equals(accion)) {
                boolean activar = "activar".equals(accion);
                if (!activar && nombre.equals(actual.getNombre())) {
                    throw new IllegalArgumentException("No puedes bloquear tu propio usuario.");
                }
                DaoFactory.usuarioDAO().cambiarEstado(nombre, activar);
                LOGGER.info("Usuario '{}' {} por '{}'.", nombre, activar ? "activado" : "bloqueado", actual.getNombre());
                SesionUtil.guardarMensaje(request, "Usuario '" + nombre + "' " + (activar ? "activado." : "bloqueado."));
            } else {
                throw new IllegalArgumentException("Acción no válida.");
            }
            response.sendRedirect(request.getContextPath() + "/usuarios");
        } catch (IllegalArgumentException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.setAttribute("error", exception.getMessage());
            mostrar(request, response);
        }
    }

    private void mostrar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("usuarios", DaoFactory.usuarioDAO().listar());
        } catch (DatosException | UnsupportedOperationException exception) {
            request.setAttribute("error", "No se pudo consultar los usuarios. Revisa la conexión a la base de datos.");
            request.setAttribute("usuarios", List.of());
        }
        request.setAttribute("roles", Rol.values());
        request.setAttribute("token", SesionUtil.tokenCsrf(request));
        request.getRequestDispatcher("/WEB-INF/views/usuarios.jsp").forward(request, response);
    }
}
