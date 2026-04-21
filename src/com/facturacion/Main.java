package com.facturacion;

import com.facturacion.model.Cliente;
import com.facturacion.model.Comprobante;
import com.facturacion.model.LineaComprobante;
import com.facturacion.model.Producto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Punto de entrada opcional para probar el modelo del dominio (primera entrega).
 */
public class Main {

    public static void main(String[] args) {
        Cliente cliente = new Cliente(null, "Ana", "García", "30123456");
        Producto producto = new Producto(null, "SKU-001", "Teclado mecánico",
                "Layout ES", new BigDecimal("8999.99"), 12);

        Comprobante comp = new Comprobante(null, LocalDateTime.now(), cliente);

        BigDecimal precioAlVender = producto.getPrecio();
        LineaComprobante linea = new LineaComprobante(null, producto, 2, precioAlVender);
        comp.agregarLinea(linea);

        System.out.println("Cliente: " + cliente.getNombreCompleto());
        System.out.println(comp);
        System.out.println("Total comprobante: " + comp.calcularTotal());
        System.out.println("Unidades vendidas: " + comp.calcularCantidadProductos());
    }
}
