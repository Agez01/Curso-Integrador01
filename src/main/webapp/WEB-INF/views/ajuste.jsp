<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="menu" value="ajuste" scope="request"/>
<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Ajuste de existencias | Ferretería El Tornillo</title>
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
    <div class="topbar"><span class="path">/almacen/ajuste</span><div class="user"><span class="role-pill">Rol: <c:out value="${sessionScope.usuario.rol.etiqueta}"/></span><div class="avatar"><c:out value="${sessionScope.usuario.iniciales}"/></div></div></div>
    <div class="content">
      <section class="view active">
        <div class="page-head"><div class="eyebrow">CU-05</div><h2>Ajuste de existencias (merma)</h2></div>
        <div class="divider"></div>
        <c:if test="${not empty exito}"><p class="catalogo-message catalogo-success" role="status"><c:out value="${exito}"/></p></c:if>
        <c:if test="${not empty error}"><p class="catalogo-message catalogo-error" role="alert"><c:out value="${error}"/></p></c:if>
        <section class="card catalogo-form">
          <form class="catalogo-fields" method="post" action="${pageContext.request.contextPath}/ajuste">
            <input type="hidden" name="token" value="<c:out value='${token}'/>">
            <div class="catalogo-field"><label for="sku">Producto</label><select id="sku" name="sku" required><option value="">Selecciona…</option><c:forEach var="p" items="${productos}"><option value="<c:out value='${p.sku}'/>"><c:out value="${p.sku}"/> — <c:out value="${p.nombre}"/> (stock <c:out value="${p.stock}"/>)</option></c:forEach></select></div>
            <div class="catalogo-field"><label for="cantidad">Cantidad a descontar</label><input id="cantidad" name="cantidad" type="number" min="1" max="9999" step="1" required value="<c:out value='${param.cantidad}'/>"></div>
            <div class="catalogo-field"><label for="motivo">Motivo (obligatorio)</label><input id="motivo" name="motivo" maxlength="200" minlength="5" required placeholder="Producto dañado, vencido, pérdida…" value="<c:out value='${param.motivo}'/>"></div>
            <div class="catalogo-actions"><button class="btn btn-primary catalogo-submit" type="submit">Registrar merma</button></div>
          </form>
        </section>
        <p class="stub-note">La merma descuenta existencias y queda registrada en el kardex con su motivo y tu usuario.</p>
      </section>
    </div>
  </main>
</div>
</body>
</html>
