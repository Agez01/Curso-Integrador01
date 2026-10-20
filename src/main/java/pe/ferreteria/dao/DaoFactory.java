package pe.ferreteria.dao;

/**
 * Único lugar que decide qué implementación de cada DAO se usa.
 * Para pasar a PostgreSQL solo se cambian estas líneas (principio de inversión de dependencias).
 */
public final class DaoFactory {
    private static final UsuarioDAO USUARIO_DAO = new UsuarioDAOMemoria();
    private static final KardexDAO KARDEX_DAO = new KardexDAOMemoria();

    private DaoFactory() {
    }

    public static UsuarioDAO usuarioDAO() {
        return USUARIO_DAO;
    }

    public static KardexDAO kardexDAO() {
        return KARDEX_DAO;
    }
}
