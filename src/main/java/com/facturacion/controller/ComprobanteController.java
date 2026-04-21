package com.facturacion.controller;

import com.facturacion.dto.CrearComprobanteRequest;
import com.facturacion.entity.Comprobante;
import com.facturacion.service.ComprobanteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/comprobantes")
public class ComprobanteController {

    private final ComprobanteService comprobanteService;

    public ComprobanteController(ComprobanteService comprobanteService) {
        this.comprobanteService = comprobanteService;
    }

    @GetMapping
    public List<Comprobante> listar() {
        return comprobanteService.listar();
    }

    @GetMapping("/{id}")
    public Comprobante obtener(@PathVariable Long id) {
        return comprobanteService.obtener(id);
    }

    @PostMapping
    public ResponseEntity<Comprobante> crear(@Valid @RequestBody CrearComprobanteRequest request) {
        Comprobante guardado = comprobanteService.crear(request);
        return ResponseEntity.created(URI.create("/api/comprobantes/" + guardado.getId())).body(guardado);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        comprobanteService.eliminar(id);
    }
}
