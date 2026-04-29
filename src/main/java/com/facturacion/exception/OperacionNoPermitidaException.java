package com.facturacion.exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OperacionNoPermitidaException extends RuntimeException {

    private final List<String> errores;

    public OperacionNoPermitidaException(List<String> errores) {
        super(String.join("; ", errores));
        this.errores = new ArrayList<>(errores);
    }

    public OperacionNoPermitidaException(String mensaje) {
        this(List.of(mensaje));
    }

    public List<String> getErrores() {
        return Collections.unmodifiableList(errores);
    }
}
