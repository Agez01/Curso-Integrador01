package pe.ferreteria.dao;

import pe.ferreteria.model.Usuario;

import java.util.Optional;

/** Contrato de acceso a usuarios. Hoy hay implementación en memoria; mañana puede ser JDBC. */
public interface UsuarioDAO {
    Optional<Usuario> buscarPorNombre(String nombre);

    /** Devuelve el usuario si el nombre y la clave son correctos. */
    Optional<Usuario> autenticar(String nombre, String clave);
}
