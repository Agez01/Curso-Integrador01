package pe.ferreteria.dao;

import pe.ferreteria.model.ItemMovimiento;
import pe.ferreteria.model.MovimientoKardex;

import java.util.List;
import java.util.Locale;

/** Contrato de acceso al kardex (movimientos de inventario). */
public interface KardexDAO {
    List<MovimientoKardex> listar();

    /**
     * Registra una Entrada, Salida o Merma y actualiza el stock.
     *
     * @throws IllegalArgumentException si los datos son inválidos
     * @throws IllegalStateException    si una salida o merma supera las existencias
     */
    MovimientoKardex registrar(String sku, String producto, String tipo, int cantidad, String usuario);

    /**
     * Registra varias líneas del mismo tipo en una sola transacción: o se guardan todas o ninguna.
     * Solo la implementación JDBC la soporta.
     */
    default List<MovimientoKardex> registrarLote(List<ItemMovimiento> items, String tipo,
                                                 String usuario, String motivo) {
        throw new UnsupportedOperationException("Requiere la implementación con base de datos.");
    }

    /** Filtra por texto (SKU o producto) y por tipo; valores vacíos significan "sin filtro". */
    default List<MovimientoKardex> buscar(String texto, String tipo) {
        String consulta = texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
        return listar().stream()
                .filter(m -> tipo == null || tipo.isBlank() || m.getTipo().equals(tipo))
                .filter(m -> consulta.isEmpty()
                        || m.getSku().toLowerCase(Locale.ROOT).contains(consulta)
                        || m.getProducto().toLowerCase(Locale.ROOT).contains(consulta))
                .toList();
    }
}
