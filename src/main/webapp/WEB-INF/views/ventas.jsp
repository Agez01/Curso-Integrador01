<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="menu" value="ventas" scope="request"/>
<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Punto de Venta | Ferretería El Tornillo</title>
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
    <div class="topbar"><span class="path">/ventas/pos</span><div class="user"><span class="role-pill">Rol: <c:out value="${sessionScope.usuario.rol.etiqueta}"/></span><div class="avatar"><c:out value="${sessionScope.usuario.iniciales}"/></div></div></div>
    <div class="content">
      <section class="view active">
        <div class="page-head"><div class="eyebrow">Pantalla 03</div><h2>Punto de Venta (Caja / Facturación)</h2></div>
        <div class="divider"></div>
        <c:if test="${not empty exito}"><p class="catalogo-message catalogo-success" role="status"><c:out value="${exito}"/></p></c:if>
        <c:if test="${not empty error}"><p class="catalogo-message catalogo-error" role="alert"><c:out value="${error}"/></p></c:if>
        <div class="pos-layout">
          <div class="card">
            <div class="section-title"><span class="n">1</span> Búsqueda de productos</div>
            <input class="search search-wide search-product" id="filtro-productos" type="search" placeholder="🔍 Buscar producto por nombre…" aria-label="Buscar producto">
            <table id="tabla-productos">
              <tr><th>Producto</th><th>Precio</th><th></th></tr>
              <c:forEach var="p" items="${productos}">
                <tr data-sku="<c:out value='${p.sku}'/>" data-stock="<c:out value='${p.stock}'/>"><td><c:out value="${p.nombre}"/> <small class="mono">(<c:out value="${p.stock}"/> en stock)</small></td><td>S/ <c:out value="${p.precio}"/></td><td><button class="btn btn-primary" type="button">Agregar</button></td></tr>
              </c:forEach>
              <c:if test="${empty productos}"><tr><td colspan="3">No hay productos con stock disponible.</td></tr></c:if>
            </table>
          </div>
          <div class="card">
            <div class="section-title"><span class="n">2</span> Carrito de venta</div>
            <div id="carrito-items" aria-live="polite"><p>Carrito vacío. Agrega un producto para empezar.</p></div>
            <button type="button" id="vaciar-carrito" class="btn">Vaciar carrito</button>
            <div class="cart-total"><span>Total</span><span id="carrito-total">S/ 0.00</span></div>
            <form class="checkout-details" id="form-venta" method="post" action="${pageContext.request.contextPath}/venta">
              <input type="hidden" name="token" value="<c:out value='${token}'/>">
              <input type="hidden" name="tipo" id="campo-tipo" value="Boleta">
              <div id="campos-carrito"></div>
              <div class="section-title section-title-compact"><span class="n">3</span> Cliente y comprobante</div>
              <input class="search search-wide search-customer" id="documento" name="documento" inputmode="numeric" pattern="[0-9]{8}|[0-9]{11}" maxlength="11" placeholder="👤 DNI (8) o RUC (11) del cliente" aria-label="DNI o RUC del cliente" list="lista-clientes">
              <datalist id="lista-clientes"><c:forEach var="cl" items="${clientes}"><option value="<c:out value='${cl.documento}'/>"><c:out value="${cl.nombre}"/></option></c:forEach></datalist>
              <div class="chip-group" id="tipo-comprobante">
                <button type="button" class="chip on" data-tipo="Boleta" aria-pressed="true">Boleta</button>
                <button type="button" class="chip" data-tipo="Factura" aria-pressed="false">Factura</button>
              </div>
              <button class="btn btn-primary checkout-submit" type="submit" id="emitir" disabled>Emitir comprobante</button>
            </form>
          </div>
        </div>
      </section>
    </div>
  </main>
</div>
<script>
(() => {
  "use strict";

  const tablaProductos = document.getElementById("tabla-productos");
  const contenedor = document.getElementById("carrito-items");
  const totalElemento = document.getElementById("carrito-total");
  const botonVaciar = document.getElementById("vaciar-carrito");
  const botonesComprobante = document.querySelectorAll("#tipo-comprobante button");
  const botonEmitir = document.getElementById("emitir");
  const formulario = document.getElementById("form-venta");
  const camposCarrito = document.getElementById("campos-carrito");
  const campoTipo = document.getElementById("campo-tipo");
  const campoDocumento = document.getElementById("documento");
  const filtro = document.getElementById("filtro-productos");
  const carrito = new Map();
  const moneda = new Intl.NumberFormat("es-PE", { style: "currency", currency: "PEN" });
  const LIMITE_GLOBAL = 99;
  let tipoComprobante = "Boleta";

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
      const controles = document.createElement("span");

      const disminuir = document.createElement("button");
      disminuir.type = "button";
      disminuir.className = "btn";
      disminuir.textContent = "−";
      disminuir.setAttribute("aria-label", "Disminuir " + producto.nombre);
      disminuir.addEventListener("click", () => {
        if (producto.cantidad === 1) carrito.delete(clave);
        else producto.cantidad -= 1;
        mostrarCarrito();
      });

      const cantidad = document.createElement("span");
      cantidad.textContent = " " + producto.cantidad + " ";

      const aumentar = document.createElement("button");
      aumentar.type = "button";
      aumentar.className = "btn";
      aumentar.textContent = "+";
      aumentar.setAttribute("aria-label", "Aumentar " + producto.nombre);
      aumentar.disabled = producto.cantidad >= producto.tope;
      aumentar.addEventListener("click", () => {
        if (producto.cantidad >= producto.tope) return;
        producto.cantidad += 1;
        mostrarCarrito();
      });

      controles.append(disminuir, cantidad, aumentar);
      importe.appendChild(controles);
      fila.append(descripcion, importe);
      contenedor.appendChild(fila);
    }

    totalElemento.textContent = moneda.format(totalCentimos / 100);
    botonVaciar.disabled = carrito.size === 0;
    botonEmitir.disabled = carrito.size === 0;
  }


  for (const fila of Array.from(tablaProductos.querySelectorAll("tr[data-sku]"))) {
    const celdas = fila.querySelectorAll("td");
    const boton = fila.querySelector("button");
    const sku = fila.dataset.sku;
    const tope = Math.min(Number(fila.dataset.stock), LIMITE_GLOBAL);
    boton.addEventListener("click", () => {
      const nombre = celdas[0].childNodes[0].textContent.trim();
      const precioCentimos = Math.round(Number(celdas[1].textContent.replace(/[^\d.]/g, "")) * 100);
      if (!Number.isSafeInteger(precioCentimos) || precioCentimos < 0) return;
      if (carrito.has(sku)) {
        const producto = carrito.get(sku);
        if (producto.cantidad >= producto.tope) return;
        producto.cantidad += 1;
      } else {
        carrito.set(sku, { nombre, precioCentimos, cantidad: 1, tope });
      }
      mostrarCarrito();
    });
  }

  filtro.addEventListener("input", () => {
    const texto = filtro.value.trim().toLowerCase();
    for (const fila of tablaProductos.querySelectorAll("tr[data-sku]")) {
      fila.hidden = texto !== "" && !fila.textContent.toLowerCase().includes(texto);
    }
  });

  // El servidor recalcula precios y total desde la base de datos: solo se envían SKU y cantidades.
  formulario.addEventListener("submit", evento => {
    if (carrito.size === 0) { evento.preventDefault(); return; }
    campoTipo.value = tipoComprobante;
    if (tipoComprobante === "Factura" && !/^\d{11}$/.test(campoDocumento.value)) {
      evento.preventDefault();
      alert("La factura requiere el RUC del cliente (11 dígitos).");
      return;
    }
    camposCarrito.replaceChildren();
    for (const [sku, producto] of carrito) {
      for (const [nombre, valor] of [["sku", sku], ["cantidad", String(producto.cantidad)]]) {
        const campo = document.createElement("input");
        campo.type = "hidden";
        campo.name = nombre;
        campo.value = valor;
        camposCarrito.appendChild(campo);
      }
    }
  });

  botonVaciar.addEventListener("click", () => {
    if (carrito.size === 0) return;
    if (confirm("¿Estás seguro de que deseas vaciar el carrito?")) {
      carrito.clear();
      mostrarCarrito();
    }
  });

  botonesComprobante.forEach(boton => {
    boton.addEventListener("click", () => {
      tipoComprobante = boton.dataset.tipo;
      botonesComprobante.forEach(opcion => {
        const seleccionado = opcion.dataset.tipo === tipoComprobante;
        opcion.classList.toggle("on", seleccionado);
        opcion.setAttribute("aria-pressed", String(seleccionado));
      });
    });
  });

  mostrarCarrito();
})();
</script>
</body>
</html>
