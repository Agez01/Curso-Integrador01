package pe.ferreteria.filter;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.ferreteria.model.Rol;
import pe.ferreteria.model.Usuario;
import pe.ferreteria.util.SesionUtil;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Seguridad de acceso: exige sesión iniciada y valida el rol por ruta.
 * Para proteger un módulo nuevo, agrega su ruta en REGLAS.
 */
@WebFilter("/*")
public class AuthFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthFilter.class);

    private static final Set<String> RUTAS_PUBLICAS = ImmutableSet.of("/login", "/login.jsp");
    private static final String PREFIJO_RECURSOS = "/css/";

    /** Ruta (prefijo) -> roles autorizados. Lo que no figura aquí solo exige sesión iniciada. */
    private static final Map<String, Set<Rol>> REGLAS = ImmutableMap.<String, Set<Rol>>builder()
            // CU-02 Gestionar usuarios y roles: Administrador
            .put("/usuarios", ImmutableSet.of(Rol.ADMINISTRADOR))
            // CU-08 Gestionar clientes: Cajero y Administrador
            .put("/clientes", ImmutableSet.of(Rol.ADMINISTRADOR, Rol.CAJERO))
            // CU-03 Gestionar catálogo: Administrador y Almacenero
            .put("/productos", ImmutableSet.of(Rol.ADMINISTRADOR, Rol.ALMACENERO))
            .put("/Pages/catalogo", ImmutableSet.of(Rol.ADMINISTRADOR, Rol.ALMACENERO))
            // CU-04 Registrar entrada de mercadería: Almacenero
            .put("/entrada", ImmutableSet.of(Rol.ALMACENERO))
            .put("/Pages/recepcion", ImmutableSet.of(Rol.ALMACENERO))
            // CU-05 Ajustar existencias (merma): Almacenero y Administrador
            .put("/ajuste", ImmutableSet.of(Rol.ADMINISTRADOR, Rol.ALMACENERO))
            // CU-07 Registrar venta y comprobante: Cajero
            .put("/venta", ImmutableSet.of(Rol.CAJERO))
            .put("/Pages/ventas", ImmutableSet.of(Rol.CAJERO))
            // CU-09 Consultar kardex y exportar: Administrador y Almacenero
            .put("/kardex", ImmutableSet.of(Rol.ADMINISTRADOR, Rol.ALMACENERO))
            .build();

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String ruta = request.getServletPath();

        if (RUTAS_PUBLICAS.contains(ruta) || ruta.startsWith(PREFIJO_RECURSOS)) {
            chain.doFilter(req, res);
            return;
        }

        Usuario usuario = SesionUtil.usuarioActual(request);
        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        for (Map.Entry<String, Set<Rol>> regla : REGLAS.entrySet()) {
            if (ruta.startsWith(regla.getKey()) && !regla.getValue().contains(usuario.getRol())) {
                LOGGER.warn("Acceso denegado: usuario='{}' (rol {}) intentó abrir '{}'.",
                        usuario.getNombre(), usuario.getRol(), ruta);
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "No tienes permiso para este módulo.");
                return;
            }
        }

        // Evita que el navegador guarde páginas privadas tras cerrar sesión.
        response.setHeader("Cache-Control", "no-store");
        chain.doFilter(req, res);
    }
}
