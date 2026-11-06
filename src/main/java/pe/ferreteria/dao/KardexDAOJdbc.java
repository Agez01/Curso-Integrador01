package pe.ferreteria.dao;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.ferreteria.model.ItemMovimiento;
import pe.ferreteria.model.MovimientoKardex;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Kardex en PostgreSQL. Cada lote es una transacción que bloquea los productos con FOR UPDATE. */
public class KardexDAOJdbc implements KardexDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(KardexDAOJdbc.class);
    private static final ZoneId ZONA = ZoneId.of("America/Lima");
    private static final int LIMITE = 500;
    private static final List<String> TIPOS = List.of("Entrada", "Salida", "Merma");

    private static final String SELECT = "SELECT m.fecha, m.sku, p.nombre, m.tipo, m.cantidad, "
            + "m.stock_resultante, m.usuario, m.motivo FROM movimiento_kardex m JOIN producto p ON p.sku = m.sku ";

    @Override
    public List<MovimientoKardex> listar() {
        return buscar(null, null);
    }

    @Override
    public List<MovimientoKardex> buscar(String texto, String tipo) {
        String consulta = StringUtils.trimToEmpty(texto);
        String sql = SELECT + "WHERE (? = '' OR m.sku ILIKE ? OR p.nombre ILIKE ?) "
                + "AND (? = '' OR m.tipo = ?) ORDER BY m.fecha DESC, m.id DESC LIMIT " + LIMITE;
        String patron = "%" + consulta.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
        String tipoFiltro = StringUtils.trimToEmpty(tipo);
        try (Connection conexion = ConexionBD.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, consulta);
            sentencia.setString(2, patron);
            sentencia.setString(3, patron);
            sentencia.setString(4, tipoFiltro);
            sentencia.setString(5, tipoFiltro);
            try (ResultSet filas = sentencia.executeQuery()) {
                List<MovimientoKardex> movimientos = new ArrayList<>();
                while (filas.next()) {
                    movimientos.add(desdeFila(filas));
                }
                return movimientos;
            }
        } catch (SQLException exception) {
            LOGGER.error("Error al consultar el kardex.", exception);
            throw new DatosException("No se pudo consultar el kardex.", exception);
        }
    }

    @Override
    public MovimientoKardex registrar(String sku, String producto, String tipo, int cantidad, String usuario) {
        return registrarLote(List.of(new ItemMovimiento(sku, cantidad)), tipo, usuario, null).get(0);
    }

    @Override
    public List<MovimientoKardex> registrarLote(List<ItemMovimiento> items, String tipo,
                                                String usuario, String motivo) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Agrega al menos un producto.");
        }
        if (!TIPOS.contains(tipo)) {
            throw new IllegalArgumentException("El tipo debe ser Entrada, Salida o Merma.");
        }
        if (StringUtils.isBlank(usuario)) {
            throw new IllegalArgumentException("El usuario es obligatorio.");
        }
        String detalle = StringUtils.abbreviate(StringUtils.trimToNull(motivo), 200);
        Map<String, Integer> porSku = new TreeMap<>(); // orden fijo de bloqueo: evita interbloqueos
        for (ItemMovimiento item : items) {
            porSku.merge(item.getSku(), item.getCantidad(), Integer::sum);
        }

        try (Connection conexion = ConexionBD.abrir()) {
            conexion.setAutoCommit(false);
            try {
                List<MovimientoKardex> registrados = new ArrayList<>();
                for (Map.Entry<String, Integer> linea : porSku.entrySet()) {
                    registrados.add(aplicar(conexion, linea.getKey(), linea.getValue(), tipo, usuario, detalle));
                }
                conexion.commit();
                return registrados;
            } catch (SQLException | RuntimeException exception) {
                conexion.rollback();
                throw exception;
            }
        } catch (SQLException exception) {
            LOGGER.error("Error al registrar {} en el kardex.", tipo, exception);
            throw new DatosException("No se pudo registrar el movimiento.", exception);
        }
    }

    /** Aplica una línea dentro de la transacción abierta por el llamador. */
    static MovimientoKardex aplicar(Connection conexion, String sku, int cantidad, String tipo,
                                    String usuario, String motivo) throws SQLException {
        String nombre;
        int stock;
        try (PreparedStatement bloqueo = conexion.prepareStatement(
                "SELECT nombre, stock FROM producto WHERE sku = ? FOR UPDATE")) {
            bloqueo.setString(1, sku);
            try (ResultSet fila = bloqueo.executeQuery()) {
                if (!fila.next()) {
                    throw new IllegalStateException("El producto " + sku + " no existe.");
                }
                nombre = fila.getString("nombre");
                stock = fila.getInt("stock");
            }
        }
        long nuevo = "Entrada".equals(tipo) ? (long) stock + cantidad : (long) stock - cantidad;
        if (nuevo < 0) {
            throw new IllegalStateException("Stock insuficiente de " + nombre + ": hay " + stock + ".");
        }
        if (nuevo > Integer.MAX_VALUE) {
            throw new IllegalStateException("El stock de " + nombre + " excede el máximo permitido.");
        }
        try (PreparedStatement actualizar = conexion.prepareStatement("UPDATE producto SET stock = ? WHERE sku = ?")) {
            actualizar.setInt(1, (int) nuevo);
            actualizar.setString(2, sku);
            actualizar.executeUpdate();
        }
        Timestamp ahora;
        try (PreparedStatement insertar = conexion.prepareStatement(
                "INSERT INTO movimiento_kardex (sku, tipo, cantidad, stock_resultante, usuario, motivo) "
                        + "VALUES (?, ?, ?, ?, ?, ?) RETURNING fecha")) {
            insertar.setString(1, sku);
            insertar.setString(2, tipo);
            insertar.setInt(3, cantidad);
            insertar.setInt(4, (int) nuevo);
            insertar.setString(5, usuario);
            insertar.setString(6, motivo);
            try (ResultSet fila = insertar.executeQuery()) {
                fila.next();
                ahora = fila.getTimestamp(1);
            }
        }
        return new MovimientoKardex(ahora.toInstant().atZone(ZONA).toLocalDateTime(), sku, nombre, tipo,
                cantidad, (int) nuevo, usuario, motivo);
    }

    private static MovimientoKardex desdeFila(ResultSet fila) throws SQLException {
        return new MovimientoKardex(fila.getTimestamp("fecha").toInstant().atZone(ZONA).toLocalDateTime(),
                fila.getString("sku"), fila.getString("nombre"), fila.getString("tipo"),
                fila.getInt("cantidad"), fila.getInt("stock_resultante"), fila.getString("usuario"),
                fila.getString("motivo"));
    }
}
