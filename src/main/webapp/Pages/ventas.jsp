<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<title>Punto de Venta | Ferretería El Tornillo</title>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link href="https://fonts.googleapis.com/css2?family=Oswald:wght@500;600;700&family=Inter:wght@400;500;600;700&family=IBM+Plex+Mono:wght@500;600&display=swap" rel="stylesheet">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/pages.css">
</head>
<body>
<!-- .app es el padre general y usa Flexbox para sidebar y main. -->
<div class="app">
  <!-- Sidebar hijo de .app; organiza sus hijos verticalmente con Flexbox. -->
  <aside class="sidebar">
    <div class="brand"><span class="stamp">FERRESYS · JAVA EE</span><h1>Ferretería<br>El Tornillo S.A.C.</h1><p>Alternativa 1 - Web JSP/Servlets</p></div>
    <%@ include file="/WEB-INF/jspf/sidebar-nav.jspf" %>
  </aside>
  <div class="hazard hazard-fixed"></div>
  <!-- main es hijo de .app y usa Flexbox vertical. -->
  <main class="main">
    <div class="topbar"><span class="path">/ventas/pos</span><div class="user"><span class="role-pill">Rol: <c:out value="${sessionScope.usuario.rol.etiqueta}"/></span><div class="avatar"><c:out value="${sessionScope.usuario.iniciales}"/></div></div></div>
    <div class="content">
      <!-- Punto de venta: pos-layout usa CSS Grid para sus dos tarjetas hijas. -->
      <section class="view active">
        <div class="page-head"><div class="eyebrow">Pantalla 03</div><h2>Punto de Venta (Caja / Facturación)</h2></div>
        <div class="divider"></div>
        <div class="pos-layout">
          <div class="card">
            <div class="section-title"><span class="n">1</span> Búsqueda de productos</div>
            <div class="search search-wide search-product">🔍 Escanear código de barras o buscar producto…</div>
            <table><tr><th>Producto</th><th>Precio</th><th></th></tr>
              <tr><td>Martillo carpintero 16oz</td><td>S/ 28.50</td><td><button class="btn btn-primary">Agregar</button></td></tr>
              <tr><td>Tornillos autorroscantes 1&quot;</td><td>S/ 9.50</td><td><button class="btn btn-primary">Agregar</button></td></tr>
              <tr><td>Tubo PVC 1/2&quot; x 5m</td><td>S/ 14.90</td><td><button class="btn btn-primary">Agregar</button></td></tr>
            </table>
          </div>
          <div class="card">
            <div class="section-title"><span class="n">2</span> Carrito de venta</div>
            <div class="cart-row"><span>Martillo carpintero 16oz ×1</span><span>S/ 28.50</span></div>
            <div class="cart-row"><span>Tornillos autorroscantes 1&quot; ×2</span><span>S/ 19.00</span></div>
            <div class="cart-row"><span>Tubo PVC 1/2&quot; x 5m ×3</span><span>S/ 44.70</span></div>
            <div class="cart-total"><span>Total</span><span>S/ 92.20</span></div>
            <div class="checkout-details">
              <div class="section-title section-title-compact"><span class="n">3</span> Cliente y comprobante</div>
              <div class="search search-wide search-customer">👤 DNI / RUC del cliente…</div>
              <!-- Los chips son hijos de este contenedor Flexbox. -->
              <div class="chip-group"><span class="chip on">Boleta</span><span class="chip">Factura</span></div>
              <button class="btn btn-primary checkout-submit">Emitir comprobante</button>
            </div>
          </div>
        </div>
      </section>
    </div>
  </main>
</div>
</body>
</html>
