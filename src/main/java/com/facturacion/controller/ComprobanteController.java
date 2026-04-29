package com.facturacion.controller;

import com.facturacion.dto.ComprobanteCreadoResponse;
import com.facturacion.dto.CrearComprobanteRequest;
import com.facturacion.entity.Comprobante;
import com.facturacion.service.ComprobanteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comprobantes")
@Tag(name = "Comprobantes", description = "Alta y consulta de comprobantes de venta")
public class ComprobanteController {

    private final ComprobanteService comprobanteService;

    public ComprobanteController(ComprobanteService comprobanteService) {
        this.comprobanteService = comprobanteService;
    }

    @GetMapping
    @Operation(summary = "Listar comprobantes")
    public List<Comprobante> listar() {
        return comprobanteService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener comprobante por id")
    public Comprobante obtener(@PathVariable Long id) {
        return comprobanteService.obtener(id);
    }

    @PostMapping
    @Operation(summary = "Crear comprobante (body con cliente.clienteid y lineas[].producto.productoid)")
    public ResponseEntity<ComprobanteCreadoResponse> crear(@Valid @RequestBody CrearComprobanteRequest request) {
        return ResponseEntity.ok(comprobanteService.crear(request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar comprobante")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        comprobanteService.eliminar(id);
        return ResponseEntity.ok().build();
    }
}
