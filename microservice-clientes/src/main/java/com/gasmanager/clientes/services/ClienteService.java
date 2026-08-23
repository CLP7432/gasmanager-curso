package com.gasmanager.clientes.services;

import com.gasmanager.clientes.dto.ClienteDTO;
import com.gasmanager.clientes.entities.Cliente;
import com.gasmanager.clientes.exceptions.RecursoNoEncontradoException;
import com.gasmanager.clientes.repositories.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteDTO crearCliente(ClienteDTO dto){
        if(clienteRepository.existsByRfc(dto.getRfc())){
            throw new IllegalStateException("El RFC ya está registrado");
        }
        Cliente cliente = aEntidad(dto);
        return aDTO(clienteRepository.save(cliente));
    }
    public List<ClienteDTO> listarClientes(){
        return clienteRepository
                .findAll()
                .stream()
                .map(cliente -> aDTO(cliente)).toList();
    }
    public ClienteDTO obtenerCliente(Long id){
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no existe: " + id));
        return aDTO(cliente);
    }
    public ClienteDTO actualizarCliente(Long id, ClienteDTO dto){
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no existe: " +id));
        cliente.setRazonSocial(dto.getRazonSocial());
        cliente.setNombreComercial(dto.getNombreComercial());
        cliente.setEmail(dto.getEmail());
        cliente.setTelefono(dto.getTelefono());
        cliente.setCelular(dto.getCelular());
        cliente.setCalle(dto.getCalle());
        cliente.setNumeroExterior(dto.getNumeroExterior());
        cliente.setNumeroInterior(dto.getNumeroInterior());
        cliente.setColonia(dto.getColonia());
        cliente.setCiudad(dto.getCiudad());
        cliente.setEstado(dto.getEstado());
        cliente.setCodigoPostal(dto.getCodigoPostal());
        return aDTO(clienteRepository.save(cliente));
    }
    public void eliminarCliente(Long id){
        if(!clienteRepository.existsById(id)){
            throw new RecursoNoEncontradoException("Cliente no existe: " + id);
        }
        clienteRepository.deleteById(id);
    }

    //Traduccion DTO <-> Entidad
    private Cliente aEntidad(ClienteDTO dto){
        return Cliente.builder()
                .codigoCliente(dto.getCodigoCliente())
                .tipoPersona(dto.getTipoPersona())
                .razonSocial(dto.getRazonSocial())
                .nombreComercial(dto.getNombreComercial())
                .rfc(dto.getRfc())
                .curp(dto.getCurp())
                .email(dto.getEmail())
                .telefono(dto.getTelefono())
                .celular(dto.getCelular())
                .calle(dto.getCalle())
                .numeroExterior(dto.getNumeroExterior())
                .numeroInterior(dto.getNumeroInterior())
                .colonia(dto.getColonia())
                .ciudad(dto.getCiudad())
                .estado(dto.getEstado())
                .codigoPostal(dto.getCodigoPostal())
                .build();
    }
    private ClienteDTO aDTO(Cliente c){
        return ClienteDTO.builder()
                .id(c.getId())
                .codigoCliente(c.getCodigoCliente())
                .tipoPersona(c.getTipoPersona())
                .razonSocial(c.getRazonSocial())
                .nombreComercial(c.getNombreComercial())
                .rfc(c.getRfc())
                .curp(c.getCurp())
                .email(c.getEmail())
                .telefono(c.getTelefono())
                .celular(c.getCelular())
                .calle(c.getCalle())
                .numeroExterior(c.getNumeroExterior())
                .numeroInterior(c.getNumeroInterior())
                .colonia(c.getColonia())
                .ciudad(c.getCiudad())
                .estado(c.getEstado())
                .codigoPostal(c.getCodigoPostal())
                .activo(c.getActivo())
                .build();
    }
}
