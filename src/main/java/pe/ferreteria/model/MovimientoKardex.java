package pe.ferreteria.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Representa un movimiento de inventario y su saldo posterior. */
public class MovimientoKardex {
    private static final DateTimeFormatter FORMATO_CORTO = DateTimeFormatter.ofPattern("dd/MM");

    private final LocalDateTime fecha;
    private final String sku;
    private final String producto;
    private final String tipo;
    private final int cantidad;
    private final int stockResultante;
    private final String usuario;

    public MovimientoKardex(LocalDateTime fecha, String sku, String producto, String tipo,
                            int cantidad, int stockResultante, String usuario) {
        this.fecha = fecha;
        this.sku = sku;
        this.producto = producto;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.stockResultante = stockResultante;
        this.usuario = usuario;
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
}
