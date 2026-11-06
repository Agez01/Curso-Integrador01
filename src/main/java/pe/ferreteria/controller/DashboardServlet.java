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

import java.io.IOException;

/** Dashboard (CU-06): indicadores del día y alertas de stock, disponible para todos los roles. */
@WebServlet({"/dashboard", "/dashboard.jsp"})
public class DashboardServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("resumen", DaoFactory.dashboardDAO().resumen());
        } catch (DatosException exception) {
            LOGGER.error("No se pudo cargar el dashboard.", exception);
            request.setAttribute("error", "No se pudieron cargar los indicadores. Revisa la conexión a la base de datos.");
        }
        request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(request, response);
    }
}
