import re
import urllib.request as u
import urllib.parse as p
from urllib.error import HTTPError
from http.cookiejar import CookieJar

base = 'http://127.0.0.1:18090/ferresys'
def browser():
    return u.build_opener(u.HTTPCookieProcessor(CookieJar()), u.ProxyHandler({}))
def call(b, path, data=None):
    req = u.Request(base + path, None if data is None else p.urlencode(data).encode())
    try:
        with b.open(req, timeout=15) as r:
            return r.status, r.read().decode('utf-8'), r.url
    except HTTPError as e:
        return e.code, e.read().decode('utf-8'), e.url

def login(name, password):
    b = browser()
    status, html, url = call(b, '/login', {'usuario': name, 'clave': password})
    assert status == 200, (status, html)
    return b

b = browser()
assert call(b, '/productos')[2].endswith('/login')
b = login('admin', 'admin123')
status, html, _ = call(b, '/Pages/catalogo.jsp')
assert status == 200, html
assert 'FR-1042' in html and 'Registrar producto' in html
assert 'nav-item active' in html
m = re.search(r'name="token" value="([^"]+)"', html)
assert m, html
data = dict(sku='HTTP-001', nombre='Producto <prueba>', categoria='Ferretería', unidad='Caja', precio='15.50', token=m[1])
status, html, _ = call(b, '/productos', data)
assert status == 200 and 'Producto registrado correctamente.' in html, html
assert 'Producto &lt;prueba&gt;' in html and 'S/ 15.50' in html
status, html, _ = call(b, '/productos', data)
assert status == 400 and 'Ya existe un producto con ese SKU.' in html, html
assert 'value="HTTP-001"' in html
for key, value in [('precio', '-1'), ('precio', '1.001'), ('categoria', 'Otra'), ('nombre', ' '), ('unidad', ' ')]:
    invalid = dict(data, sku='INVALIDO')
    invalid[key] = value
    status, html, _ = call(b, '/productos', invalid)
    assert status == 400, (key, status, html)
assert call(b, '/productos', dict(data, token='incorrecto'))[0] == 403
status, html, _ = call(b, '/productos?q=http-001')
assert status == 200 and 'HTTP-001' in html and 'FR-1042' not in html
assert 'No se encontraron productos.' in call(b, '/productos?q=INEXISTENTE')[1]
assert call(b, '/WEB-INF/views/catalogo.jsp')[0] == 404
c = login('cajero', 'cajero123')
assert call(c, '/productos')[0] == 403
assert call(c, '/Pages/catalogo.jsp')[0] == 403
assert call(c, '/productos', data)[0] == 403
a = login('almacen', 'almacen123')
status, html, _ = call(a, '/productos?q=http-001')
assert status == 200 and 'HTTP-001' in html
assert 'HTTP-001' not in call(a, '/productos?q=INVALIDO')[1]
print('OK: JSP, registro, validaciones, duplicados, busqueda, escape HTML, acceso por roles y datos compartidos.')
