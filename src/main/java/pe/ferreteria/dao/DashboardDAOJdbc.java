package pe.ferreteria.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.ferreteria.model.Producto;
import pe.ferreteria.model.ResumenDashboard;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Indicadores del día calculados con consultas agregadas sobre PostgreSQL. */
public class DashboardDAOJdbc implements DashboardDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardDAOJdbc.class);

    @Override
    public ResumenDashboard resumen() {
        try (Connection conexion = ConexionBD.abrir()) {
            BigDecimal ventas;
            int comprobantes;
            try (PreparedStatement s = conexion.prepareStatement(
                    "SELECT COALESCE(SUM(total), 0), COUNT(*) FROM venta WHERE fecha::date = current_date");
                 ResultSet f = s.executeQuery()) {
                f.next();
                ventas = f.getBigDecimal(1);
                comprobantes = f.getInt(2);
            }
            int movimientos;
            try (PreparedStatement s = conexion.prepareStatement(
                    "SELECT COUNT(*) FROM movimiento_kardex WHERE fecha::date = current_date");
                 ResultSet f = s.executeQuery()) {
                f.next();
                movimientos = f.getInt(1);
            }
            List<Producto> alertas = new ArrayList<>();
            try (PreparedStatement s = conexion.prepareStatement(
                    "SELECT sku, nombre, categoria, unidad, precio, stock, stock_minimo FROM producto "
                            + "WHERE stock <= stock_minimo ORDER BY (stock::numeric / NULLIF(stock_minimo, 0)), nombre");
                 ResultSet f = s.executeQuery()) {
                while (f.next()) {
                    alertas.add(ProductoDAOJdbc.desdeFila(f));
                }
            }
            return new ResumenDashboard(ventas, comprobantes, alertas.size(), movimientos, alertas);
        } catch (SQLException exception) {
            LOGGER.error("Error al calcular el dashboard.", exception);
            throw new DatosException("No se pudo calcular el resumen.", exception);
        }
    }
}
