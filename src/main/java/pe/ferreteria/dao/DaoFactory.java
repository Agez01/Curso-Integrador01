package pe.ferreteria.dao;

/**
 * Único lugar que decide qué implementación de cada DAO se usa (inversión de dependencias).
 * Por defecto usa PostgreSQL; con la variable de entorno FERRESYS_DAO=memoria se usan los datos
 * de demostración en memoria (solo usuarios, catálogo y kardex; ventas y dashboard requieren PostgreSQL).
 */
public final class DaoFactory {
    private static final boolean MEMORIA = "memoria".equalsIgnoreCase(System.getenv("FERRESYS_DAO"));

    private static final UsuarioDAO USUARIO_DAO = MEMORIA ? new UsuarioDAOMemoria() : new UsuarioDAOJdbc();
    private static final KardexDAO KARDEX_DAO = MEMORIA ? new KardexDAOMemoria() : new KardexDAOJdbc();
    private static final ProductoDAO PRODUCTO_DAO = MEMORIA ? new ProductoDAOMemoria() : new ProductoDAOJdbc();
    private static final VentaDAO VENTA_DAO = new VentaDAOJdbc();
    private static final DashboardDAO DASHBOARD_DAO = new DashboardDAOJdbc();
    private static final ClienteDAO CLIENTE_DAO = new ClienteDAOJdbc();

    private DaoFactory() {
    }

    public static UsuarioDAO usuarioDAO() {
        return USUARIO_DAO;
    }

    public static KardexDAO kardexDAO() {
        return KARDEX_DAO;
    }

    public static ProductoDAO productoDAO() {
        return PRODUCTO_DAO;
    }

    public static VentaDAO ventaDAO() {
        return VENTA_DAO;
    }

    public static ClienteDAO clienteDAO() {
        return CLIENTE_DAO;
    }

    public static DashboardDAO dashboardDAO() {
        return DASHBOARD_DAO;
    }
}
