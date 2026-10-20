package pe.ferreteria.dao;

import pe.ferreteria.model.MovimientoKardex;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Mantiene movimientos de demostración en memoria, sin conexión a una base de datos. */
public class KardexDAO {
    private static final KardexDAO INSTANCIA = new KardexDAO();
    private final List<MovimientoKardex> movimientos = new ArrayList<>();
    private final Map<String, Integer> existencias = new HashMap<>();

    private KardexDAO() {
        LocalDateTime fecha = LocalDateTime.of(2026, 8, 25, 10, 0);
        movimientos.add(new MovimientoKardex(fecha, "CN-0087", "Cemento Sol Tipo I",
                "Entrada", 80, 83, "almacén-01"));
        movimientos.add(new MovimientoKardex(fecha.minusMinutes(20), "FR-1108",
                "Tornillos autorroscantes 1\"", "Salida", 12, 58, "almacén-01"));
        movimientos.add(new MovimientoKardex(fecha.minusDays(1), "PT-0330",
                "Pintura látex blanco", "Merma", 2, 9, "almacén-01"));
        movimientos.add(new MovimientoKardex(fecha.minusDays(1).minusMinutes(30), "GF-0215",
                "Tubo PVC 1/2\"", "Salida", 5, 11, "almacén-01"));

        existencias.put("Cemento Sol Tipo I", 83);
        existencias.put("Tornillos autorroscantes 1\"", 58);
        existencias.put("Pintura látex blanco", 9);
        existencias.put("Tubo PVC 1/2\"", 11);
    }

    public static KardexDAO getInstancia() {
        return INSTANCIA;
    }

    public synchronized List<MovimientoKardex> listar() {
        return List.copyOf(movimientos);
    }

    public synchronized MovimientoKardex registrar(String sku, String producto, String tipo,
                                                    int cantidad, String usuario) {
        if (producto == null || producto.isBlank() || usuario == null || usuario.isBlank()) {
            throw new IllegalArgumentException("El producto y el usuario son obligatorios.");
        }
        if (!"Entrada".equals(tipo) && !"Salida".equals(tipo)) {
            throw new IllegalArgumentException("El tipo debe ser Entrada o Salida.");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        }

        int stockActual = existencias.getOrDefault(producto, 0);
        if ("Salida".equals(tipo) && cantidad > stockActual) {
            throw new IllegalStateException("No hay existencias suficientes para la salida.");
        }
        int stockNuevo = "Entrada".equals(tipo) ? Math.addExact(stockActual, cantidad)
                : stockActual - cantidad;
        existencias.put(producto, stockNuevo);

        MovimientoKardex movimiento = new MovimientoKardex(LocalDateTime.now(),
                sku == null || sku.isBlank() ? "-" : sku, producto, tipo, cantidad, stockNuevo, usuario);
        movimientos.add(0, movimiento);
        return movimiento;
    }
}
