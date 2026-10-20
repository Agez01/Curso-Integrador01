package pe.ferreteria.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.ferreteria.dao.DaoFactory;
import pe.ferreteria.dao.KardexDAO;
import pe.ferreteria.model.Usuario;
import pe.ferreteria.util.SesionUtil;

import java.io.IOException;

/**
 * Base común de VentaServlet y EntradaServlet: registra un movimiento en el kardex
 * a nombre del usuario de la sesión (nunca de un parámetro de la petición).
 */
public abstract class MovimientoServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(MovimientoServlet.class);
    private final KardexDAO kardexDAO = DaoFactory.kardexDAO();

    /** "Entrada" o "Salida". */
    protected abstract String tipoMovimiento();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Usuario usuario = SesionUtil.usuarioActual(request); // el AuthFilter garantiza que existe
        String producto = request.getParameter("producto");
        try {
            int cantidad = Integer.parseInt(request.getParameter("cantidad"));
            kardexDAO.registrar(request.getParameter("sku"), producto, tipoMovimiento(), cantidad,
                    usuario.getNombre());
            LOGGER.info("{} registrada: producto='{}', cantidad={}, usuario='{}'.",
                    tipoMovimiento(), producto, cantidad, usuario.getNombre());
            response.setStatus(HttpServletResponse.SC_CREATED);
        } catch (IllegalArgumentException exception) { // incluye NumberFormatException
            LOGGER.error("Datos inválidos al registrar {} de '{}' (usuario '{}').",
                    tipoMovimiento(), producto, usuario.getNombre(), exception);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Datos de movimiento inválidos.");
        } catch (IllegalStateException exception) {
            LOGGER.error("No se pudo completar {} de '{}' (usuario '{}').",
                    tipoMovimiento(), producto, usuario.getNombre(), exception);
            response.sendError(HttpServletResponse.SC_CONFLICT, exception.getMessage());
        } catch (ArithmeticException exception) {
            LOGGER.error("Desbordamiento al actualizar existencias para '{}'.", producto, exception);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "No se pudo actualizar el inventario.");
        }
    }
}
