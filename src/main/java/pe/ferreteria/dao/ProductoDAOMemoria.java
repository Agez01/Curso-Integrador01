package pe.ferreteria.dao;

import com.google.common.base.Preconditions;
import pe.ferreteria.model.Producto;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Catálogo compartido durante la ejecución de la aplicación. */
public class ProductoDAOMemoria implements ProductoDAO {
    private final Map<String, Producto> productos = new LinkedHashMap<>();

    public ProductoDAOMemoria() {
        registrar(new Producto("FR-1042", "Martillo carpintero 16oz", "Ferretería", "Unidad", new BigDecimal("28.50")));
        registrar(new Producto("CN-0087", "Cemento Sol Tipo I x 42.5kg", "Construcción", "Bolsa", new BigDecimal("31.00")));
        registrar(new Producto("GF-0215", "Tubo PVC 1/2\" x 5m", "Gasfitería", "Unidad", new BigDecimal("14.90")));
        registrar(new Producto("PT-0330", "Pintura látex blanco 1gal", "Pinturas", "Galón", new BigDecimal("46.00")));
        registrar(new Producto("FR-1108", "Tornillos autorroscantes 1\" (caja x100)", "Ferretería", "Caja", new BigDecimal("9.50")));
    }

    @Override
    public synchronized List<Producto> listar() {
        return List.copyOf(productos.values());
    }

    @Override
    public synchronized void registrar(Producto producto) {
        Preconditions.checkArgument(producto != null, "El producto es obligatorio.");
        Preconditions.checkArgument(!productos.containsKey(producto.getSku()), "Ya existe un producto con ese SKU.");
        productos.put(producto.getSku(), producto);
    }
}
