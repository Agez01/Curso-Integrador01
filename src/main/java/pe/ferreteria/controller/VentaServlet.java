package pe.ferreteria.controller;

import jakarta.servlet.annotation.WebServlet;

/** Registra salidas de inventario correspondientes a ventas. */
@WebServlet("/venta")
public class VentaServlet extends MovimientoServlet {
    @Override
    protected String tipoMovimiento() {
        return "Salida";
    }
}
