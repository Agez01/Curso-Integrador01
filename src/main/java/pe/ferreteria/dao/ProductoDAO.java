package pe.ferreteria.dao;

import pe.ferreteria.model.Producto;
import java.util.List;

/** Consulta y registro del catálogo, sin modificar existencias. */
public interface ProductoDAO {
    List<Producto> listar();
    /** Rechaza un SKU ya registrado. */
    void registrar(Producto producto);
}
