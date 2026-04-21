-- Datos semilla (INSERT IGNORE permite reiniciar la app sin fallar si ya existen ids)
INSERT IGNORE INTO cliente (id, nombre, apellido, documento) VALUES
    (1, 'Ana', 'García', '30123456');

INSERT IGNORE INTO producto (id, codigo, nombre, descripcion, precio, stock) VALUES
    (1, 'SKU-001', 'Teclado mecánico', 'Layout ES', 8999.99, 12);
