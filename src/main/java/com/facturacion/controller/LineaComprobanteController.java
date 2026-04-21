package com.facturacion.controller;

import com.facturacion.entity.LineaComprobante;
import com.facturacion.service.LineaComprobanteService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lineas-comprobante")
public class LineaComprobanteController {

    private final LineaComprobanteService lineaComprobanteService;

    public LineaComprobanteController(LineaComprobanteService lineaComprobanteService) {
        this.lineaComprobanteService = lineaComprobanteService;
    }

    @GetMapping
    public List<LineaComprobante> listar() {
        return lineaComprobanteService.listar();
    }

    @GetMapping("/{id}")
    public LineaComprobante obtener(@PathVariable Long id) {
        return lineaComprobanteService.obtener(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        lineaComprobanteService.eliminar(id);
    }
}
