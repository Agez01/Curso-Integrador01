package pe.ferreteria.model;

/** Usuario autenticado. Guarda el hash de la clave, nunca la clave en texto plano. */
public class Usuario {
    private final String nombre;
    private final String claveHash;
    private final Rol rol;
    private final boolean activo;

    public Usuario(String nombre, String claveHash, Rol rol) {
        this(nombre, claveHash, rol, true);
    }

    public Usuario(String nombre, String claveHash, Rol rol, boolean activo) {
        this.activo = activo;
        this.nombre = nombre;
        this.claveHash = claveHash;
        this.rol = rol;
    }

    public String getNombre() {
        return nombre;
    }

    public String getClaveHash() {
        return claveHash;
    }

    public Rol getRol() {
        return rol;
    }

    public boolean isActivo() {
        return activo;
    }

    /** Iniciales para el avatar de la barra superior. */
    public String getIniciales() {
        return nombre.length() >= 2 ? nombre.substring(0, 2).toUpperCase() : nombre.toUpperCase();
    }
}
