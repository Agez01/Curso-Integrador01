package pe.ferreteria.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.ferreteria.dao.DaoFactory;
import pe.ferreteria.dao.DatosException;
import pe.ferreteria.model.ItemMovimiento;
import pe.ferreteria.model.Producto;
import pe.ferreteria.model.Usuario;
import pe.ferreteria.model.Venta;
import pe.ferreteria.util.SesionUtil;

import java.io.IOException;
import java.util.List;

/** Punto de venta: muestra los productos con stock y registra la venta con su comprobante. */
@WebServlet({"/venta", "/Pages/ventas.jsp"})
public class VentaServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(VentaServlet.class);

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
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Vuelve a abrir el punto de venta e intenta nuevamente.");
            return;
        }
        Usuario usuario = SesionUtil.usuarioActual(request); // el AuthFilter garantiza que existe
        try {
            List<ItemMovimiento> items = ItemMovimiento.desdeParametros(
                    request.getParameterValues("sku"), request.getParameterValues("cantidad"));
            Venta venta = DaoFactory.ventaDAO().registrar(items, request.getParameter("tipo"),
                    request.getParameter("documento"), usuario.getNombre());
            SesionUtil.guardarMensaje(request, "Venta registrada: " + venta.getTipoComprobante() + " "
                    + venta.getNumeroComprobante() + " por S/ " + venta.getTotal().toPlainString() + ".");
            response.sendRedirect(request.getContextPath() + "/venta");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            LOGGER.warn("Venta rechazada (usuario '{}'): {}", usuario.getNombre(), exception.getMessage());
            response.setStatus(exception instanceof IllegalStateException
                    ? HttpServletResponse.SC_CONFLICT : HttpServletResponse.SC_BAD_REQUEST);
            request.setAttribute("error", exception.getMessage());
            mostrar(request, response);
        }
    }

    private void mostrar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<Producto> disponibles = DaoFactory.productoDAO().listar().stream()
                    .filter(p -> p.getStock() > 0).toList();
            request.setAttribute("productos", disponibles);
            request.setAttribute("clientes", DaoFactory.clienteDAO().buscar(""));
        } catch (DatosException exception) {
            request.setAttribute("error", "No se pudo cargar el catálogo. Revisa la conexión a la base de datos.");
            request.setAttribute("productos", List.of());
        }
        request.setAttribute("token", SesionUtil.tokenCsrf(request));
        request.getRequestDispatcher("/WEB-INF/views/ventas.jsp").forward(request, response);
    }
}
