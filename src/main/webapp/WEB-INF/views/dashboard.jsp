<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="menu" value="dashboard" scope="request"/>
<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Dashboard Principal | Ferretería El Tornillo</title>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link href="https://fonts.googleapis.com/css2?family=Oswald:wght@500;600;700&amp;family=Inter:wght@400;500;600;700&amp;family=IBM+Plex+Mono:wght@500;600&amp;display=swap" rel="stylesheet">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/pages.css">
</head>
<body>
<div class="app">
  <aside class="sidebar">
    <div class="brand"><span class="stamp">FERRESYS · JAVA EE</span><h1>Ferretería<br>El Tornillo S.A.C.</h1><p>Alternativa 1 - Web JSP/Servlets</p></div>
    <%@ include file="/WEB-INF/jspf/sidebar-nav.jspf" %>
  </aside>
  <div class="hazard hazard-fixed"></div>
  <main class="main">
    <div class="topbar"><span class="path">/dashboard</span><div class="user"><span class="role-pill">Rol: <c:out value="${sessionScope.usuario.rol.etiqueta}"/></span><div class="avatar"><c:out value="${sessionScope.usuario.iniciales}"/></div></div></div>
    <div class="content">
      <section class="view active">
        <div class="page-head"><div class="eyebrow">Pantalla 01</div><h2>Dashboard Principal</h2></div>
        <div class="divider"></div>
        <c:if test="${not empty error}"><p class="catalogo-message catalogo-error" role="alert"><c:out value="${error}"/></p></c:if>
        <c:if test="${not empty resumen}">
        <div class="grid g4 dashboard-kpis">
          <div class="card kpi"><div class="lbl">Ventas de hoy</div><div class="val">S/ <c:out value="${resumen.ventasHoy}"/></div><div class="delta up">total facturado hoy</div></div>
          <div class="card kpi"><div class="lbl">Boletas / facturas</div><div class="val"><c:out value="${resumen.comprobantesHoy}"/></div><div class="delta up">emitidas hoy</div></div>
          <div class="card kpi"><div class="lbl">Productos bajo stock mínimo</div><div class="val text-danger"><c:out value="${resumen.productosBajoStock}"/></div><div class="delta down">requieren reposición</div></div>
          <div class="card kpi"><div class="lbl">Movimientos de kardex</div><div class="val"><c:out value="${resumen.movimientosHoy}"/></div><div class="delta up">registrados hoy</div></div>
        </div>
        </c:if>
        <div class="grid g2">
          <div class="card">
            <div class="section-title"><span class="n">A</span> Alertas de stock mínimo</div>
            <table>
              <tr><th>Producto</th><th>Categoría</th><th>Stock</th><th>Estado</th></tr>
              <c:forEach var="p" items="${resumen.alertas}">
                <tr><td><c:out value="${p.nombre}"/></td><td class="mono"><c:out value="${p.categoria}"/></td><td><c:out value="${p.stock}"/> / <c:out value="${p.stockMinimo}"/></td>
                  <td><c:choose><c:when test="${p.estadoStock eq 'Crítico'}"><span class="badge b-low">Crítico</span></c:when><c:otherwise><span class="badge b-mid">Bajo</span></c:otherwise></c:choose></td></tr>
              </c:forEach>
              <c:if test="${not empty resumen and empty resumen.alertas}"><tr><td colspan="4">Todos los productos están sobre su stock mínimo.</td></tr></c:if>
            </table>
          </div>
          <div class="card">
            <div class="section-title"><span class="n">B</span> Accesos directos por rol</div>
            <div class="grid g2 dashboard-shortcuts">
              <c:if test="${sessionScope.usuario.rol.etiqueta eq 'Cajero'}">
                <a class="btn btn-primary shortcut-btn" href="${pageContext.request.contextPath}/venta">Nueva venta</a>
              </c:if>
              <c:if test="${sessionScope.usuario.rol.etiqueta eq 'Almacenero'}">
                <a class="btn btn-primary shortcut-btn" href="${pageContext.request.contextPath}/entrada">Registrar ingreso</a>
              </c:if>
              <c:if test="${sessionScope.usuario.rol.etiqueta eq 'Administrador' or sessionScope.usuario.rol.etiqueta eq 'Almacenero'}">
                <a class="btn btn-ghost shortcut-btn" href="${pageContext.request.contextPath}/ajuste">Ajustar existencias</a>
                <a class="btn btn-ghost shortcut-btn" href="${pageContext.request.contextPath}/kardex">Ver kardex</a>
                <a class="btn btn-ghost shortcut-btn" href="${pageContext.request.contextPath}/productos">Gestionar catálogo</a>
              </c:if>
            </div>
            <p class="stub-note">Se muestran los accesos disponibles para tu rol. Indicadores calculados desde PostgreSQL.</p>
          </div>
        </div>
      </section>
    </div>
  </main>
</div>
</body>
</html>
