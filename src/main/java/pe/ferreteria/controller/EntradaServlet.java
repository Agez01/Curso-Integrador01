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
import pe.ferreteria.model.ItemMovimiento;
import pe.ferreteria.model.Producto;
import pe.ferreteria.model.Usuario;
import pe.ferreteria.util.SesionUtil;

import java.io.IOException;
import java.util.List;

/** Recepción de mercadería: registra entradas de varias líneas en una sola transacción. */
@WebServlet({"/entrada", "/Pages/recepcion.jsp"})
public class EntradaServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntradaServlet.class);

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
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Vuelve a abrir la recepción e intenta nuevamente.");
            return;
        }
        Usuario usuario = SesionUtil.usuarioActual(request);
        try {
            List<ItemMovimiento> items = ItemMovimiento.desdeParametros(
                    request.getParameterValues("sku"), request.getParameterValues("cantidad"));
            String proveedor = StringUtils.trimToEmpty(request.getParameter("proveedor"));
            String factura = StringUtils.trimToEmpty(request.getParameter("factura"));
            if (proveedor.isEmpty()) {
                throw new IllegalArgumentException("Indica el proveedor.");
            }
            String motivo = "Proveedor: " + proveedor + (factura.isEmpty() ? "" : " · Doc. " + factura);
            DaoFactory.kardexDAO().registrarLote(items, "Entrada", usuario.getNombre(), motivo);
            LOGGER.info("Entrada registrada por '{}': {} línea(s), {}.", usuario.getNombre(), items.size(), motivo);
            SesionUtil.guardarMensaje(request, "Entrada registrada: " + items.size()
                    + " producto(s) actualizados en existencias.");
            response.sendRedirect(request.getContextPath() + "/entrada");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            LOGGER.warn("Entrada rechazada (usuario '{}'): {}", usuario.getNombre(), exception.getMessage());
            response.setStatus(exception instanceof IllegalStateException
                    ? HttpServletResponse.SC_CONFLICT : HttpServletResponse.SC_BAD_REQUEST);
            request.setAttribute("error", exception.getMessage());
            mostrar(request, response);
        }
    }

    private void mostrar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<Producto> productos = DaoFactory.productoDAO().listar();
            request.setAttribute("productos", productos);
        } catch (DatosException exception) {
            request.setAttribute("error", "No se pudo cargar el catálogo. Revisa la conexión a la base de datos.");
            request.setAttribute("productos", List.of());
        }
        request.setAttribute("token", SesionUtil.tokenCsrf(request));
        request.getRequestDispatcher("/WEB-INF/views/recepcion.jsp").forward(request, response);
    }
}
