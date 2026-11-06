package pe.ferreteria.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Representa un movimiento de inventario y su saldo posterior. */
public class MovimientoKardex {
    private static final DateTimeFormatter FORMATO_CORTO = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    private final LocalDateTime fecha;
    private final String sku;
    private final String producto;
    private final String tipo;
    private final int cantidad;
    private final int stockResultante;
    private final String usuario;
    private final String motivo;

    public MovimientoKardex(LocalDateTime fecha, String sku, String producto, String tipo,
                            int cantidad, int stockResultante, String usuario) {
        this(fecha, sku, producto, tipo, cantidad, stockResultante, usuario, null);
    }

    public MovimientoKardex(LocalDateTime fecha, String sku, String producto, String tipo,
                            int cantidad, int stockResultante, String usuario, String motivo) {
        this.fecha = fecha;
        this.sku = sku;
        this.producto = producto;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.stockResultante = stockResultante;
        this.usuario = usuario;
        this.motivo = motivo == null ? "" : motivo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getFechaTexto() {
        return fecha.format(FORMATO_CORTO);
    }

    public String getSku() {
        return sku;
    }

    public String getProducto() {
        return producto;
    }

    public String getTipo() {
        return tipo;
    }

    public int getCantidad() {
        return cantidad;
    }

    public int getStockResultante() {
        return stockResultante;
    }

    public String getUsuario() {
        return usuario;
    }

    /** Detalle del movimiento (comprobante de venta, proveedor, motivo de ajuste); puede estar vacío. */
    public String getMotivo() {
        return motivo;
    }
}
