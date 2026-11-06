<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="menu" value="kardex" scope="request"/>
<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Kardex y Reportes | Ferretería El Tornillo</title>
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
    <div class="topbar"><span class="path">/almacen/kardex</span><div class="user"><span class="role-pill">Rol: <c:out value="${sessionScope.usuario.rol.etiqueta}"/></span><div class="avatar"><c:out value="${sessionScope.usuario.iniciales}"/></div></div></div>
    <div class="content">
      <section class="view active">
        <div class="page-head"><div class="eyebrow">Pantalla 05</div><h2>Kardex y Reporte de Movimientos</h2></div>
        <div class="divider"></div>
        <c:if test="${not empty error}"><p class="catalogo-message catalogo-error" role="alert"><c:out value="${error}"/></p></c:if>
        <form class="toolbar" method="get" action="${pageContext.request.contextPath}/kardex">
          <label for="q">Filtrar</label>
          <input class="search" id="q" name="q" maxlength="120" placeholder="Producto o SKU" value="<c:out value='${busqueda}'/>">
          <select name="tipo" aria-label="Tipo de movimiento">
            <option value="">Todos</option>
            <option value="Entrada" ${tipoFiltro eq 'Entrada' ? 'selected' : ''}>Entradas</option>
            <option value="Salida" ${tipoFiltro eq 'Salida' ? 'selected' : ''}>Salidas</option>
            <option value="Merma" ${tipoFiltro eq 'Merma' ? 'selected' : ''}>Mermas</option>
          </select>
          <button class="btn btn-ghost" type="submit">Filtrar</button>
          <a class="btn btn-primary" href="${pageContext.request.contextPath}/kardex?action=exportar&amp;q=<c:out value='${busqueda}'/>&amp;tipo=<c:out value='${tipoFiltro}'/>">Exportar a Excel</a>
        </form>
        <div class="card table-card"><table>
          <tr><th>Fecha</th><th>SKU</th><th>Producto</th><th>Tipo</th><th>Cant.</th><th>Saldo</th><th>Usuario</th><th>Detalle</th></tr>
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
              <td><c:out value="${movimiento.usuario}"/></td>
              <td><c:out value="${movimiento.motivo}"/></td>
            </tr>
          </c:forEach>
          <c:if test="${empty movimientos}"><tr><td colspan="8">No hay movimientos con ese filtro.</td></tr></c:if>
        </table></div>
        <p class="stub-note">Se muestran los 500 movimientos más recientes. La exportación a Excel respeta el filtro aplicado.</p>
      </section>
    </div>
  </main>
</div>
</body>
</html>
