package pe.ferreteria.dao;

import pe.ferreteria.model.MovimientoKardex;

import java.util.List;

/** Contrato de acceso al kardex (movimientos de inventario). */
public interface KardexDAO {
    List<MovimientoKardex> listar();

    /**
     * Registra una Entrada o Salida y actualiza el stock.
     *
     * @throws IllegalArgumentException si los datos son inválidos
     * @throws IllegalStateException    si una salida supera las existencias
     */
    MovimientoKardex registrar(String sku, String producto, String tipo, int cantidad, String usuario);
}
