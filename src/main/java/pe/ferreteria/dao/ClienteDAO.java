package pe.ferreteria.dao;

import pe.ferreteria.model.Cliente;

import java.util.List;

/** CU-08: gestión de clientes para la atención y el comprobante. */
public interface ClienteDAO {
    /** Filtra por documento o nombre; vacío devuelve todos. */
    List<Cliente> buscar(String texto);

    /** Crea el cliente o actualiza sus datos si el documento ya existe. */
    void guardar(Cliente cliente);
}
