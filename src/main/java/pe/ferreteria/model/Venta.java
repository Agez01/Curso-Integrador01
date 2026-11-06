package pe.ferreteria.model;

import java.math.BigDecimal;

/** Resultado de registrar una venta. */
public class Venta {
    private final long id;
    private final String tipoComprobante;
    private final String numeroComprobante;
    private final BigDecimal total;

    public Venta(long id, String tipoComprobante, String numeroComprobante, BigDecimal total) {
        this.id = id;
        this.tipoComprobante = tipoComprobante;
        this.numeroComprobante = numeroComprobante;
        this.total = total;
    }

    public long getId() { return id; }
    public String getTipoComprobante() { return tipoComprobante; }
    public String getNumeroComprobante() { return numeroComprobante; }
    public BigDecimal getTotal() { return total; }
}
