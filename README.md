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

## Cómo ejecutar
1. Copia `run.config.example.ps1` como `run.config.ps1` y pon las rutas de TU PC (este archivo no se sube a Git).
2. En la terminal del proyecto: `.\run.ps1` → abre `http://localhost:8090/ferresys/`
3. Detener: `Ctrl+C` o `.\stop.ps1`.
4. Pruebas: `mvn test`

## Reparto de módulos
### Catálogo: primer incremento funcional
- Administrador y almacenero pueden consultar y registrar productos desde **Catálogo e Inventario** (`/productos`; la URL anterior `/Pages/catalogo.jsp` sigue funcionando).
- El formulario solicita SKU único, nombre, una de las cuatro categorías actuales, unidad y precio de venta. La búsqueda consulta por nombre o SKU.
- Los cinco productos de la maqueta son la carga inicial. Los registros se comparten entre sesiones y se pierden al reiniciar Tomcat.
- Este incremento no modifica existencias: la conexión con entradas de almacén, edición, gestión de categorías y PostgreSQL se implementará en pasos posteriores.
- Comprobación: entrar como `admin` o `almacen`, guardar un producto y buscar su SKU; intentar repetirlo debe mostrar un error sin reemplazar el original. `cajero` no tiene acceso al módulo.

## Flujo de Git
- `main` siempre compila. Cada uno trabaja en su rama: `feature/catalogo`, `feature/ventas`, `feature/recepcion`, `feature/kardex`.
- Commits pequeños y frecuentes, con mensaje claro (`Agrega ProductoDAO y pruebas`). Cada uno commitea con su propia cuenta.
- Integrar con Pull Request a `main`; revisar que `mvn test` pase antes de aceptar.
- También suban los entregables (informe, diagramas) para tener evidencia del 100%.
- Nunca subir `target/`, `logs/` ni `run.config.ps1` (ya están en `.gitignore`).
