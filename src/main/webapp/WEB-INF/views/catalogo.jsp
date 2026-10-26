<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Catálogo e Inventario | Ferretería El Tornillo</title>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link href="https://fonts.googleapis.com/css2?family=Oswald:wght@500;600;700&family=Inter:wght@400;500;600;700&family=IBM+Plex+Mono:wght@500;600&display=swap" rel="stylesheet">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/pages.css?v=2">
</head>
<body>
<!-- .app es el padre general y usa Flexbox para sidebar y main. -->
<div class="app">
  <!-- Sidebar hijo de .app; sus elementos se organizan verticalmente con Flexbox. -->
  <aside class="sidebar">
    <div class="brand"><span class="stamp">FERRESYS · JAVA EE</span><h1>Ferretería<br>El Tornillo S.A.C.</h1><p>Alternativa 1 - Web JSP/Servlets</p></div>
    <%@ include file="/WEB-INF/jspf/sidebar-nav.jspf" %>
  </aside>
  <div class="hazard hazard-fixed"></div>
  <!-- main es hijo de .app y usa Flexbox vertical para topbar y content. -->
  <main class="main">
    <div class="topbar"><span class="path">/inventario/catalogo</span><div class="user"><span class="role-pill">Rol: <c:out value="${sessionScope.usuario.rol.etiqueta}"/></span><div class="avatar"><c:out value="${sessionScope.usuario.iniciales}"/></div></div></div>
    <div class="content">
      <!-- Página de catálogo: toolbar Flexbox, tabla hija de su tarjeta. -->
      <section class="view active">
        <div class="page-head"><div class="eyebrow">Pantalla 02</div><h2>Gestión de Catálogo e Inventario</h2></div>
        <div class="divider"></div>
        <c:if test="${not empty exito}"><p class="catalogo-message catalogo-success" role="status"><c:out value="${exito}"/></p></c:if>
        <c:if test="${not empty error}"><p class="catalogo-message catalogo-error" role="alert"><c:out value="${error}"/></p></c:if>
        <form class="toolbar" method="get" action="${pageContext.request.contextPath}/productos">
          <label for="buscar-producto">Buscar producto</label>
          <input class="search" id="buscar-producto" name="q" maxlength="120" placeholder="Nombre o SKU" value="<c:out value='${busqueda}'/>">
          <button class="btn btn-ghost" type="submit">Buscar</button>
          <a class="btn btn-ghost" href="${pageContext.request.contextPath}/productos">Ver todos</a>
          <a class="btn btn-primary" href="#nuevo-producto">+ Nuevo producto</a>
        </form>
        <div class="card table-card"><table>
          <thead><tr><th>SKU</th><th>Producto</th><th>Categoría</th><th>Unidad</th><th>Precio</th></tr></thead>
          <tbody>
          <c:forEach var="producto" items="${productos}">
            <tr><td class="mono"><c:out value="${producto.sku}"/></td><td><c:out value="${producto.nombre}"/></td><td><c:out value="${producto.categoria}"/></td><td><c:out value="${producto.unidad}"/></td><td>S/ <c:out value="${producto.precio}"/></td></tr>
          </c:forEach>
          <c:if test="${empty productos}"><tr><td colspan="5">No se encontraron productos.</td></tr></c:if>
          </tbody>
        </table></div>
        <p class="stub-note">Catálogo de demostración en memoria: los nuevos registros se pierden al reiniciar la aplicación.</p>
        <section class="card catalogo-form" id="nuevo-producto" aria-labelledby="titulo-producto">
          <h3 class="section-title" id="titulo-producto">Registrar producto</h3>
          <form class="catalogo-fields" method="post" action="${pageContext.request.contextPath}/productos">
            <input type="hidden" name="token" value="<c:out value='${sessionScope.tokenCatalogo}'/>">
            <div class="catalogo-field"><label for="sku">SKU</label><input id="sku" name="sku" maxlength="30" pattern="[A-Za-z0-9][A-Za-z0-9_-]{0,29}" required value="<c:out value='${param.sku}'/>"></div>
            <div class="catalogo-field"><label for="nombre">Nombre del producto</label><input id="nombre" name="nombre" maxlength="120" required value="<c:out value='${param.nombre}'/>"></div>
            <div class="catalogo-field"><label for="categoria">Categoría</label><select id="categoria" name="categoria" required>
                <option value="">Selecciona una categoría</option>
                <c:forEach var="categoria" items="${categorias}"><option value="<c:out value='${categoria}'/>" ${param.categoria eq categoria ? 'selected' : ''}><c:out value="${categoria}"/></option></c:forEach>
              </select></div>
            <div class="catalogo-field"><label for="unidad">Unidad de medida</label><input id="unidad" name="unidad" maxlength="30" placeholder="Unidad, caja, bolsa…" required value="<c:out value='${param.unidad}'/>"></div>
            <div class="catalogo-field"><label for="precio">Precio de venta (S/)</label><input id="precio" name="precio" type="number" min="0" max="999999999.99" step="0.01" required value="<c:out value='${param.precio}'/>"></div>
            <div class="catalogo-actions">
              <button class="btn btn-primary catalogo-submit" type="submit">Guardar producto</button>
            </div>
          </form>
        </section>
      </section>
    </div>
  </main>
</div>
</body>
</html>
