-- Script DDL para el modelo de Facturación.
-- Compatible con MySQL 8+ (utf8mb4). Ajustá el nombre de la base si tu curso usa otro motor.

CREATE DATABASE IF NOT EXISTS facturacion
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE facturacion;

DROP TABLE IF EXISTS linea_comprobante;
DROP TABLE IF EXISTS comprobante;
DROP TABLE IF EXISTS producto;
DROP TABLE IF EXISTS cliente;

CREATE TABLE cliente (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    documento VARCHAR(20) NOT NULL,
    CONSTRAINT uk_cliente_documento UNIQUE (documento)
) ENGINE=InnoDB;

CREATE TABLE producto (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    descripcion VARCHAR(500),
    precio DECIMAL(12, 2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    CONSTRAINT uk_producto_codigo UNIQUE (codigo)
) ENGINE=InnoDB;

CREATE TABLE comprobante (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha_emision DATETIME NOT NULL,
    cliente_id BIGINT NOT NULL,
    CONSTRAINT fk_comprobante_cliente FOREIGN KEY (cliente_id)
        REFERENCES cliente (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    INDEX idx_comprobante_cliente (cliente_id),
    INDEX idx_comprobante_fecha (fecha_emision)
) ENGINE=InnoDB;

CREATE TABLE linea_comprobante (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    comprobante_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(12, 2) NOT NULL,
    CONSTRAINT fk_linea_comprobante FOREIGN KEY (comprobante_id)
        REFERENCES comprobante (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_linea_producto FOREIGN KEY (producto_id)
        REFERENCES producto (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    INDEX idx_linea_comprobante (comprobante_id),
    INDEX idx_linea_producto (producto_id),
    CONSTRAINT chk_linea_cantidad_positiva CHECK (cantidad > 0)
) ENGINE=InnoDB;
