package com.facturacion.repository;

import com.facturacion.entity.LineaComprobante;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LineaComprobanteRepository extends JpaRepository<LineaComprobante, Long> {
}
