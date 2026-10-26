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
import pe.ferreteria.dao.ProductoDAO;
import pe.ferreteria.model.Producto;
import pe.ferreteria.util.SesionUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;

/** Conserva la URL anterior del catálogo y prepara los datos antes de mostrar la JSP. */
@WebServlet({"/productos", "/Pages/catalogo.jsp"})
public class ProductoServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductoServlet.class);
    private static final String TOKEN = "tokenCatalogo";
    private final ProductoDAO productoDAO;

    public ProductoServlet() { this(DaoFactory.productoDAO()); }
    ProductoServlet(ProductoDAO productoDAO) { this.productoDAO = productoDAO; }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        mostrar(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Object token = request.getSession().getAttribute(TOKEN);
        if (token == null || !token.equals(request.getParameter("token"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Vuelve a abrir el catálogo e intenta nuevamente.");
            return;
        }
        try {
            BigDecimal precio;
            try {
                String valor = StringUtils.trimToEmpty(request.getParameter("precio"));
                if (!valor.matches("[0-9]{1,9}(\\.[0-9]{1,2})?")) {
                    throw new NumberFormatException();
                }
                precio = new BigDecimal(valor);
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Ingresa un precio válido, con hasta dos decimales.");
            }
            Producto producto = new Producto(request.getParameter("sku"), request.getParameter("nombre"),
                    request.getParameter("categoria"), request.getParameter("unidad"), precio);
            productoDAO.registrar(producto);
            LOGGER.info("Producto registrado: sku='{}', usuario='{}'.", producto.getSku(),
                    SesionUtil.usuarioActual(request).getNombre());
            request.getSession().setAttribute("exitoCatalogo", "Producto registrado correctamente.");
            response.sendRedirect(request.getContextPath() + "/productos");
        } catch (IllegalArgumentException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.setAttribute("error", exception.getMessage());
            mostrar(request, response);
        }
    }

    private void mostrar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.getSession().getAttribute(TOKEN) == null) {
            request.getSession().setAttribute(TOKEN, UUID.randomUUID().toString());
        }
        Object exito = request.getSession().getAttribute("exitoCatalogo");
        request.setAttribute("exito", exito);
        request.getSession().removeAttribute("exitoCatalogo");
        String busqueda = StringUtils.trimToEmpty(request.getParameter("q"));
        String texto = busqueda.toLowerCase(Locale.ROOT);
        request.setAttribute("busqueda", busqueda);
        request.setAttribute("catalogoActivo", true);
        request.setAttribute("categorias", Producto.CATEGORIAS);
        request.setAttribute("productos", productoDAO.listar().stream()
                .filter(p -> p.getSku().toLowerCase(Locale.ROOT).contains(texto)
                        || p.getNombre().toLowerCase(Locale.ROOT).contains(texto))
                .toList());
        request.getRequestDispatcher("/WEB-INF/views/catalogo.jsp").forward(request, response);
    }
}
