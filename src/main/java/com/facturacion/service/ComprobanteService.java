package com.facturacion.service;

import com.facturacion.dto.CrearComprobanteRequest;
import com.facturacion.entity.Cliente;
import com.facturacion.entity.Comprobante;
import com.facturacion.entity.LineaComprobante;
import com.facturacion.entity.Producto;
import com.facturacion.exception.ResourceNotFoundException;
import com.facturacion.repository.ClienteRepository;
import com.facturacion.repository.ComprobanteRepository;
import com.facturacion.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ComprobanteService {

    private final ComprobanteRepository comprobanteRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;

    public ComprobanteService(ComprobanteRepository comprobanteRepository,
                              ClienteRepository clienteRepository,
                              ProductoRepository productoRepository) {
        this.comprobanteRepository = comprobanteRepository;
        this.clienteRepository = clienteRepository;
        this.productoRepository = productoRepository;
    }

    public List<Comprobante> listar() {
        return comprobanteRepository.findAll();
    }

    public Comprobante obtener(Long id) {
        return comprobanteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comprobante no encontrado: " + id));
    }

    /**
     * Crea un comprobante, descuenta stock y guarda precio unitario histórico por línea.
     */
    @Transactional
    public Comprobante crear(CrearComprobanteRequest request) {
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + request.getClienteId()));

        Comprobante comprobante = new Comprobante();
        comprobante.setFechaEmision(LocalDateTime.now());
        comprobante.setCliente(cliente);

        for (CrearComprobanteRequest.LineaItem item : request.getLineas()) {
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + item.getProductoId()));

            if (producto.getStock() < item.getCantidad()) {
                throw new IllegalArgumentException(
                        "Stock insuficiente para el producto " + producto.getCodigo()
                                + " (disponible: " + producto.getStock() + ", solicitado: " + item.getCantidad() + ")");
            }

            LineaComprobante linea = new LineaComprobante();
            linea.setProducto(producto);
            linea.setCantidad(item.getCantidad());
            linea.setPrecioUnitario(producto.getPrecio());
            producto.setStock(producto.getStock() - item.getCantidad());

            comprobante.addLinea(linea);
        }

        return comprobanteRepository.save(comprobante);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!comprobanteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Comprobante no encontrado: " + id);
        }
        comprobanteRepository.deleteById(id);
    }
}
