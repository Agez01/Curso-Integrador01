package pe.ferreteria.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.ferreteria.model.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Catálogo de productos en PostgreSQL. El stock solo cambia mediante movimientos de kardex o ventas. */
public class ProductoDAOJdbc implements ProductoDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductoDAOJdbc.class);
    private static final String SQL_UNICIDAD = "23505";

    @Override
    public List<Producto> listar() {
        String sql = "SELECT sku, nombre, categoria, unidad, precio, stock, stock_minimo FROM producto ORDER BY nombre";
        try (Connection conexion = ConexionBD.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet filas = sentencia.executeQuery()) {
            List<Producto> productos = new ArrayList<>();
            while (filas.next()) {
                productos.add(desdeFila(filas));
            }
            return productos;
        } catch (SQLException exception) {
            LOGGER.error("Error al listar productos.", exception);
            throw new DatosException("No se pudo consultar el catálogo.", exception);
        }
    }

    @Override
    public void registrar(Producto producto) {
        String sql = "INSERT INTO producto (sku, nombre, categoria, unidad, precio, stock, stock_minimo) "
                + "VALUES (?, ?, ?, ?, ?, 0, ?)";
        try (Connection conexion = ConexionBD.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, producto.getSku());
            sentencia.setString(2, producto.getNombre());
            sentencia.setString(3, producto.getCategoria());
            sentencia.setString(4, producto.getUnidad());
            sentencia.setBigDecimal(5, producto.getPrecio());
            sentencia.setInt(6, producto.getStockMinimo());
            sentencia.executeUpdate();
        } catch (SQLException exception) {
            if (SQL_UNICIDAD.equals(exception.getSQLState())) {
                throw new IllegalArgumentException("El SKU " + producto.getSku() + " ya está registrado.", exception);
            }
            LOGGER.error("Error al registrar el producto '{}'.", producto.getSku(), exception);
            throw new DatosException("No se pudo registrar el producto.", exception);
        }
    }

    static Producto desdeFila(ResultSet filas) throws SQLException {
        return new Producto(filas.getString("sku"), filas.getString("nombre"), filas.getString("categoria"),
                filas.getString("unidad"), filas.getBigDecimal("precio"),
                filas.getInt("stock"), filas.getInt("stock_minimo"));
    }
}
