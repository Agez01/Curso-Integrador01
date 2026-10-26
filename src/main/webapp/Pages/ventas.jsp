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
            <!-- El carrito se llena dinámicamente al pulsar Agregar. -->
            <div id="carrito-items" aria-live="polite"><p>Carrito vacío. Agrega un producto para empezar.</p></div>
            <div class="cart-total"><span>Total</span><span id="carrito-total">S/ 0.00</span></div>
            <div class="checkout-details">
              <div class="section-title section-title-compact"><span class="n">3</span> Cliente y comprobante</div>
              <div class="search search-wide search-customer">👤 DNI / RUC del cliente…</div>
              <!-- Los chips son hijos de este contenedor Flexbox. -->
              <div class="chip-group"><span class="chip on">Boleta</span><span class="chip">Factura</span></div>
              <!-- Se habilitará cuando el servlet registre ventas reales. -->
              <button class="btn btn-primary checkout-submit" type="button" disabled title="Pendiente: conectar la venta con el servidor">Emitir comprobante (próximamente)</button>
            </div>
          </div>
        </div>
      </section>
    </div>
  </main>
</div>
<script>
// Etapa 1: carrito dinámico en la pantalla. NO registra ventas ni modifica el Kardex.
(() => {
  "use strict";
  const tablaProductos = document.querySelector(".pos-layout .card:first-child table");
  const contenedor = document.getElementById("carrito-items");
  const totalElemento = document.getElementById("carrito-total");
  const carrito = new Map();
  const moneda = new Intl.NumberFormat("es-PE", { style: "currency", currency: "PEN" });

  function mostrarCarrito() {
    contenedor.replaceChildren();
    let totalCentimos = 0;
    if (carrito.size === 0) {
      const aviso = document.createElement("p");
      aviso.textContent = "Carrito vacío. Agrega un producto para empezar.";
      contenedor.appendChild(aviso);
    }
    for (const [clave, producto] of carrito) {
      const subtotalCentimos = producto.precioCentimos * producto.cantidad;
      totalCentimos += subtotalCentimos;
      const fila = document.createElement("div");
      fila.className = "cart-row";
      const descripcion = document.createElement("span");
      descripcion.textContent = producto.nombre + " ×" + producto.cantidad;
      const importe = document.createElement("span");
      importe.textContent = moneda.format(subtotalCentimos / 100) + " ";
      const quitar = document.createElement("button");
      quitar.type = "button";
      quitar.className = "btn";
      quitar.textContent = "Quitar";
      quitar.setAttribute("aria-label", "Quitar una unidad de " + producto.nombre);
      quitar.addEventListener("click", () => {
        if (producto.cantidad === 1) carrito.delete(clave);
        else producto.cantidad -= 1;
        mostrarCarrito();
      });
      importe.appendChild(quitar);
      fila.append(descripcion, importe);
      contenedor.appendChild(fila);
    }
    totalElemento.textContent = moneda.format(totalCentimos / 100);
  }

  for (const [indice, fila] of Array.from(tablaProductos.querySelectorAll("tr")).entries()) {
    const celdas = fila.querySelectorAll("td");
    const boton = fila.querySelector("button");
    if (celdas.length < 2 || !boton) continue;
    boton.type = "button";
    boton.addEventListener("click", () => {
      const nombre = celdas[0].textContent.trim();
      const precioTexto = celdas[1].textContent.replace(/[^\d.]/g, "");
      const precioCentimos = Math.round(Number(precioTexto) * 100);
      if (!Number.isSafeInteger(precioCentimos) || precioCentimos < 0) return;
      if (carrito.has(indice)) carrito.get(indice).cantidad += 1;
      else carrito.set(indice, { nombre, precioCentimos, cantidad: 1 });
      mostrarCarrito();
    });
  }
  mostrarCarrito();
})();
</script>
</body>
</html>