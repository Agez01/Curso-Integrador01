<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<title>Recepción de Mercadería | Ferretería El Tornillo</title>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link href="https://fonts.googleapis.com/css2?family=Oswald:wght@500;600;700&family=Inter:wght@400;500;600;700&family=IBM+Plex+Mono:wght@500;600&display=swap" rel="stylesheet">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/pages.css">
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
    <div class="topbar"><span class="path">/almacen/recepcion</span><div class="user"><span class="role-pill">Rol: <c:out value="${sessionScope.usuario.rol.etiqueta}"/></span><div class="avatar"><c:out value="${sessionScope.usuario.iniciales}"/></div></div></div>
    <div class="content">
      <!-- Recepción: este grid usa CSS Grid para sus dos tarjetas hijas. -->
      <section class="view active">
        <div class="page-head"><div class="eyebrow">Pantalla 04</div><h2>Recepción de Mercadería (Entrada de Almacén)</h2></div>
        <div class="divider"></div>
        <div class="grid g2">
          <div class="card">
            <div class="section-title"><span class="n">1</span> Datos del proveedor y comprobante</div>
            <!-- grid interno usa CSS Grid; cada div de datos es un hijo. -->
            <div class="grid g2 reception-fields">
              <div><label class="reception-label">Proveedor</label><div class="search search-wide">Inversiones Ferroaceros S.A.C.</div></div>
              <div><label class="reception-label">N° factura proveedor</label><div class="search search-wide mono">F001-008842</div></div>
              <div><label class="reception-label">Fecha de recepción</label><div class="search search-wide">25/08/2026</div></div>
              <div><label class="reception-label">Almacén destino</label><div class="search search-wide">Almacén principal</div></div>
            </div>
          </div>
          <div class="card">
            <div class="section-title"><span class="n">2</span> Detalle de productos recibidos</div>
            <table><tr><th>SKU</th><th>Producto</th><th>Cant.</th><th>Costo unit.</th></tr>
              <tr><td class="mono">CN-0087</td><td>Cemento Sol Tipo I 42.5kg</td><td>80</td><td>S/ 24.10</td></tr>
              <tr><td class="mono">FR-1042</td><td>Martillo carpintero 16oz</td><td>20</td><td>S/ 18.00</td></tr>
            </table>
            <button class="btn btn-ghost add-line-btn">+ Agregar línea</button>
          </div>
        </div>
        <button class="btn btn-primary confirm-reception-btn">Confirmar y actualizar existencias</button>
      </section>
    </div>
  </main>
</div>
</body>
</html>
