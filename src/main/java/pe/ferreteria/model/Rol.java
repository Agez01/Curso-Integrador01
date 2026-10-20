package pe.ferreteria.model;

/** Roles del sistema; cada uno decide qué módulos puede abrir. */
public enum Rol {
    ADMINISTRADOR("Administrador"),
    CAJERO("Cajero"),
    ALMACENERO("Almacenero");

    private final String etiqueta;

    Rol(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
