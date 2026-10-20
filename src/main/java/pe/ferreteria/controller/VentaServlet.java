package pe.ferreteria.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pe.ferreteria.dao.KardexDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/** Registra salidas de inventario correspondientes a ventas. */
@WebServlet("/venta")
public class VentaServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(VentaServlet.class);
    private final KardexDAO kardexDAO = KardexDAO.getInstancia();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String producto = request.getParameter("producto");
        String usuario = obtenerUsuario(request);
        try {
            int cantidad = Integer.parseInt(request.getParameter("cantidad"));
            kardexDAO.registrar(request.getParameter("sku"), producto, "Salida", cantidad, usuario);
            LOGGER.info("Venta registrada: producto='{}', cantidad={}, usuario='{}'.",
                    producto, cantidad, usuario);
            response.setStatus(HttpServletResponse.SC_CREATED);
        } catch (IllegalArgumentException exception) {
            LOGGER.error("No se pudo registrar la venta de '{}' por el usuario '{}'.",
                    producto, usuario, exception);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
        } catch (IllegalStateException exception) {
            LOGGER.error("No se pudo completar la venta de '{}' por el usuario '{}'.",
                    producto, usuario, exception);
            response.sendError(HttpServletResponse.SC_CONFLICT, exception.getMessage());
        } catch (ArithmeticException exception) {
            LOGGER.error("Desbordamiento al actualizar existencias para '{}'.", producto, exception);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "No se pudo actualizar el inventario.");
        }
    }

    private String obtenerUsuario(HttpServletRequest request) {
        Object usuarioSesion = request.getSession(false) == null ? null
                : request.getSession(false).getAttribute("usuario");
        String usuario = usuarioSesion == null ? request.getParameter("usuario") : usuarioSesion.toString();
        return usuario == null || usuario.isBlank() ? "anónimo" : usuario;
    }
}
