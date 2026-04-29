package com.facturacion.dto;

import com.facturacion.entity.Comprobante;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Respuesta del servicio de creación de comprobante (punto 4): fecha, total, cantidad de productos.
 */
public class ComprobanteCreadoResponse {

    private Long comprobanteId;
    private LocalDateTime fechaEmision;
    private BigDecimal total;
    private long cantidadProductos;
    private Comprobante comprobante;

    public ComprobanteCreadoResponse() {
    }

    public ComprobanteCreadoResponse(Long comprobanteId, LocalDateTime fechaEmision, BigDecimal total,
                                     long cantidadProductos, Comprobante comprobante) {
        this.comprobanteId = comprobanteId;
        this.fechaEmision = fechaEmision;
        this.total = total;
        this.cantidadProductos = cantidadProductos;
        this.comprobante = comprobante;
    }

    public Long getComprobanteId() {
        return comprobanteId;
    }

    public void setComprobanteId(Long comprobanteId) {
        this.comprobanteId = comprobanteId;
    }

    public LocalDateTime getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDateTime fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public long getCantidadProductos() {
        return cantidadProductos;
    }

    public void setCantidadProductos(long cantidadProductos) {
        this.cantidadProductos = cantidadProductos;
    }

    public Comprobante getComprobante() {
        return comprobante;
    }

    public void setComprobante(Comprobante comprobante) {
        this.comprobante = comprobante;
    }
}
