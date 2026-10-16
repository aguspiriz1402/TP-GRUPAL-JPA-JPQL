-- Ejecutar luego de Main, sobre la misma base PostgreSQL.
SELECT f.id, f.numero, f.fecha_emision, f.estado, f.importe_total,
       COUNT(d.id) AS cantidad_detalles, SUM(d.importe_subtotal) AS suma_detalles
FROM facturas_venta f
LEFT JOIN facturas_venta_detalles d ON d.factura_id = f.id
GROUP BY f.id, f.numero, f.fecha_emision, f.estado, f.importe_total
ORDER BY f.id;

SELECT d.id, d.factura_id, a.codigo, d.descripcion,
       d.cantidad, d.precio_unitario, d.importe_subtotal
FROM facturas_venta_detalles d
JOIN listas_precio_articulos lpa ON lpa.id = d.lista_precio_articulo_id
JOIN articulos a ON a.id = lpa.articulo_id
ORDER BY d.factura_id, d.id;

-- Las columnas heredadas se encuentran en cada tabla concreta.
SELECT id, fecha_alta, fecha_modificacion, usuario_carga_id,
       usuario_modificacion_id, fecha_baja, usuario_baja_id
FROM facturas_venta;
