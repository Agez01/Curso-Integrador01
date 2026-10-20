package pe.ferreteria.dao;

import com.google.common.collect.ImmutableMap;
import pe.ferreteria.model.Rol;
import pe.ferreteria.model.Usuario;
import pe.ferreteria.util.PasswordUtil;

import java.util.Map;
import java.util.Optional;

/** Usuarios de demostración en memoria. Las claves están guardadas como hash PBKDF2. */
public class UsuarioDAOMemoria implements UsuarioDAO {
    // Claves de demo: admin/admin123, cajero/cajero123, almacen/almacen123
    private static final Map<String, Usuario> USUARIOS = ImmutableMap.of(
            "admin", new Usuario("admin",
                    "120000:NexdckkJg7im2uW/Bovd1Q==:aKrScA2k75od4KnqOcWkmLcLpbzpKfDtcBb/wTH6umE=",
                    Rol.ADMINISTRADOR),
            "cajero", new Usuario("cajero",
                    "120000:2Ji+v2V1RzTObhqdcevBZA==:bBb35vwkeC6n1+8HvCteHNJv0O1mxYS6Ip6PKHRMAp0=",
                    Rol.CAJERO),
            "almacen", new Usuario("almacen",
                    "120000:DOABRnxq+C2d5qm6VsxheQ==:LU1Ehk04L9zDK/OlIXd/125GR79aIzNjWMFwPgoN9Yw=",
                    Rol.ALMACENERO));

    @Override
    public Optional<Usuario> buscarPorNombre(String nombre) {
        return Optional.ofNullable(nombre == null ? null : USUARIOS.get(nombre));
    }

    @Override
    public Optional<Usuario> autenticar(String nombre, String clave) {
        return buscarPorNombre(nombre).filter(u -> PasswordUtil.verificar(clave, u.getClaveHash()));
    }
}
