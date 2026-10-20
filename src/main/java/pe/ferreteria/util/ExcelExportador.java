package pe.ferreteria.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import pe.ferreteria.model.MovimientoKardex;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/** Genera el reporte de movimientos en formato Excel .xlsx. */
public class ExcelExportador {
    private static final String[] ENCABEZADOS = {
            "fecha", "sku", "producto", "tipo", "cantidad", "stock resultante", "usuario"
    };

    public void exportarKardex(List<MovimientoKardex> movimientos, OutputStream salida)
            throws IOException {
        try (XSSFWorkbook libro = new XSSFWorkbook()) {
            Sheet hoja = libro.createSheet("Kardex");
            Font fuenteNegrita = libro.createFont();
            fuenteNegrita.setBold(true);
            CellStyle estiloEncabezado = libro.createCellStyle();
            estiloEncabezado.setFont(fuenteNegrita);
            CreationHelper helper = libro.getCreationHelper();
            CellStyle estiloFecha = libro.createCellStyle();
            estiloFecha.setDataFormat(helper.createDataFormat().getFormat("dd/mm/yyyy hh:mm"));

            Row encabezado = hoja.createRow(0);
            for (int columna = 0; columna < ENCABEZADOS.length; columna++) {
                Cell celda = encabezado.createCell(columna);
                celda.setCellValue(ENCABEZADOS[columna]);
                celda.setCellStyle(estiloEncabezado);
            }

            int fila = 1;
            for (MovimientoKardex movimiento : movimientos) {
                Row registro = hoja.createRow(fila++);
                Cell fecha = registro.createCell(0);
                fecha.setCellValue(movimiento.getFecha());
                fecha.setCellStyle(estiloFecha);
                registro.createCell(1).setCellValue(movimiento.getSku());
                registro.createCell(2).setCellValue(movimiento.getProducto());
                registro.createCell(3).setCellValue(movimiento.getTipo());
                registro.createCell(4).setCellValue(movimiento.getCantidad());
                registro.createCell(5).setCellValue(movimiento.getStockResultante());
                registro.createCell(6).setCellValue(movimiento.getUsuario());
            }

            for (int columna = 0; columna < ENCABEZADOS.length; columna++) {
                hoja.autoSizeColumn(columna);
            }
            libro.write(salida);
        }
    }
}
