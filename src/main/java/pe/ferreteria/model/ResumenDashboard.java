package pe.ferreteria.model;

import java.math.BigDecimal;
import java.util.List;

/** Indicadores del dashboard calculados desde la base de datos. */
public class ResumenDashboard {
    private final BigDecimal ventasHoy;
    private final int comprobantesHoy;
    private final int productosBajoStock;
    private final int movimientosHoy;
    private final List<Producto> alertas;

    public ResumenDashboard(BigDecimal ventasHoy, int comprobantesHoy, int productosBajoStock,
                            int movimientosHoy, List<Producto> alertas) {
        this.ventasHoy = ventasHoy;
        this.comprobantesHoy = comprobantesHoy;
        this.productosBajoStock = productosBajoStock;
        this.movimientosHoy = movimientosHoy;
        this.alertas = List.copyOf(alertas);
    }

    public BigDecimal getVentasHoy() { return ventasHoy; }
    public int getComprobantesHoy() { return comprobantesHoy; }
    public int getProductosBajoStock() { return productosBajoStock; }
    public int getMovimientosHoy() { return movimientosHoy; }
    public List<Producto> getAlertas() { return alertas; }
}
