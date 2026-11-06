package pe.ferreteria.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.ferreteria.model.ItemMovimiento;
import pe.ferreteria.model.Venta;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Ventas en PostgreSQL: una transacción guarda la venta, su detalle, el stock y el kardex. */
public class VentaDAOJdbc implements VentaDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(VentaDAOJdbc.class);

    @Override
    public Venta registrar(List<ItemMovimiento> items, String tipoComprobante, String documentoCliente,
                           String usuario) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("El carrito está vacío.");
        }
        boolean factura = "Factura".equals(tipoComprobante);
        if (!factura && !"Boleta".equals(tipoComprobante)) {
            throw new IllegalArgumentException("El comprobante debe ser Boleta o Factura.");
        }
        String documento = documentoCliente == null || documentoCliente.isBlank() ? null : documentoCliente.trim();
        if (factura && (documento == null || !documento.matches("\\d{11}"))) {
            throw new IllegalArgumentException("La factura requiere un RUC de 11 dígitos.");
        }
        if (documento != null && !documento.matches("\\d{8}|\\d{11}")) {
            throw new IllegalArgumentException("El documento del cliente debe tener 8 (DNI) u 11 (RUC) dígitos.");
        }
        Map<String, Integer> porSku = new TreeMap<>();
        for (ItemMovimiento item : items) {
            porSku.merge(item.getSku(), item.getCantidad(), Integer::sum);
        }

        try (Connection conexion = ConexionBD.abrir()) {
            conexion.setAutoCommit(false);
            try {
                Venta venta = guardar(conexion, porSku, tipoComprobante, factura, documento, usuario);
                conexion.commit();
                LOGGER.info("Venta {} registrada por '{}': total S/ {}.", venta.getNumeroComprobante(),
                        usuario, venta.getTotal());
                return venta;
            } catch (SQLException | RuntimeException exception) {
                conexion.rollback();
                throw exception;
            }
        } catch (SQLException exception) {
            LOGGER.error("Error al registrar la venta de '{}'.", usuario, exception);
            throw new DatosException("No se pudo registrar la venta.", exception);
        }
    }

    private Venta guardar(Connection conexion, Map<String, Integer> porSku, String tipo, boolean factura,
                          String documento, String usuario) throws SQLException {
        // Verifica existencia y stock (bloqueando las filas) ANTES de consumir un número de comprobante,
        // para no dejar huecos en la numeración cuando la venta se rechaza.
        for (Map.Entry<String, Integer> linea : porSku.entrySet()) {
            try (PreparedStatement bloqueo = conexion.prepareStatement(
                    "SELECT nombre, stock FROM producto WHERE sku = ? FOR UPDATE")) {
                bloqueo.setString(1, linea.getKey());
                try (ResultSet fila = bloqueo.executeQuery()) {
                    if (!fila.next()) {
                        throw new IllegalStateException("El producto " + linea.getKey() + " no existe.");
                    }
                    if (fila.getInt("stock") < linea.getValue()) {
                        throw new IllegalStateException("Stock insuficiente de " + fila.getString("nombre")
                                + ": hay " + fila.getInt("stock") + ".");
                    }
                }
            }
        }
        String numero;
        String consultaNumero = factura
                ? "SELECT 'F001-' || lpad(nextval('seq_factura')::text, 8, '0')"
                : "SELECT 'B001-' || lpad(nextval('seq_boleta')::text, 8, '0')";
        try (PreparedStatement sentencia = conexion.prepareStatement(consultaNumero);
             ResultSet fila = sentencia.executeQuery()) {
            fila.next();
            numero = fila.getString(1);
        }

        long ventaId;
        try (PreparedStatement insertar = conexion.prepareStatement(
                "INSERT INTO venta (usuario, tipo_comprobante, numero_comprobante, documento_cliente) "
                        + "VALUES (?, ?, ?, ?) RETURNING id")) {
            insertar.setString(1, usuario);
            insertar.setString(2, tipo);
            insertar.setString(3, numero);
            insertar.setString(4, documento);
            try (ResultSet fila = insertar.executeQuery()) {
                fila.next();
                ventaId = fila.getLong(1);
            }
        }

        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<String, Integer> linea : porSku.entrySet()) {
            String sku = linea.getKey();
            int cantidad = linea.getValue();
            // Descuenta el stock y deja la salida en el kardex (bloquea el producto con FOR UPDATE).
            KardexDAOJdbc.aplicar(conexion, sku, cantidad, "Salida", usuario, numero);
            BigDecimal precio;
            try (PreparedStatement consulta = conexion.prepareStatement("SELECT precio FROM producto WHERE sku = ?")) {
                consulta.setString(1, sku);
                try (ResultSet fila = consulta.executeQuery()) {
                    fila.next();
                    precio = fila.getBigDecimal(1);
                }
            }
            BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(cantidad));
            try (PreparedStatement detalle = conexion.prepareStatement(
                    "INSERT INTO venta_detalle (venta_id, sku, cantidad, precio_unitario, subtotal) "
                            + "VALUES (?, ?, ?, ?, ?)")) {
                detalle.setLong(1, ventaId);
                detalle.setString(2, sku);
                detalle.setInt(3, cantidad);
                detalle.setBigDecimal(4, precio);
                detalle.setBigDecimal(5, subtotal);
                detalle.executeUpdate();
            }
            total = total.add(subtotal);
        }
        try (PreparedStatement actualizar = conexion.prepareStatement(
                "UPDATE movimiento_kardex SET venta_id = ? WHERE motivo = ? AND tipo = 'Salida' AND venta_id IS NULL")) {
            actualizar.setLong(1, ventaId);
            actualizar.setString(2, numero);
            actualizar.executeUpdate();
        }
        try (PreparedStatement actualizar = conexion.prepareStatement("UPDATE venta SET total = ? WHERE id = ?")) {
            actualizar.setBigDecimal(1, total);
            actualizar.setLong(2, ventaId);
            actualizar.executeUpdate();
        }
        return new Venta(ventaId, tipo, numero, total);
    }
}
