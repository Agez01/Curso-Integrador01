package pe.ferreteria.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.ferreteria.model.Rol;
import pe.ferreteria.model.Usuario;
import pe.ferreteria.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/** Usuarios guardados en PostgreSQL (la clave se guarda como hash PBKDF2). */
public class UsuarioDAOJdbc implements UsuarioDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(UsuarioDAOJdbc.class);

    @Override
    public Optional<Usuario> buscarPorNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return Optional.empty();
        }
        String sql = "SELECT nombre, clave_hash, rol, activo FROM usuario WHERE nombre = ?";
        try (Connection conexion = ConexionBD.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, nombre);
            try (ResultSet filas = sentencia.executeQuery()) {
                if (filas.next()) {
                    return Optional.of(new Usuario(filas.getString("nombre"),
                            filas.getString("clave_hash"), Rol.valueOf(filas.getString("rol")), filas.getBoolean("activo")));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            LOGGER.error("Error al buscar el usuario '{}'.", nombre, exception);
            throw new DatosException("No se pudo consultar el usuario.", exception);
        }
    }

    @Override
    public Optional<Usuario> autenticar(String nombre, String clave) {
        return buscarPorNombre(nombre).filter(Usuario::isActivo).filter(u -> PasswordUtil.verificar(clave, u.getClaveHash()));
    }

    @Override
    public java.util.List<Usuario> listar() {
        String sql = "SELECT nombre, clave_hash, rol, activo FROM usuario ORDER BY nombre";
        try (Connection conexion = ConexionBD.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet filas = sentencia.executeQuery()) {
            java.util.List<Usuario> usuarios = new java.util.ArrayList<>();
            while (filas.next()) {
                usuarios.add(new Usuario(filas.getString("nombre"), filas.getString("clave_hash"),
                        Rol.valueOf(filas.getString("rol")), filas.getBoolean("activo")));
            }
            return usuarios;
        } catch (SQLException exception) {
            LOGGER.error("Error al listar usuarios.", exception);
            throw new DatosException("No se pudo consultar los usuarios.", exception);
        }
    }

    @Override
    public void crear(String nombre, String claveHash, Rol rol) {
        String sql = "INSERT INTO usuario (nombre, clave_hash, rol) VALUES (?, ?, ?)";
        try (Connection conexion = ConexionBD.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, nombre);
            sentencia.setString(2, claveHash);
            sentencia.setString(3, rol.name());
            sentencia.executeUpdate();
        } catch (SQLException exception) {
            if ("23505".equals(exception.getSQLState())) {
                throw new IllegalArgumentException("El usuario '" + nombre + "' ya existe.", exception);
            }
            LOGGER.error("Error al crear el usuario '{}'.", nombre, exception);
            throw new DatosException("No se pudo crear el usuario.", exception);
        }
    }

    @Override
    public void cambiarEstado(String nombre, boolean activo) {
        try (Connection conexion = ConexionBD.abrir();
             PreparedStatement sentencia = conexion.prepareStatement("UPDATE usuario SET activo = ? WHERE nombre = ?")) {
            sentencia.setBoolean(1, activo);
            sentencia.setString(2, nombre);
            if (sentencia.executeUpdate() == 0) {
                throw new IllegalArgumentException("El usuario no existe.");
            }
        } catch (SQLException exception) {
            LOGGER.error("Error al cambiar el estado de '{}'.", nombre, exception);
            throw new DatosException("No se pudo actualizar el usuario.", exception);
        }
    }
}
