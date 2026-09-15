package com.gasmanager.facturacion.services;

import com.gasmanager.facturacion.dto.ClienteFiscalDTO;
import com.gasmanager.facturacion.entities.ClienteFiscal;
import com.gasmanager.facturacion.exceptions.RecursoNoEncontradoException;
import com.gasmanager.facturacion.repositories.ClienteFiscalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteFiscalService {

    private final ClienteFiscalRepository repository;

    public ClienteFiscalDTO aDTO(ClienteFiscal cf) {
        if (cf == null) return null;
        return ClienteFiscalDTO.builder()
                .id(cf.getId())
                .clienteId(cf.getClienteId())
                .rfc(cf.getRfc())
                .razonSocial(cf.getRazonSocial())
                .regimenFiscal(cf.getRegimenFiscal())
                .codigoPostal(cf.getCodigoPostal())
                .usoCfdi(cf.getUsoCfdi())
                .correo(cf.getCorreo())
                .build();
    }

    @Transactional(readOnly = true)
    public List<ClienteFiscalDTO> listar() {
        return repository.findAll().stream().map(this::aDTO).toList();
    }

    @Transactional(readOnly = true)
    public ClienteFiscalDTO obtener(Long id) {
        return aDTO(buscar(id));
    }

    @Transactional(readOnly = true)
    public ClienteFiscalDTO obtenerPorClienteId(Long clienteId) {
        return aDTO(repository.findByClienteId(clienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El cliente " + clienteId + " no tiene datos fiscales registrados")));
    }

    @Transactional
    public ClienteFiscalDTO guardar(ClienteFiscalDTO dto) {
        if (dto.getClienteId() == null) {
            throw new IllegalArgumentException("El clienteId es obligatorio");
        }
        if (repository.existsByClienteId(dto.getClienteId()) && dto.getId() == null) {
            throw new IllegalArgumentException("El cliente ya tiene datos fiscales registrados");
        }
        if (dto.getUsoCfdi() == null || dto.getUsoCfdi().isBlank()) {
            dto.setUsoCfdi("G03");
        }
        ClienteFiscal cf = ClienteFiscal.builder()
                .clienteId(dto.getClienteId())
                .rfc(normalizarRfc(dto.getRfc()))
                .razonSocial(dto.getRazonSocial())
                .regimenFiscal(dto.getRegimenFiscal())
                .codigoPostal(dto.getCodigoPostal())
                .usoCfdi(dto.getUsoCfdi())
                .correo(dto.getCorreo())
                .build();
        return aDTO(repository.save(cf));
    }

    @Transactional
    public ClienteFiscalDTO actualizar(Long id, ClienteFiscalDTO dto) {
        ClienteFiscal cf = buscar(id);
        if (dto.getRfc() != null && !dto.getRfc().isBlank()) cf.setRfc(normalizarRfc(dto.getRfc()));
        if (dto.getRazonSocial() != null) cf.setRazonSocial(dto.getRazonSocial());
        if (dto.getRegimenFiscal() != null && !dto.getRegimenFiscal().isBlank()) cf.setRegimenFiscal(dto.getRegimenFiscal());
        if (dto.getCodigoPostal() != null && !dto.getCodigoPostal().isBlank()) cf.setCodigoPostal(dto.getCodigoPostal());
        if (dto.getUsoCfdi() != null && !dto.getUsoCfdi().isBlank()) cf.setUsoCfdi(dto.getUsoCfdi());
        if (dto.getCorreo() != null) cf.setCorreo(dto.getCorreo());
        return aDTO(repository.save(cf));
    }

    @Transactional
    public void eliminar(Long id) {
        ClienteFiscal cf = buscar(id);
        repository.delete(cf);
    }

    private ClienteFiscal buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente fiscal no encontrado con id: " + id));
    }

    private String normalizarRfc(String rfc) {
        if (rfc == null) {
            throw new IllegalArgumentException("El RFC es obligatorio");
        }
        return rfc.trim().toUpperCase();
    }
}