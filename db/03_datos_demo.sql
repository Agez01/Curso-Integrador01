-- Datos de demostración:  psql -U ferresys -d ferresys -f db/03_datos_demo.sql
-- Usuarios (clave en texto entre paréntesis; en la tabla solo se guarda el hash PBKDF2):
--   admin (admin123), cajero (cajero123), almacen (almacen123)
INSERT INTO usuario (nombre, clave_hash, rol) VALUES
 ('admin',   '120000:NexdckkJg7im2uW/Bovd1Q==:aKrScA2k75od4KnqOcWkmLcLpbzpKfDtcBb/wTH6umE=', 'ADMINISTRADOR'),
 ('cajero',  '120000:2Ji+v2V1RzTObhqdcevBZA==:bBb35vwkeC6n1+8HvCteHNJv0O1mxYS6Ip6PKHRMAp0=', 'CAJERO'),
 ('almacen', '120000:DOABRnxq+C2d5qm6VsxheQ==:LU1Ehk04L9zDK/OlIXd/125GR79aIzNjWMFwPgoN9Yw=', 'ALMACENERO');

INSERT INTO producto (sku, nombre, categoria, unidad, precio, stock, stock_minimo) VALUES
 ('FR-1042', 'Martillo carpintero 16oz',          'Ferretería',   'Unidad', 28.50, 25, 8),
 ('CN-0087', 'Cemento Sol Tipo I x 42.5kg',       'Construcción', 'Bolsa',  31.00,  3, 20),
 ('GF-0215', 'Tubo PVC 1/2" x 5m',                'Gasfitería',   'Unidad', 14.90, 11, 15),
 ('PT-0330', 'Pintura látex blanco 1gal',         'Pinturas',     'Galón',  46.00,  9, 12),
 ('FR-1108', 'Tornillos autorroscantes 1" (caja x100)', 'Ferretería', 'Caja', 9.50, 58, 20),
 ('FR-1201', 'Alambre galvanizado N°16',          'Ferretería',   'Rollo',  18.00,  6, 10),
 ('CN-0102', 'Ladrillo King Kong 18 huecos',      'Construcción', 'Unidad',  1.20, 900, 300),
 ('GF-0310', 'Codo PVC 1/2" (unidad)',            'Gasfitería',   'Unidad',  1.50, 120, 40),
 ('PT-0415', 'Thinner acrílico 1gal',             'Pinturas',     'Galón',  38.00, 14, 6);

-- Inventario inicial reflejado en el kardex
INSERT INTO movimiento_kardex (fecha, sku, tipo, cantidad, stock_resultante, usuario, motivo)
SELECT now() - interval '2 days', sku, 'Entrada', stock, stock, 'admin', 'Inventario inicial'
FROM producto WHERE stock > 0;

INSERT INTO cliente (documento, nombre, telefono, direccion) VALUES
 ('45871236',   'Carlos Quispe Mamani',          '987654321', 'Av. Los Olivos 123, Lima'),
 ('20512345678','Constructora Andina S.A.C.',    '014445566', 'Jr. Cusco 450, Lima');
