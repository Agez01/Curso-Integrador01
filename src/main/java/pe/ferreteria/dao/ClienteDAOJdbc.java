package pe.ferreteria.dao;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.ferreteria.model.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAOJdbc implements ClienteDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClienteDAOJdbc.class);

    @Override
    public List<Cliente> buscar(String texto) {
        String consulta = StringUtils.trimToEmpty(texto);
        String patron = "%" + consulta.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
        String sql = "SELECT documento, nombre, telefono, direccion FROM cliente "
                + "WHERE (? = '' OR documento LIKE ? OR nombre ILIKE ?) ORDER BY nombre LIMIT 200";
        try (Connection conexion = ConexionBD.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, consulta);
            sentencia.setString(2, patron);
            sentencia.setString(3, patron);
            try (ResultSet filas = sentencia.executeQuery()) {
                List<Cliente> clientes = new ArrayList<>();
                while (filas.next()) {
                    clientes.add(new Cliente(filas.getString("documento"), filas.getString("nombre"),
                            filas.getString("telefono"), filas.getString("direccion")));
                }
                return clientes;
            }
        } catch (SQLException exception) {
            LOGGER.error("Error al buscar clientes.", exception);
            throw new DatosException("No se pudo consultar los clientes.", exception);
        }
    }

    @Override
    public void guardar(Cliente cliente) {
        String sql = "INSERT INTO cliente (documento, nombre, telefono, direccion) VALUES (?, ?, ?, ?) "
                + "ON CONFLICT (documento) DO UPDATE SET nombre = EXCLUDED.nombre, "
                + "telefono = EXCLUDED.telefono, direccion = EXCLUDED.direccion";
        try (Connection conexion = ConexionBD.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, cliente.getDocumento());
            sentencia.setString(2, cliente.getNombre());
            sentencia.setString(3, StringUtils.trimToNull(cliente.getTelefono()));
            sentencia.setString(4, StringUtils.trimToNull(cliente.getDireccion()));
            sentencia.executeUpdate();
        } catch (SQLException exception) {
            LOGGER.error("Error al guardar el cliente '{}'.", cliente.getDocumento(), exception);
            throw new DatosException("No se pudo guardar el cliente.", exception);
        }
    }
}
