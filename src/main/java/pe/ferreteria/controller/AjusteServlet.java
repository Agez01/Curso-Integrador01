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

/** Ajuste de existencias por merma (CU-05): descuenta stock y exige un motivo. */
@WebServlet("/ajuste")
public class AjusteServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(AjusteServlet.class);

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
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Vuelve a abrir el ajuste e intenta nuevamente.");
            return;
        }
        Usuario usuario = SesionUtil.usuarioActual(request);
        try {
            String motivo = StringUtils.trimToEmpty(request.getParameter("motivo"));
            if (motivo.length() < 5) {
                throw new IllegalArgumentException("Describe el motivo de la merma (mínimo 5 caracteres).");
            }
            int cantidad;
            try {
                cantidad = Integer.parseInt(StringUtils.trimToEmpty(request.getParameter("cantidad")));
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("La cantidad debe ser un número entero.");
            }
            DaoFactory.kardexDAO().registrarLote(List.of(new ItemMovimiento(request.getParameter("sku"), cantidad)),
                    "Merma", usuario.getNombre(), motivo);
            LOGGER.info("Merma registrada por '{}': sku='{}', cantidad={}.", usuario.getNombre(),
                    request.getParameter("sku"), cantidad);
            SesionUtil.guardarMensaje(request, "Ajuste registrado: se descontaron " + cantidad + " unidad(es).");
            response.sendRedirect(request.getContextPath() + "/ajuste");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            LOGGER.warn("Ajuste rechazado (usuario '{}'): {}", usuario.getNombre(), exception.getMessage());
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
        request.getRequestDispatcher("/WEB-INF/views/ajuste.jsp").forward(request, response);
    }
}
