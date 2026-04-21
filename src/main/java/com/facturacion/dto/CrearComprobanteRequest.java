package com.facturacion.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class CrearComprobanteRequest {

    @NotNull(message = "clienteId es obligatorio")
    private Long clienteId;

    @NotEmpty(message = "Debe incluir al menos una línea")
    @Valid
    private List<LineaItem> lineas;

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public List<LineaItem> getLineas() {
        return lineas;
    }

    public void setLineas(List<LineaItem> lineas) {
        this.lineas = lineas;
    }

    public static class LineaItem {

        @NotNull(message = "productoId es obligatorio")
        private Long productoId;

        @NotNull
        @Min(value = 1, message = "cantidad debe ser >= 1")
        private Integer cantidad;

        public Long getProductoId() {
            return productoId;
        }

        public void setProductoId(Long productoId) {
            this.productoId = productoId;
        }

        public Integer getCantidad() {
            return cantidad;
        }

        public void setCantidad(Integer cantidad) {
            this.cantidad = cantidad;
        }
    }
}
