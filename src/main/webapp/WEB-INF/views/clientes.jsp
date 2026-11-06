<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="menu" value="clientes" scope="request"/>
<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Clientes | Ferretería El Tornillo</title>
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
    <div class="topbar"><span class="path">/ventas/clientes</span><div class="user"><span class="role-pill">Rol: <c:out value="${sessionScope.usuario.rol.etiqueta}"/></span><div class="avatar"><c:out value="${sessionScope.usuario.iniciales}"/></div></div></div>
    <div class="content">
      <section class="view active">
        <div class="page-head"><div class="eyebrow">CU-08</div><h2>Gestión de clientes</h2></div>
        <div class="divider"></div>
        <c:if test="${not empty exito}"><p class="catalogo-message catalogo-success" role="status"><c:out value="${exito}"/></p></c:if>
        <c:if test="${not empty error}"><p class="catalogo-message catalogo-error" role="alert"><c:out value="${error}"/></p></c:if>
        <form class="toolbar" method="get" action="${pageContext.request.contextPath}/clientes">
          <label for="q">Buscar</label>
          <input class="search" id="q" name="q" maxlength="120" placeholder="DNI/RUC o nombre" value="<c:out value='${busqueda}'/>">
          <button class="btn btn-ghost" type="submit">Buscar</button>
          <a class="btn btn-ghost" href="${pageContext.request.contextPath}/clientes">Ver todos</a>
        </form>
        <div class="card table-card"><table>
          <thead><tr><th>Documento</th><th>Tipo</th><th>Nombre / Razón social</th><th>Teléfono</th><th>Dirección</th></tr></thead>
          <tbody>
          <c:forEach var="cl" items="${clientes}">
            <tr><td class="mono"><c:out value="${cl.documento}"/></td><td><c:out value="${cl.tipoDocumento}"/></td><td><c:out value="${cl.nombre}"/></td><td><c:out value="${cl.telefono}"/></td><td><c:out value="${cl.direccion}"/></td></tr>
          </c:forEach>
          <c:if test="${empty clientes}"><tr><td colspan="5">No se encontraron clientes.</td></tr></c:if>
          </tbody>
        </table></div>
        <section class="card catalogo-form" aria-labelledby="titulo-cliente">
          <h3 class="section-title" id="titulo-cliente">Registrar o actualizar cliente</h3>
          <form class="catalogo-fields" method="post" action="${pageContext.request.contextPath}/clientes">
            <input type="hidden" name="token" value="<c:out value='${token}'/>">
            <div class="catalogo-field"><label for="documento">DNI (8) o RUC (11)</label><input id="documento" name="documento" inputmode="numeric" pattern="[0-9]{8}|[0-9]{11}" maxlength="11" required value="<c:out value='${param.documento}'/>"></div>
            <div class="catalogo-field"><label for="nombre">Nombre o razón social</label><input id="nombre" name="nombre" maxlength="120" required value="<c:out value='${param.nombre}'/>"></div>
            <div class="catalogo-field"><label for="telefono">Teléfono</label><input id="telefono" name="telefono" maxlength="20" value="<c:out value='${param.telefono}'/>"></div>
            <div class="catalogo-field"><label for="direccion">Dirección</label><input id="direccion" name="direccion" maxlength="160" value="<c:out value='${param.direccion}'/>"></div>
            <div class="catalogo-actions"><button class="btn btn-primary catalogo-submit" type="submit">Guardar cliente</button></div>
          </form>
        </section>
        <p class="stub-note">Si el documento ya existe, sus datos se actualizan.</p>
      </section>
    </div>
  </main>
</div>
</body>
</html>
