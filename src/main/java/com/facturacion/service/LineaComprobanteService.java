package com.facturacion.service;

import com.facturacion.entity.LineaComprobante;
import com.facturacion.exception.ResourceNotFoundException;
import com.facturacion.repository.LineaComprobanteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LineaComprobanteService {

    private final LineaComprobanteRepository lineaComprobanteRepository;

    public LineaComprobanteService(LineaComprobanteRepository lineaComprobanteRepository) {
        this.lineaComprobanteRepository = lineaComprobanteRepository;
    }

    public List<LineaComprobante> listar() {
        return lineaComprobanteRepository.findAll();
    }

    public LineaComprobante obtener(Long id) {
        return lineaComprobanteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Línea de comprobante no encontrada: " + id));
    }

    /**
     * Las líneas se gestionan al crear el comprobante (cascade). Actualización manual raramente necesaria.
     */
    @Transactional
    public void eliminar(Long id) {
        if (!lineaComprobanteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Línea de comprobante no encontrada: " + id);
        }
        lineaComprobanteRepository.deleteById(id);
    }
}
