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
import pe.ferreteria.model.Cliente;
import pe.ferreteria.util.SesionUtil;

import java.io.IOException;
import java.util.List;

/** CU-08: registra y actualiza clientes (DNI o RUC) usados en los comprobantes. */
@WebServlet("/clientes")
public class ClienteServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClienteServlet.class);

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
        try {
            Cliente cliente = new Cliente(request.getParameter("documento"), request.getParameter("nombre"),
                    request.getParameter("telefono"), request.getParameter("direccion"));
            DaoFactory.clienteDAO().guardar(cliente);
            LOGGER.info("Cliente '{}' guardado por '{}'.", cliente.getDocumento(),
                    SesionUtil.usuarioActual(request).getNombre());
            SesionUtil.guardarMensaje(request, "Cliente guardado correctamente.");
            response.sendRedirect(request.getContextPath() + "/clientes");
        } catch (IllegalArgumentException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.setAttribute("error", exception.getMessage());
            mostrar(request, response);
        }
    }

    private void mostrar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String busqueda = StringUtils.trimToEmpty(request.getParameter("q"));
        request.setAttribute("busqueda", busqueda);
        try {
            request.setAttribute("clientes", DaoFactory.clienteDAO().buscar(busqueda));
        } catch (DatosException exception) {
            request.setAttribute("error", "No se pudo consultar los clientes. Revisa la conexión a la base de datos.");
            request.setAttribute("clientes", List.of());
        }
        request.setAttribute("token", SesionUtil.tokenCsrf(request));
        request.getRequestDispatcher("/WEB-INF/views/clientes.jsp").forward(request, response);
    }
}
