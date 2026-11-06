package pe.ferreteria.model;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

/** Cliente para el comprobante: identificado por DNI (8 dígitos) o RUC (11 dígitos). */
public class Cliente {
    private final String documento;
    private final String nombre;
    private final String telefono;
    private final String direccion;

    public Cliente(String documento, String nombre, String telefono, String direccion) {
        this.documento = StringUtils.trimToEmpty(documento);
        this.nombre = StringUtils.trimToEmpty(nombre);
        this.telefono = StringUtils.trimToEmpty(telefono);
        this.direccion = StringUtils.trimToEmpty(direccion);
        Preconditions.checkArgument(this.documento.matches("\\d{8}|\\d{11}"),
                "El documento debe tener 8 (DNI) u 11 (RUC) dígitos.");
        Preconditions.checkArgument(!this.nombre.isEmpty() && this.nombre.length() <= 120,
                "El nombre es obligatorio (máximo 120 caracteres).");
        Preconditions.checkArgument(this.telefono.matches("[0-9+() -]{0,20}"), "El teléfono no es válido.");
        Preconditions.checkArgument(this.direccion.length() <= 160, "La dirección es demasiado larga.");
    }

    public String getDocumento() { return documento; }
    public String getNombre() { return nombre; }
    public String getTelefono() { return telefono; }
    public String getDireccion() { return direccion; }
    public String getTipoDocumento() { return documento.length() == 8 ? "DNI" : "RUC"; }
}
