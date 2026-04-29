package com.facturacion.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Body POST comprobante según consigna entrega final (clienteid / productoid en minúsculas).
 */
public class CrearComprobanteRequest {

    @NotNull(message = "cliente es obligatorio")
    @Valid
    private ClienteNested cliente;

    @NotEmpty(message = "Debe incluir al menos una línea")
    @Valid
    private List<LineaNested> lineas;

    public ClienteNested getCliente() {
        return cliente;
    }

    public void setCliente(ClienteNested cliente) {
        this.cliente = cliente;
    }

    public List<LineaNested> getLineas() {
        return lineas;
    }

    public void setLineas(List<LineaNested> lineas) {
        this.lineas = lineas;
    }

    public static class ClienteNested {

        @NotNull(message = "clienteid es obligatorio")
        @JsonProperty("clienteid")
        private Long clienteid;

        public Long getClienteid() {
            return clienteid;
        }

        public void setClienteid(Long clienteid) {
            this.clienteid = clienteid;
        }
    }

    public static class LineaNested {

        @NotNull
        @Min(value = 1, message = "cantidad debe ser >= 1")
        private Integer cantidad;

        @NotNull(message = "producto es obligatorio en cada línea")
        @Valid
        private ProductoNested producto;

        public Integer getCantidad() {
            return cantidad;
        }

        public void setCantidad(Integer cantidad) {
            this.cantidad = cantidad;
        }

        public ProductoNested getProducto() {
            return producto;
        }

        public void setProducto(ProductoNested producto) {
            this.producto = producto;
        }
    }

    public static class ProductoNested {

        @NotNull(message = "productoid es obligatorio")
        @JsonProperty("productoid")
        private Long productoid;

        public Long getProductoid() {
            return productoid;
        }

        public void setProductoid(Long productoid) {
            this.productoid = productoid;
        }
    }
}
