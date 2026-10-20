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
                excelExportador.exportarKardex(kardexDAO.listar(), response.getOutputStream());
                LOGGER.info("Kardex exportado a Excel.");
            } catch (IOException exception) {
                LOGGER.error("No se pudo generar la descarga del kardex.", exception);
                throw exception;
            }
            return;
        }

        request.setAttribute("movimientos", kardexDAO.listar());
        request.getRequestDispatcher("/kardex.jsp").forward(request, response);
    }
}
