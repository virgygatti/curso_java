package com.facturacion.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Comprobante de venta asociado a un cliente y a una lista de líneas de detalle.
 */
public class Comprobante {

    private Long id;
    private LocalDateTime fechaEmision;
    private Cliente cliente;
    private final List<LineaComprobante> lineas = new ArrayList<>();

    public Comprobante() {
    }

    public Comprobante(Long id, LocalDateTime fechaEmision, Cliente cliente) {
        this.id = id;
        this.fechaEmision = fechaEmision;
        this.cliente = cliente;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDateTime fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    /**
     * Lista de líneas del comprobante (no modificable desde fuera).
     */
    public List<LineaComprobante> getLineas() {
        return Collections.unmodifiableList(lineas);
    }

    public void agregarLinea(LineaComprobante linea) {
        if (linea != null) {
            lineas.add(linea);
        }
    }

    /**
     * Total del comprobante según los subtotales de cada línea.
     */
    public BigDecimal calcularTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (LineaComprobante linea : lineas) {
            total = total.add(linea.calcularSubtotal());
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    /** Cantidad total de unidades vendidas en todas las líneas. */
    public int calcularCantidadProductos() {
        int suma = 0;
        for (LineaComprobante linea : lineas) {
            suma += linea.getCantidad();
        }
        return suma;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Comprobante that = (Comprobante) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Comprobante{id=" + id + ", fechaEmision=" + fechaEmision + ", cliente=" + cliente
                + ", lineas=" + lineas.size() + "}";
    }
}
