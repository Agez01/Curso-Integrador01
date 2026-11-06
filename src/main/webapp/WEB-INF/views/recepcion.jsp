<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="menu" value="recepcion" scope="request"/>
<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Recepción de Mercadería | Ferretería El Tornillo</title>
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
    <div class="topbar"><span class="path">/almacen/recepcion</span><div class="user"><span class="role-pill">Rol: <c:out value="${sessionScope.usuario.rol.etiqueta}"/></span><div class="avatar"><c:out value="${sessionScope.usuario.iniciales}"/></div></div></div>
    <div class="content">
      <section class="view active">
        <div class="page-head"><div class="eyebrow">Pantalla 04</div><h2>Recepción de Mercadería (Entrada de Almacén)</h2></div>
        <div class="divider"></div>
        <c:if test="${not empty exito}"><p class="catalogo-message catalogo-success" role="status"><c:out value="${exito}"/></p></c:if>
        <c:if test="${not empty error}"><p class="catalogo-message catalogo-error" role="alert"><c:out value="${error}"/></p></c:if>
        <form class="reception-form" method="post" action="${pageContext.request.contextPath}/entrada" id="form-entrada">
          <input type="hidden" name="token" value="<c:out value='${token}'/>">
          <div class="grid reception-layout">
            <div class="card">
              <div class="section-title"><span class="n">1</span> Datos del proveedor y comprobante</div>
              <div class="reception-header-fields">
                <div class="reception-field"><label for="proveedor">Proveedor</label><input id="proveedor" name="proveedor" maxlength="80" required value="<c:out value='${param.proveedor}'/>"></div>
                <div class="reception-field"><label for="factura">N° factura / guía del proveedor</label><input id="factura" name="factura" maxlength="40" value="<c:out value='${param.factura}'/>"></div>
              </div>
            </div>
            <div class="card">
              <div class="section-title"><span class="n">2</span> Detalle de productos recibidos</div>
              <table id="lineas" class="reception-lines"><thead><tr><th>Producto</th><th>Cantidad</th><th></th></tr></thead><tbody></tbody></table>
              <button class="btn btn-ghost add-line-btn" type="button" id="agregar-linea">+ Agregar línea</button>
            </div>
          </div>
          <button class="btn btn-primary confirm-reception-btn" type="submit">Confirmar y actualizar existencias</button>
        </form>
        <template id="plantilla-linea"><tr>
          <td><select class="reception-control" name="sku" required><option value="">Selecciona…</option><c:forEach var="p" items="${productos}"><option value="<c:out value='${p.sku}'/>"><c:out value="${p.sku}"/> — <c:out value="${p.nombre}"/> (stock <c:out value="${p.stock}"/>)</option></c:forEach></select></td>
          <td><input class="reception-control reception-quantity" name="cantidad" type="number" min="1" max="9999" step="1" required></td>
          <td><button class="btn btn-ghost reception-remove-line" type="button" data-quitar aria-label="Quitar línea">✕</button></td>
        </tr></template>
      </section>
    </div>
  </main>
</div>
<script>
(() => {
  "use strict";
  const cuerpo = document.querySelector("#lineas tbody");
  const plantilla = document.getElementById("plantilla-linea");
  function agregar() {
    const fila = plantilla.content.firstElementChild.cloneNode(true);
    fila.querySelector("[data-quitar]").addEventListener("click", () => {
      if (cuerpo.children.length > 1) fila.remove();
    });
    cuerpo.appendChild(fila);
  }
  document.getElementById("agregar-linea").addEventListener("click", agregar);
  agregar();
})();
</script>
</body>
</html>
