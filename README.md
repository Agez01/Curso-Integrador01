# Ferresys — Sistema Web de Control de Almacén y Ventas
Ferretería El Tornillo S.A.C. · Curso Integrador I · Java (Servlets + JSP, Tomcat 10.1, Maven)

## Arquitectura (MVC + DAO + SOLID)
```
src/main/java/pe/ferreteria/
  controller/  Servlets (Controlador). Reciben la petición, llaman al DAO y hacen forward a la JSP.
  dao/         Interfaz + implementación (Modelo). Los servlets solo conocen la INTERFAZ, vía DaoFactory.
  model/       Clases de datos (Usuario, Rol, MovimientoKardex ...).
  filter/      AuthFilter: sesión obligatoria + permisos por rol.
  util/        PasswordUtil (hash PBKDF2), SesionUtil, ExcelExportador (Apache POI).
src/main/webapp/   JSP (Vista) + css/styles.css + WEB-INF/web.xml
src/test/java/     Pruebas JUnit 5 (TDD)
```
Reglas: la JSP no tiene SQL ni lógica de negocio · el servlet no accede a datos sin pasar por el DAO · cada DAO tiene interfaz (`XxxDAO`) + implementación (`XxxDAOMemoria`) y se registra en `DaoFactory`.

## Seguridad ya resuelta (no la rehagan)
- Claves con hash PBKDF2 + sal (`PasswordUtil`). Usuarios demo: `admin/admin123`, `cajero/cajero123`, `almacen/almacen123`.
- `AuthFilter` exige sesión y valida rol por ruta. **Al crear un módulo nuevo, agreguen su ruta en `REGLAS`.**
- Menú por rol: administrador ve todos los módulos; cajero ve Punto de Venta; almacenero ve Catálogo, Recepción y Kardex. El servidor mantiene las mismas restricciones aunque se escriba una URL directamente.
- Login con bloqueo tras 5 intentos fallidos (Guava Cache), nueva sesión al ingresar, cookie HttpOnly, expiración a los 30 min.
- El usuario de un movimiento sale SIEMPRE de la sesión, nunca de un parámetro.
- JSP: mostrar datos siempre con `<c:out>` (evita XSS). Nada de concatenar SQL: usar `PreparedStatement`.
- Logs con Logback: no escribir claves ni datos sensibles.

## Cómo ejecutar
1. Copia `run.config.example.ps1` como `run.config.ps1` y pon las rutas de TU PC (este archivo no se sube a Git).
2. En la terminal del proyecto: `.\run.ps1` → abre `http://localhost:8090/ferresys/`
3. Detener: `Ctrl+C` o `.\stop.ps1`.
4. Alternativa sin `run.ps1`: `mvn -Dtomcat.home="C:\ruta\a\apache-tomcat-10.1.x" package cargo:run`. Esto compila, inicia el Tomcat instalado y despliega el WAR; abre `http://localhost:8090/ferresys/`. Detén con `Ctrl+C`. No inicies este modo mientras otra instancia use el puerto 8090.
5. Pruebas: `mvn test`

## Reparto de módulos
| Integrante | Módulo (JSP + Servlet + DAO + pruebas) | Librería a evidenciar |
|---|---|---|
| Diego | Base, login/seguridad, Kardex y Reportes | Logback, Apache POI |
| Integrante 2 | Catálogo e Inventario (`catalogo.jsp`, `ProductoServlet`, `ProductoDAO`) | Google Guava |
| Integrante 3 | Punto de Venta (`pos.jsp`, `VentaServlet` + comprobante) | Apache Commons |
| Integrante 4 | Recepción de mercadería + Dashboard con datos reales | Commons / Logback |

Cada módulo está "terminado" cuando: (1) su pantalla es una JSP funcional con el CSS compartido, (2) usa su DAO por interfaz, (3) tiene ≥ 2 pruebas JUnit, (4) registra logs con Logback, (5) su ruta está en `AuthFilter.REGLAS`, (6) está en GitHub con commits propios.
Plantillas a copiar: `KardexServlet` + `kardex.jsp` (lectura/Excel), `MovimientoServlet` (POST), `KardexDAO`/`KardexDAOMemoria` (DAO).

## Flujo de Git
- `main` siempre compila. Cada uno trabaja en su rama: `feature/catalogo`, `feature/ventas`, `feature/recepcion`, `feature/kardex`.
- Commits pequeños y frecuentes, con mensaje claro (`Agrega ProductoDAO y pruebas`). Cada uno commitea con su propia cuenta.
- Integrar con Pull Request a `main`; revisar que `mvn test` pase antes de aceptar.
- También suban los entregables (informe, diagramas) para tener evidencia del 100%.
- Nunca subir `target/`, `logs/` ni `run.config.ps1` (ya están en `.gitignore`).
