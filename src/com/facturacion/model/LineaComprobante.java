package com.facturacion.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Detalle de una venta: cantidad, producto y precio unitario al momento del comprobante.
 * El precio unitario se persiste para que cambios posteriores del producto no alteren ventas ya emitidas.
 */
public class LineaComprobante {

    private Long id;
    private Producto producto;
    private int cantidad;
    /** Precio unitario aplicado en esta línea (histórico). */
    private BigDecimal precioUnitario;

    public LineaComprobante() {
    }

    public LineaComprobante(Long id, Producto producto, int cantidad, BigDecimal precioUnitario) {
        this.id = id;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    /**
     * Subtotal de la línea (cantidad × precio unitario de la línea).
     */
    public BigDecimal calcularSubtotal() {
        if (precioUnitario == null || cantidad <= 0) {
            return BigDecimal.ZERO;
        }
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad)).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LineaComprobante that = (LineaComprobante) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "LineaComprobante{id=" + id + ", producto=" + producto + ", cantidad=" + cantidad
                + ", precioUnitario=" + precioUnitario + "}";
    }
}
