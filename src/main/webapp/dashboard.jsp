<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<title>Alternativa 1 - Sistema Web JSP/Servlets | Ferretería</title>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link href="https://fonts.googleapis.com/css2?family=Oswald:wght@500;600;700&family=Inter:wght@400;500;600;700&family=IBM+Plex+Mono:wght@500;600&display=swap" rel="stylesheet">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>
<!-- El contenedor conserva la distribución original de la maqueta. -->
<div class="app">
  <aside class="sidebar">
    <div class="brand">
      <span class="stamp">FERRESYS · JAVA EE</span>
      <h1>Ferretería<br>El Tornillo S.A.C.</h1>
      <p>Alternativa 1 - Web JSP/Servlets</p>
    </div>
    <nav>
      <a class="nav-item active" href="${pageContext.request.contextPath}/dashboard.jsp"><span class="ic">◆</span> Dashboard Principal</a>
      <a class="nav-item" href="${pageContext.request.contextPath}/Pages/catalogo.html"><span class="ic">▤</span> Catálogo e Inventario</a>
      <a class="nav-item" href="${pageContext.request.contextPath}/Pages/ventas.html"><span class="ic">₪</span> Punto de Venta</a>
      <a class="nav-item" href="${pageContext.request.contextPath}/Pages/recepcion.html"><span class="ic">▣</span> Recepción de Mercadería</a>
      <a class="nav-item" href="${pageContext.request.contextPath}/kardex"><span class="ic">≡</span> Kardex y Reportes</a>
    </nav>
    <div class="nav-foot">Almacén · datos en memoria<br>Sesión: almacén-01</div>
  </aside>

  <div class="hazard" style="position:fixed;"></div>
  <main class="main">
    <div class="topbar">
      <span class="path" id="crumb">/dashboard</span>
      <div class="user"><span class="role-pill">Rol: Administrador</span><div class="avatar">MJ</div></div>
    </div>
    <div class="content">
      <section class="view active" id="view-1">
        <div class="page-head"><div class="eyebrow">Pantalla 01</div><h2>Dashboard Principal</h2></div>
        <div class="divider"></div>
        <div class="grid g4" style="margin-bottom:18px;">
          <div class="card kpi"><div class="lbl">Ventas de hoy</div><div class="val">S/ 3,842</div><div class="delta up">▲ 12% vs. ayer</div></div>
          <div class="card kpi"><div class="lbl">Boletas / facturas</div><div class="val">57</div><div class="delta up">▲ 5 más que ayer</div></div>
          <div class="card kpi"><div class="lbl">Productos bajo stock mínimo</div><div class="val" style="color:var(--red)">14</div><div class="delta down">requieren reposición</div></div>
          <div class="card kpi"><div class="lbl">Clientes atendidos</div><div class="val">41</div><div class="delta up">▲ 3 nuevos</div></div>
        </div>
        <div class="grid g2">
          <div class="card">
            <div class="section-title"><span class="n">A</span> Alertas de stock mínimo</div>
            <table>
              <tr><th>Producto</th><th>Categoría</th><th>Stock</th><th>Estado</th></tr>
              <tr><td>Cemento Sol Tipo I x 42.5kg</td><td class="mono">CONSTR</td><td>3</td><td><span class="badge b-low">Crítico</span></td></tr>
              <tr><td>Alambre galvanizado N°16</td><td class="mono">FERRET</td><td>6</td><td><span class="badge b-low">Crítico</span></td></tr>
              <tr><td>Pintura látex blanco 1gal</td><td class="mono">PINTUR</td><td>9</td><td><span class="badge b-mid">Bajo</span></td></tr>
              <tr><td>Tubo PVC 1/2&quot; x 5m</td><td class="mono">GASFIT</td><td>11</td><td><span class="badge b-mid">Bajo</span></td></tr>
            </table>
          </div>
          <div class="card">
            <div class="section-title"><span class="n">B</span> Accesos directos por rol</div>
            <div class="grid g2" style="gap:12px;">
              <a class="btn btn-primary" style="padding:16px;text-align:center;" href="${pageContext.request.contextPath}/Pages/ventas.html">Nueva venta</a>
              <a class="btn btn-ghost" style="padding:16px;text-align:center;" href="${pageContext.request.contextPath}/Pages/recepcion.html">Registrar ingreso</a>
              <a class="btn btn-ghost" style="padding:16px;text-align:center;" href="${pageContext.request.contextPath}/kardex">Ver kardex</a>
              <a class="btn btn-ghost" style="padding:16px;text-align:center;" href="${pageContext.request.contextPath}/Pages/catalogo.html">Gestionar catálogo</a>
            </div>
            <p class="stub-note">Los accesos varían según el rol de sesión (cajero, almacenero, administrador).</p>
          </div>
        </div>
      </section>
    </div>
  </main>
</div>
</body>
</html>
