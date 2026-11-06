package pe.ferreteria.model;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

/** Línea de una venta o de una recepción: un SKU y la cantidad. El precio siempre lo pone el servidor. */
public final class ItemMovimiento {
    public static final int CANTIDAD_MAXIMA = 9999;

    private final String sku;
    private final int cantidad;

    public ItemMovimiento(String sku, int cantidad) {
        this.sku = StringUtils.trimToEmpty(sku).toUpperCase(Locale.ROOT);
        Preconditions.checkArgument(this.sku.matches("[A-Z0-9][A-Z0-9_-]{0,29}"), "SKU inválido.");
        Preconditions.checkArgument(cantidad > 0 && cantidad <= CANTIDAD_MAXIMA,
                "La cantidad debe estar entre 1 y " + CANTIDAD_MAXIMA + ".");
        this.cantidad = cantidad;
    }

    public String getSku() {
        return sku;
    }

    public int getCantidad() {
        return cantidad;
    }

    /**
     * Convierte los parámetros paralelos sku[] y cantidad[] de un formulario en líneas válidas.
     * Ignora las filas completamente vacías.
     *
     * @throws IllegalArgumentException si no hay líneas, si falta un dato o si algún valor es inválido
     */
    public static java.util.List<ItemMovimiento> desdeParametros(String[] skus, String[] cantidades) {
        Preconditions.checkArgument(skus != null && cantidades != null && skus.length == cantidades.length,
                "Los datos del formulario están incompletos.");
        Preconditions.checkArgument(skus.length <= 50, "Máximo 50 líneas por operación.");
        java.util.List<ItemMovimiento> items = new java.util.ArrayList<>();
        for (int i = 0; i < skus.length; i++) {
            if (StringUtils.isBlank(skus[i]) && StringUtils.isBlank(cantidades[i])) {
                continue;
            }
            int cantidad;
            try {
                cantidad = Integer.parseInt(StringUtils.trimToEmpty(cantidades[i]));
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("La cantidad de la línea " + (i + 1) + " no es un número entero.");
            }
            items.add(new ItemMovimiento(skus[i], cantidad));
        }
        Preconditions.checkArgument(!items.isEmpty(), "Agrega al menos un producto.");
        return items;
    }
}
