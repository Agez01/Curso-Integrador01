-- Esquema de Ferresys. Ejecutar conectado a la base ferresys:
--   psql -U ferresys -d ferresys -f db/02_esquema.sql
-- Es idempotente: se puede volver a ejecutar para reiniciar la base (borra los datos).
DROP TABLE IF EXISTS cliente, movimiento_kardex, venta_detalle, venta, producto, usuario CASCADE;
DROP SEQUENCE IF EXISTS seq_boleta, seq_factura;

CREATE TABLE usuario (
    id         SERIAL PRIMARY KEY,
    nombre     VARCHAR(30)  NOT NULL UNIQUE,
    clave_hash VARCHAR(200) NOT NULL,
    rol        VARCHAR(20)  NOT NULL CHECK (rol IN ('ADMINISTRADOR', 'CAJERO', 'ALMACENERO')),
    activo     BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE cliente (
    documento VARCHAR(11)  PRIMARY KEY CHECK (documento ~ '^([0-9]{8}|[0-9]{11})$'),  -- DNI o RUC
    nombre    VARCHAR(120) NOT NULL,
    telefono  VARCHAR(20),
    direccion VARCHAR(160)
);

CREATE TABLE producto (
    sku          VARCHAR(30)   PRIMARY KEY CHECK (sku ~ '^[A-Z0-9][A-Z0-9_-]{0,29}$'),
    nombre       VARCHAR(120)  NOT NULL,
    categoria    VARCHAR(30)   NOT NULL CHECK (categoria IN ('Ferretería', 'Construcción', 'Gasfitería', 'Pinturas')),
    unidad       VARCHAR(30)   NOT NULL,
    precio       NUMERIC(11,2) NOT NULL CHECK (precio >= 0),
    stock        INTEGER       NOT NULL DEFAULT 0 CHECK (stock >= 0),
    stock_minimo INTEGER       NOT NULL DEFAULT 5 CHECK (stock_minimo >= 0)
);

CREATE SEQUENCE seq_boleta;
CREATE SEQUENCE seq_factura;

CREATE TABLE venta (
    id                 BIGSERIAL PRIMARY KEY,
    fecha              TIMESTAMPTZ   NOT NULL DEFAULT now(),
    usuario            VARCHAR(30)   NOT NULL REFERENCES usuario(nombre),
    tipo_comprobante   VARCHAR(10)   NOT NULL CHECK (tipo_comprobante IN ('Boleta', 'Factura')),
    numero_comprobante VARCHAR(20)   NOT NULL UNIQUE,
    documento_cliente  VARCHAR(11),
    total              NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (total >= 0),
    CHECK (documento_cliente IS NULL OR documento_cliente ~ '^([0-9]{8}|[0-9]{11})$'),
    CHECK (tipo_comprobante = 'Boleta' OR documento_cliente ~ '^[0-9]{11}$')   -- la factura exige RUC
);

CREATE TABLE venta_detalle (
    id              BIGSERIAL PRIMARY KEY,
    venta_id        BIGINT        NOT NULL REFERENCES venta(id) ON DELETE CASCADE,
    sku             VARCHAR(30)   NOT NULL REFERENCES producto(sku),
    cantidad        INTEGER       NOT NULL CHECK (cantidad > 0),
    precio_unitario NUMERIC(11,2) NOT NULL CHECK (precio_unitario >= 0),
    subtotal        NUMERIC(12,2) NOT NULL CHECK (subtotal >= 0),
    UNIQUE (venta_id, sku)
);

CREATE TABLE movimiento_kardex (
    id               BIGSERIAL PRIMARY KEY,
    fecha            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    sku              VARCHAR(30)  NOT NULL REFERENCES producto(sku),
    tipo             VARCHAR(10)  NOT NULL CHECK (tipo IN ('Entrada', 'Salida', 'Merma')),
    cantidad         INTEGER      NOT NULL CHECK (cantidad > 0),
    stock_resultante INTEGER      NOT NULL CHECK (stock_resultante >= 0),
    usuario          VARCHAR(30)  NOT NULL REFERENCES usuario(nombre),
    motivo           VARCHAR(200),
    venta_id         BIGINT       REFERENCES venta(id)
);
CREATE INDEX idx_kardex_fecha ON movimiento_kardex (fecha DESC, id DESC);
CREATE INDEX idx_kardex_sku   ON movimiento_kardex (sku);
CREATE INDEX idx_venta_fecha  ON venta (fecha);
