package pe.ferreteria.dao;

import pe.ferreteria.model.ItemMovimiento;
import pe.ferreteria.model.Venta;

import java.util.List;

/** Registro de ventas con su comprobante. */
public interface VentaDAO {
    /**
     * Registra la venta, descuenta el stock y deja las salidas en el kardex, todo en una transacción.
     * Los precios se leen de la base de datos, nunca de la petición.
     *
     * @throws IllegalArgumentException si el comprobante o el documento del cliente son inválidos
     * @throws IllegalStateException    si un producto no existe o no tiene stock suficiente
     */
    Venta registrar(List<ItemMovimiento> items, String tipoComprobante, String documentoCliente, String usuario);
}
