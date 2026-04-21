package com.facturacion.service;

import com.facturacion.entity.Cliente;
import com.facturacion.exception.ResourceNotFoundException;
import com.facturacion.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public List<Cliente> listar() {
        return clienteRepository.findAll();
    }

    public Cliente obtener(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + id));
    }

    @Transactional
    public Cliente crear(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    @Transactional
    public Cliente actualizar(Long id, Cliente datos) {
        Cliente existente = obtener(id);
        existente.setNombre(datos.getNombre());
        existente.setApellido(datos.getApellido());
        existente.setDocumento(datos.getDocumento());
        return clienteRepository.save(existente);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cliente no encontrado: " + id);
        }
        clienteRepository.deleteById(id);
    }
}
