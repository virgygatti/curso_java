package com.facturacion.service;

import com.facturacion.dto.ComprobanteCreadoResponse;
import com.facturacion.dto.CrearComprobanteRequest;
import com.facturacion.entity.Cliente;
import com.facturacion.entity.Comprobante;
import com.facturacion.entity.LineaComprobante;
import com.facturacion.entity.Producto;
import com.facturacion.exception.OperacionNoPermitidaException;
import com.facturacion.exception.ResourceNotFoundException;
import com.facturacion.repository.ClienteRepository;
import com.facturacion.repository.ComprobanteRepository;
import com.facturacion.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ComprobanteService {

    private final ComprobanteRepository comprobanteRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final WorldClockService worldClockService;

    public ComprobanteService(ComprobanteRepository comprobanteRepository,
                              ClienteRepository clienteRepository,
                              ProductoRepository productoRepository,
                              WorldClockService worldClockService) {
        this.comprobanteRepository = comprobanteRepository;
        this.clienteRepository = clienteRepository;
        this.productoRepository = productoRepository;
        this.worldClockService = worldClockService;
    }

    public List<Comprobante> listar() {
        return comprobanteRepository.findAll();
    }

    public Comprobante obtener(Long id) {
        return comprobanteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comprobante no encontrado: " + id));
    }

    /**
     * Crea comprobante con validaciones (cliente/productos existentes, stock), precio histórico por línea,
     * fecha desde API externa o fallback, y devuelve total y cantidad vendida.
     */
    @Transactional
    public ComprobanteCreadoResponse crear(CrearComprobanteRequest request) {
        List<String> errores = validarRequest(request);
        if (!errores.isEmpty()) {
            throw new OperacionNoPermitidaException(errores);
        }

        Long clienteId = request.getCliente().getClienteid();
        Cliente cliente = clienteRepository.findById(clienteId).orElseThrow();

        LocalDateTime fechaEmision = worldClockService.obtenerFechaHoraEmision();

        Comprobante comprobante = new Comprobante();
        comprobante.setFechaEmision(fechaEmision);
        comprobante.setCliente(cliente);

        for (CrearComprobanteRequest.LineaNested item : request.getLineas()) {
            Long productoId = item.getProducto().getProductoid();
            Producto producto = productoRepository.findById(productoId).orElseThrow();

            LineaComprobante linea = new LineaComprobante();
            linea.setProducto(producto);
            linea.setCantidad(item.getCantidad());
            linea.setPrecioUnitario(producto.getPrecio());
            producto.setStock(producto.getStock() - item.getCantidad());

            comprobante.addLinea(linea);
        }

        Comprobante guardado = comprobanteRepository.save(comprobante);

        BigDecimal total = guardado.calcularTotal();
        long cantidadProductos = guardado.calcularCantidadProductos();

        return new ComprobanteCreadoResponse(
                guardado.getId(),
                guardado.getFechaEmision(),
                total,
                cantidadProductos,
                guardado
        );
    }

    /**
     * Consolida validaciones de negocio (punto 2 y 4): existencia y stock agregado por producto.
     */
    private List<String> validarRequest(CrearComprobanteRequest request) {
        List<String> errores = new ArrayList<>();

        if (request.getCliente() == null || request.getCliente().getClienteid() == null) {
            errores.add("Debe indicarse cliente.clienteid.");
            return errores;
        }

        Long clienteId = request.getCliente().getClienteid();
        if (!clienteRepository.existsById(clienteId)) {
            errores.add("El cliente no existe (id=" + clienteId + ").");
        }

        if (request.getLineas() == null || request.getLineas().isEmpty()) {
            errores.add("Debe incluir al menos una línea de detalle.");
            return errores;
        }

        Map<Long, Integer> cantidadPorProducto = new HashMap<>();
        int lineaNum = 0;
        for (CrearComprobanteRequest.LineaNested linea : request.getLineas()) {
            lineaNum++;
            if (linea.getProducto() == null || linea.getProducto().getProductoid() == null) {
                errores.add("Línea " + lineaNum + ": falta producto.productoid.");
                continue;
            }
            if (linea.getCantidad() == null || linea.getCantidad() < 1) {
                errores.add("Línea " + lineaNum + ": cantidad inválida.");
                continue;
            }
            Long pid = linea.getProducto().getProductoid();
            cantidadPorProducto.merge(pid, linea.getCantidad(), Integer::sum);
        }

        for (Map.Entry<Long, Integer> e : cantidadPorProducto.entrySet()) {
            Long productoId = e.getKey();
            int solicitado = e.getValue();
            productoRepository.findById(productoId).ifPresentOrElse(
                    p -> {
                        if (p.getStock() < solicitado) {
                            errores.add("Stock insuficiente para el producto " + p.getCodigo()
                                    + " (disponible: " + p.getStock() + ", solicitado: " + solicitado + ").");
                        }
                    },
                    () -> errores.add("El producto no existe (id=" + productoId + ").")
            );
        }

        return errores;
    }

    @Transactional
    public void eliminar(Long id) {
        if (!comprobanteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Comprobante no encontrado: " + id);
        }
        comprobanteRepository.deleteById(id);
    }
}
