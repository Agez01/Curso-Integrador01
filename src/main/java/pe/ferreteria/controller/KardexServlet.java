package pe.ferreteria.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.ferreteria.dao.DaoFactory;
import pe.ferreteria.dao.KardexDAO;
import pe.ferreteria.util.ExcelExportador;

import java.io.IOException;

/** Muestra el kardex y permite descargarlo como libro de Excel. */
@WebServlet("/kardex")
public class KardexServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(KardexServlet.class);
    private final KardexDAO kardexDAO = DaoFactory.kardexDAO();
    private final ExcelExportador excelExportador = new ExcelExportador();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("exportar".equals(request.getParameter("action"))) {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"kardex.xlsx\"");
            try {
                excelExportador.exportarKardex(kardexDAO.buscar(request.getParameter("q"), request.getParameter("tipo")), response.getOutputStream());
                LOGGER.info("Kardex exportado a Excel.");
            } catch (IOException exception) {
                LOGGER.error("No se pudo generar la descarga del kardex.", exception);
                throw exception;
            }
            return;
        }

        request.setAttribute("busqueda", org.apache.commons.lang3.StringUtils.trimToEmpty(request.getParameter("q")));
        request.setAttribute("tipoFiltro", org.apache.commons.lang3.StringUtils.trimToEmpty(request.getParameter("tipo")));
        try {
            request.setAttribute("movimientos", kardexDAO.buscar(request.getParameter("q"), request.getParameter("tipo")));
        } catch (pe.ferreteria.dao.DatosException exception) {
            request.setAttribute("error", "No se pudo consultar el kardex. Revisa la conexión a la base de datos.");
            request.setAttribute("movimientos", java.util.List.of());
        }
        request.getRequestDispatcher("/WEB-INF/views/kardex.jsp").forward(request, response);
    }
}
