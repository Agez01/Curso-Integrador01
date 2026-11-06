<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="menu" value="usuarios" scope="request"/>
<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Usuarios y roles | Ferretería El Tornillo</title>
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
    <div class="topbar"><span class="path">/admin/usuarios</span><div class="user"><span class="role-pill">Rol: <c:out value="${sessionScope.usuario.rol.etiqueta}"/></span><div class="avatar"><c:out value="${sessionScope.usuario.iniciales}"/></div></div></div>
    <div class="content">
      <section class="view active">
        <div class="page-head"><div class="eyebrow">CU-02</div><h2>Gestión de usuarios y roles</h2></div>
        <div class="divider"></div>
        <c:if test="${not empty exito}"><p class="catalogo-message catalogo-success" role="status"><c:out value="${exito}"/></p></c:if>
        <c:if test="${not empty error}"><p class="catalogo-message catalogo-error" role="alert"><c:out value="${error}"/></p></c:if>
        <div class="card table-card"><table>
          <thead><tr><th>Usuario</th><th>Rol</th><th>Estado</th><th></th></tr></thead>
          <tbody>
          <c:forEach var="u" items="${usuarios}">
            <tr><td class="mono"><c:out value="${u.nombre}"/></td><td><c:out value="${u.rol.etiqueta}"/></td>
              <td><c:choose><c:when test="${u.activo}"><span class="badge b-ok">Activo</span></c:when><c:otherwise><span class="badge b-low">Bloqueado</span></c:otherwise></c:choose></td>
              <td>
                <c:if test="${u.nombre ne sessionScope.usuario.nombre}">
                <form method="post" action="${pageContext.request.contextPath}/usuarios" style="display:inline">
                  <input type="hidden" name="token" value="<c:out value='${token}'/>">
                  <input type="hidden" name="nombre" value="<c:out value='${u.nombre}'/>">
                  <input type="hidden" name="accion" value="${u.activo ? 'bloquear' : 'activar'}">
                  <button class="btn btn-ghost" type="submit">${u.activo ? 'Bloquear' : 'Activar'}</button>
                </form>
                </c:if>
              </td></tr>
          </c:forEach>
          </tbody>
        </table></div>
        <section class="card catalogo-form" aria-labelledby="titulo-usuario">
          <h3 class="section-title" id="titulo-usuario">Crear usuario</h3>
          <form class="catalogo-fields" method="post" action="${pageContext.request.contextPath}/usuarios" autocomplete="off">
            <input type="hidden" name="token" value="<c:out value='${token}'/>">
            <input type="hidden" name="accion" value="crear">
            <div class="catalogo-field"><label for="nombre">Usuario</label><input id="nombre" name="nombre" maxlength="30" minlength="3" pattern="[A-Za-z0-9._-]{3,30}" required value="<c:out value='${param.nombre}'/>"></div>
            <div class="catalogo-field"><label for="clave">Clave (mínimo 8 caracteres)</label><input id="clave" name="clave" type="password" minlength="8" maxlength="72" required autocomplete="new-password"></div>
            <div class="catalogo-field"><label for="rol">Rol</label><select id="rol" name="rol" required>
              <option value="">Selecciona un rol</option>
              <c:forEach var="r" items="${roles}"><option value="<c:out value='${r.name()}'/>"><c:out value="${r.etiqueta}"/></option></c:forEach>
            </select></div>
            <div class="catalogo-actions"><button class="btn btn-primary catalogo-submit" type="submit">Crear usuario</button></div>
          </form>
        </section>
        <p class="stub-note">Un usuario bloqueado no puede iniciar sesión. No puedes bloquear tu propio usuario.</p>
      </section>
    </div>
  </main>
</div>
</body>
</html>
