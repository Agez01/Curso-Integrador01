<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Iniciar sesión | Ferretería El Tornillo</title>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link href="https://fonts.googleapis.com/css2?family=Oswald:wght@500;600;700&family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/login.css">
</head>
<body>
<div class="login-wrap">
  <form class="card login-card" method="post" action="${pageContext.request.contextPath}/login" autocomplete="off">
    <div class="eyebrow">Ferretería El Tornillo S.A.C.</div>
    <h2>Iniciar sesión</h2>
    <div class="divider"></div>
    <c:if test="${not empty error}">
      <div class="login-error" role="alert"><c:out value="${error}"/></div>
    </c:if>
    <label for="usuario">Usuario</label>
    <input id="usuario" name="usuario" type="text" maxlength="30" required autofocus>
    <label for="clave">Clave</label>
    <input id="clave" name="clave" type="password" maxlength="64" required>
    <button type="submit" class="btn btn-primary">Ingresar</button>
  </form>
</div>
</body>
</html>
