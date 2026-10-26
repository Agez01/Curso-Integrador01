<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<title>Kardex y Reportes | Ferretería El Tornillo</title>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link href="https://fonts.googleapis.com/css2?family=Oswald:wght@500;600;700&family=Inter:wght@400;500;600;700&family=IBM+Plex+Mono:wght@500;600&display=swap" rel="stylesheet">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/pages.css">
</head>
<body>
<!-- La vista presenta los movimientos preparados por KardexServlet. -->
<div class="app">
  <aside class="sidebar">
    <div class="brand"><span class="stamp">FERRESYS · JAVA EE</span><h1>Ferretería<br>El Tornillo S.A.C.</h1><p>Alternativa 1 - Web JSP/Servlets</p></div>
    <%@ include file="/WEB-INF/jspf/sidebar-nav.jspf" %>
  </aside>
  <div class="hazard hazard-fixed"></div>
  <main class="main">
    <div class="topbar"><span class="path">/almacen/kardex</span><div class="user"><span class="role-pill">Rol: <c:out value="${sessionScope.usuario.rol.etiqueta}"/></span><div class="avatar"><c:out value="${sessionScope.usuario.iniciales}"/></div></div></div>
    <div class="content">
      <section class="view active">
        <div class="page-head"><div class="eyebrow">Pantalla 05</div><h2>Kardex y Reporte de Movimientos</h2></div>
        <div class="divider"></div>
        <div class="toolbar">
          <div class="search">🔍 Filtrar por producto o SKU…</div>
          <div class="chip-group"><span class="chip on">Todos</span><span class="chip">Entradas</span><span class="chip">Salidas</span><span class="chip">Mermas</span></div>
          <a class="btn btn-primary" href="${pageContext.request.contextPath}/kardex?action=exportar">Exportar a Excel</a>
        </div>
        <div class="card table-card"><table>
          <tr><th>Fecha</th><th>SKU</th><th>Producto</th><th>Tipo</th><th>Cant.</th><th>Saldo</th></tr>
          <c:forEach var="movimiento" items="${movimientos}">
            <tr>
              <td class="mono"><c:out value="${movimiento.fechaTexto}"/></td>
              <td class="mono"><c:out value="${movimiento.sku}"/></td>
              <td><c:out value="${movimiento.producto}"/></td>
              <td>
                <c:choose>
                  <c:when test="${movimiento.tipo eq 'Entrada'}"><span class="badge b-ok">Entrada</span></c:when>
                  <c:when test="${movimiento.tipo eq 'Merma'}"><span class="badge b-mid">Merma</span></c:when>
                  <c:otherwise><span class="badge b-low">Salida</span></c:otherwise>
                </c:choose>
              </td>
              <td><c:choose><c:when test="${movimiento.tipo eq 'Entrada'}">+</c:when><c:otherwise>-</c:otherwise></c:choose><c:out value="${movimiento.cantidad}"/></td>
              <td><c:out value="${movimiento.stockResultante}"/></td>
            </tr>
          </c:forEach>
        </table></div>
        <p class="stub-note">Reporte exportable a Excel, con trazabilidad de movimientos y existencias.</p>
      </section>
    </div>
  </main>
</div>
</body>
</html>
