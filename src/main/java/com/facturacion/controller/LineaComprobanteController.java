package com.facturacion.controller;

import com.facturacion.entity.LineaComprobante;
import com.facturacion.service.LineaComprobanteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lineas-comprobante")
@Tag(name = "Líneas de comprobante")
public class LineaComprobanteController {

    private final LineaComprobanteService lineaComprobanteService;

    public LineaComprobanteController(LineaComprobanteService lineaComprobanteService) {
        this.lineaComprobanteService = lineaComprobanteService;
    }

    @GetMapping
    @Operation(summary = "Listar líneas")
    public List<LineaComprobante> listar() {
        return lineaComprobanteService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener línea por id")
    public LineaComprobante obtener(@PathVariable Long id) {
        return lineaComprobanteService.obtener(id);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar línea")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        lineaComprobanteService.eliminar(id);
        return ResponseEntity.ok().build();
    }
}
