package pe.ferreteria.dao;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pe.ferreteria.model.ItemMovimiento;
import pe.ferreteria.model.Venta;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Pruebas contra PostgreSQL real. Se omiten si no hay base configurada (db.properties o DB_URL).
 * Requieren la base creada con db/02_esquema.sql y db/03_datos_demo.sql; NO usar con datos reales.
 */
class JdbcIntegracionTest {
    @BeforeAll
    static void hayBase() {
        assumeTrue(ConexionBD.disponible(), "PostgreSQL no configurado: se omiten las pruebas de integración.");
    }

    @Test
    void autenticaAlAdministradorDesdeLaBase() {
        assertTrue(new UsuarioDAOJdbc().autenticar("admin", "admin123").isPresent());
        assertTrue(new UsuarioDAOJdbc().autenticar("admin", "incorrecta").isEmpty());
    }

    @Test
    void ventaDescuentaStockYNumeraElComprobante() {
        int antes = stock("FR-1042");
        Venta venta = new VentaDAOJdbc().registrar(List.of(new ItemMovimiento("FR-1042", 1)),
                "Boleta", "12345678", "cajero");
        assertTrue(venta.getNumeroComprobante().startsWith("B001-"));
        assertEquals(0, new BigDecimal("28.50").compareTo(venta.getTotal()));
        assertEquals(antes - 1, stock("FR-1042"));
    }

    @Test
    void ventaConStockInsuficienteNoCambiaNada() {
        int antes = stock("CN-0087");
        assertThrows(IllegalStateException.class, () -> new VentaDAOJdbc().registrar(
                List.of(new ItemMovimiento("CN-0087", 9999)), "Boleta", null, "cajero"));
        assertEquals(antes, stock("CN-0087"));
    }

    @Test
    void facturaExigeRuc() {
        assertThrows(IllegalArgumentException.class, () -> new VentaDAOJdbc().registrar(
                List.of(new ItemMovimiento("FR-1042", 1)), "Factura", "12345678", "cajero"));
    }

    @Test
    void loteConUnaLineaInvalidaSeRevierteCompleto() {
        int antes = stock("FR-1042");
        assertThrows(IllegalStateException.class, () -> new KardexDAOJdbc().registrarLote(
                List.of(new ItemMovimiento("FR-1042", 1), new ItemMovimiento("NO-EXISTE", 1)),
                "Entrada", "almacen", "prueba"));
        assertEquals(antes, stock("FR-1042"));
    }

    private static int stock(String sku) {
        return new ProductoDAOJdbc().listar().stream().filter(p -> p.getSku().equals(sku))
                .findFirst().orElseThrow().getStock();
    }
}
