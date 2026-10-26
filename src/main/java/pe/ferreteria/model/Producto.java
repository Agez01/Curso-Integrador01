package pe.ferreteria.model;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

/** Datos del catálogo. Las existencias se incorporarán con los movimientos de almacén. */
public class Producto {
    public static final List<String> CATEGORIAS = List.of(
            "Ferretería", "Construcción", "Gasfitería", "Pinturas");

    private final String sku;
    private final String nombre;
    private final String categoria;
    private final String unidad;
    private final BigDecimal precio;

    public Producto(String sku, String nombre, String categoria, String unidad, BigDecimal precio) {
        this.sku = StringUtils.trimToEmpty(sku).toUpperCase(Locale.ROOT);
        this.nombre = StringUtils.trimToEmpty(nombre);
        this.categoria = StringUtils.trimToEmpty(categoria);
        this.unidad = StringUtils.trimToEmpty(unidad);
        Preconditions.checkArgument(this.sku.matches("[A-Z0-9][A-Z0-9_-]{0,29}"),
                "El SKU debe tener entre 1 y 30 letras, números, guiones o guiones bajos.");
        Preconditions.checkArgument(!this.nombre.isEmpty() && this.nombre.length() <= 120,
                "El nombre es obligatorio y debe tener hasta 120 caracteres.");
        Preconditions.checkArgument(CATEGORIAS.contains(this.categoria), "Selecciona una categoría válida.");
        Preconditions.checkArgument(!this.unidad.isEmpty() && this.unidad.length() <= 30,
                "La unidad es obligatoria y debe tener hasta 30 caracteres.");
        Preconditions.checkArgument(precio != null && precio.signum() >= 0
                        && precio.compareTo(new BigDecimal("999999999.99")) <= 0,
                "El precio debe estar entre 0 y 999999999.99.");
        try {
            this.precio = precio.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("El precio debe tener como máximo dos decimales.");
        }
    }

    public String getSku() { return sku; }
    public String getNombre() { return nombre; }
    public String getCategoria() { return categoria; }
    public String getUnidad() { return unidad; }
    public BigDecimal getPrecio() { return precio; }
}
