package pe.ferreteria.dao;

import pe.ferreteria.model.Usuario;

import java.util.Optional;

/** Contrato de acceso a usuarios. Hoy hay implementación en memoria; mañana puede ser JDBC. */
public interface UsuarioDAO {
    Optional<Usuario> buscarPorNombre(String nombre);

    /** Devuelve el usuario si el nombre y la clave son correctos. */
    Optional<Usuario> autenticar(String nombre, String clave);

    /** Lista todos los usuarios (solo la implementación con base de datos). */
    default java.util.List<Usuario> listar() {
        throw new UnsupportedOperationException("Requiere la implementación con base de datos.");
    }

    /** Crea un usuario con la clave ya hasheada; rechaza nombres repetidos con IllegalArgumentException. */
    default void crear(String nombre, String claveHash, pe.ferreteria.model.Rol rol) {
        throw new UnsupportedOperationException("Requiere la implementación con base de datos.");
    }

    /** Bloquea o reactiva a un usuario. */
    default void cambiarEstado(String nombre, boolean activo) {
        throw new UnsupportedOperationException("Requiere la implementación con base de datos.");
    }
}
