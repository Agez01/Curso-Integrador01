package pe.ferreteria.controller;

import jakarta.servlet.annotation.WebServlet;

/** Registra entradas de mercadería en el inventario. */
@WebServlet("/entrada")
public class EntradaServlet extends MovimientoServlet {
    @Override
    protected String tipoMovimiento() {
        return "Entrada";
    }
}
