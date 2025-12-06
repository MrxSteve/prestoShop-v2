CREATE TYPE estado_abono AS ENUM ('APLICADO', 'PENDIENTE', 'RECHAZADO');
CREATE TYPE estado_venta AS ENUM ('PENDIENTE', 'PAGADA', 'PARCIAL', 'CANCELADA');
CREATE TYPE metodo_pago AS ENUM ('EFECTIVO', 'TRANSFERENCIA', 'TARJETA', 'OTRO');
CREATE TYPE tipo_notificacion AS ENUM ('CARGO', 'ABONO', 'VENCIMIENTO', 'GENERAL');
CREATE TYPE tipo_referencia AS ENUM ('VENTA', 'ABONO', 'AJUSTE');
CREATE TYPE tipo_venta AS ENUM ('CREDITO', 'CONTADO');
CREATE TYPE tipo_evento AS ENUM ('VENTA_REGISTRADA', 'ABONO_REGISTRADO', 'CLIENTE_CREADO', 'CUENTA_CREADA');

-- Ubicaciones
CREATE TABLE departamentos (
                               id SERIAL PRIMARY KEY,
                               nombre VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE municipios (
                            id SERIAL PRIMARY KEY,
                            departamento_id INT NOT NULL,
                            nombre VARCHAR(150) NOT NULL,
                            UNIQUE(departamento_id, nombre),
                            FOREIGN KEY (departamento_id) REFERENCES departamentos(id)
);

-- Usuarios, roles y tiendas
CREATE TABLE usuarios (
                          id BIGSERIAL PRIMARY KEY,
                          nombre_completo VARCHAR(255) NOT NULL,
                          email VARCHAR(255) UNIQUE NOT NULL,
                          password VARCHAR(255) NOT NULL,
                          telefono VARCHAR(20),
                          avatar_url TEXT,
                          dui VARCHAR(50) UNIQUE,
                          activo BOOLEAN DEFAULT true,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tiendas (
                         id BIGSERIAL PRIMARY KEY,
                         nombre VARCHAR(100) NOT NULL,
                         telefono VARCHAR(20),
                         logo_url TEXT,
                         activo BOOLEAN DEFAULT true,
                         municipio_id INT NOT NULL,
                         direccion_exacta TEXT,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         FOREIGN KEY (municipio_id) REFERENCES municipios(id)
);

CREATE TABLE roles (
                       id BIGSERIAL PRIMARY KEY,
                       nombre VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE usuarios_roles (
                                usuario_id BIGINT NOT NULL,
                                rol_id BIGINT NOT NULL,
                                PRIMARY KEY (usuario_id, rol_id),
                                FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
                                FOREIGN KEY (rol_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE TABLE empleados_tienda (
                                  id BIGSERIAL PRIMARY KEY,
                                  usuario_id BIGINT NOT NULL,
                                  tienda_id BIGINT NOT NULL,
                                  activo BOOLEAN DEFAULT true,
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                  UNIQUE(usuario_id, tienda_id),

                                  FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
                                  FOREIGN KEY (tienda_id) REFERENCES tiendas(id) ON DELETE CASCADE
);

-- Catalogo
CREATE TABLE categorias (
                            id BIGSERIAL PRIMARY KEY,
                            tienda_id BIGINT NOT NULL,
                            nombre VARCHAR(100) NOT NULL,
                            UNIQUE(tienda_id, nombre),
                            FOREIGN KEY (tienda_id) REFERENCES tiendas(id) ON DELETE CASCADE
);

CREATE TABLE productos (
                           id BIGSERIAL PRIMARY KEY,
                           tienda_id BIGINT NOT NULL,
                           nombre VARCHAR(255) NOT NULL,
                           descripcion TEXT,
                           imagen_url TEXT,
                           precio_venta DECIMAL(10,2) NOT NULL,
                           precio_unitario DECIMAL(10,2) NOT NULL,
                           activo BOOLEAN DEFAULT true,
                           categoria_id BIGINT,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                           FOREIGN KEY (tienda_id) REFERENCES tiendas(id) ON DELETE CASCADE,
                           FOREIGN KEY (categoria_id) REFERENCES categorias(id) ON DELETE SET NULL
);

-- Cuentas
CREATE TABLE cuentas_cliente (
                                 id BIGSERIAL PRIMARY KEY,
                                 usuario_id BIGINT NOT NULL,
                                 tienda_id BIGINT NOT NULL,
                                 limite_credito DECIMAL(10,2) DEFAULT 0.00,
                                 saldo_actual DECIMAL(10,2) DEFAULT 0.00,
                                 saldo_disponible DECIMAL(10,2) GENERATED ALWAYS AS (limite_credito - saldo_actual) STORED,
                                 fecha_apertura DATE DEFAULT CURRENT_DATE,
                                 activa BOOLEAN DEFAULT true,
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                 UNIQUE(usuario_id, tienda_id),

                                 FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
                                 FOREIGN KEY (tienda_id) REFERENCES tiendas(id) ON DELETE CASCADE
);

-- Finanzas
CREATE TABLE ventas (
                        id BIGSERIAL PRIMARY KEY,
                        tienda_id BIGINT NOT NULL,
                        cuenta_cliente_id BIGINT,
                        cliente_ocasional VARCHAR(255),
                        fecha_venta TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        subtotal DECIMAL(10,2) NOT NULL,
                        total DECIMAL(10,2) NOT NULL,
                        tipo_venta tipo_venta DEFAULT 'CREDITO',
                        estado estado_venta DEFAULT 'PENDIENTE',
                        observaciones TEXT,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                        FOREIGN KEY (tienda_id) REFERENCES tiendas(id) ON DELETE CASCADE,
                        FOREIGN KEY (cuenta_cliente_id) REFERENCES cuentas_cliente(id) ON DELETE SET NULL,

                        CONSTRAINT chk_cliente CHECK (
                            (cuenta_cliente_id IS NOT NULL AND cliente_ocasional IS NULL)
                                OR
                            (cuenta_cliente_id IS NULL AND cliente_ocasional IS NOT NULL)
                            )
);

CREATE TABLE detalle_ventas (
                                id BIGSERIAL PRIMARY KEY,
                                venta_id BIGINT NOT NULL,
                                producto_id BIGINT NOT NULL,
                                cantidad INTEGER NOT NULL,
                                precio_unitario DECIMAL(10,2) NOT NULL,
                                subtotal DECIMAL(10,2) NOT NULL,

                                FOREIGN KEY (venta_id) REFERENCES ventas(id) ON DELETE CASCADE,
                                FOREIGN KEY (producto_id) REFERENCES productos(id) ON DELETE RESTRICT
);

CREATE TABLE abonos (
                        id BIGSERIAL PRIMARY KEY,
                        tienda_id BIGINT NOT NULL,
                        cuenta_cliente_id BIGINT NOT NULL,
                        monto DECIMAL(10,2) NOT NULL,
                        fecha_abono TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        metodo_pago metodo_pago DEFAULT 'EFECTIVO',
                        observaciones TEXT,
                        estado estado_abono DEFAULT 'APLICADO',

                        FOREIGN KEY (tienda_id) REFERENCES tiendas(id) ON DELETE CASCADE,
                        FOREIGN KEY (cuenta_cliente_id) REFERENCES cuentas_cliente(id) ON DELETE CASCADE
);

-- Movimientos
CREATE TABLE movimientos_tienda (
                                    id BIGSERIAL PRIMARY KEY,
                                    tienda_id BIGINT NOT NULL,
                                    usuario_operador_id BIGINT NOT NULL,
                                    cliente_usuario_id BIGINT,
                                    tipo_evento tipo_evento NOT NULL,
                                    descripcion TEXT NOT NULL,
                                    monto DECIMAL(10,2),
                                    referencia_id BIGINT,
                                    referencia_tabla VARCHAR(50),
                                    fecha_evento TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                    FOREIGN KEY (tienda_id) REFERENCES tiendas(id) ON DELETE CASCADE,
                                    FOREIGN KEY (usuario_operador_id) REFERENCES usuarios(id) ON DELETE RESTRICT,
                                    FOREIGN KEY (cliente_usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL
);

-- Notificaciones
CREATE TABLE notificaciones (
                                id BIGSERIAL PRIMARY KEY,
                                usuario_id BIGINT NOT NULL,
                                tienda_id BIGINT,
                                tipo tipo_notificacion NOT NULL,
                                asunto VARCHAR(255) NOT NULL,
                                mensaje TEXT NOT NULL,
                                leida BOOLEAN DEFAULT false,
                                fecha_envio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
                                FOREIGN KEY (tienda_id) REFERENCES tiendas(id) ON DELETE CASCADE
);
