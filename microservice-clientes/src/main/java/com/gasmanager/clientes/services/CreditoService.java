package com.gasmanager.clientes.services;

import com.gasmanager.clientes.dto.AbonoCreditoDTO;
import com.gasmanager.clientes.dto.CreditoDTO;
import com.gasmanager.clientes.entities.AbonoCredito;
import com.gasmanager.clientes.entities.Cliente;
import com.gasmanager.clientes.entities.Credito;
import com.gasmanager.clientes.enums.EstadoCredito;
import com.gasmanager.clientes.exceptions.RecursoNoEncontradoException;
import com.gasmanager.clientes.repositories.AbonoCreditoRepository;
import com.gasmanager.clientes.repositories.ClienteRepository;
import com.gasmanager.clientes.repositories.CreditoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CreditoService {

    private final CreditoRepository creditoRepository;
    private final ClienteRepository clienteRepository;
    private final AbonoCreditoRepository abonoRepository;

    public CreditoDTO crearCredito(CreditoDTO dto){
        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Cliente no existe: " + dto.getClienteId()));
        if(!cliente.getActivo()){
            throw new IllegalStateException("Cliente inactivo");
        }
        Credito c = Credito.builder()
                .folioCredito(generarFolio())
                .cliente(cliente)
                .montoTotal(dto.getMontoTotal())
                .saldoPendiente(dto.getMontoTotal())
                .estado(dto.getEstado() != null ? dto.getEstado() : EstadoCredito.ACTIVO)
                .plazoMeses(dto.getPlazoMeses())
                .tasaInteres(dto.getTasaInteres())
                .montoInteres(dto.getMontoInteres())
                .fechaInicio(dto.getFechaInicio())
                .fechaVencimiento(dto.getFechaVencimiento())
                .metodoPago(dto.getMetodoPago())
                .diaPago(dto.getDiaPago())
                .notas(dto.getNotas())
                .build();
        return aDTO(creditoRepository.save(c));

    }
    public CreditoDTO registrarAbono(Long creditoId, AbonoCreditoDTO dto){
        Credito credito = creditoRepository.findById(creditoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Credito no existe: " + creditoId
                ));
        if(dto.getMonto().compareTo(credito.getSaldoPendiente()) > 0){
            throw new IllegalStateException("Abono supera saldo pendiente");
        }
        AbonoCredito abono = AbonoCredito.builder()
                .folioAbono(generarFolioAbono())
                .credito(credito)
                .monto(dto.getMonto())
                .fechaAbono(dto.getFechaAbono())
                .metodoPago(dto.getMetodoPago())
                .referenciaPago(dto.getReferenciaPago())
                .notas(dto.getNotas())
                .build();
        credito.addAbono(abono);
        return aDTO(creditoRepository.save(credito));
    }
    public List<CreditoDTO> listarPorCliente(Long clienteId){
        return creditoRepository.findByClienteId(clienteId)
                .stream()
                .map(this::aDTO)
                .toList();
    }
    public CreditoDTO obtener(Long id){
        return aDTO(creditoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe " + id)));
    }
    private String generarFolio(){
        return "CRED-" + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        ) + "-" + (creditoRepository.count() + 1);
    }
    private String generarFolioAbono(){
        return "ABONO-" + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        ) + "-" + (abonoRepository.count() + 1);
    }
    private CreditoDTO aDTO(Credito c){
        return CreditoDTO.builder()
                .id(c.getId())
                .folioCredito(c.getFolioCredito())
                .clienteId(c.getCliente().getId())
                .clienteNombre(c.getCliente().getRazonSocial())
                .montoTotal(c.getMontoTotal())
                .montoPagado(c.getMontoPagado())
                .saldoPendiente(c.getSaldoPendiente())
                .plazoMeses(c.getPlazoMeses())
                .tasaInteres(c.getTasaInteres())
                .montoInteres(c.getMontoInteres())
                .fechaInicio(c.getFechaInicio())
                .fechaVencimiento(c.getFechaVencimiento())
                .fechaUltimoPago(c.getFechaUltimoPago())
                .estado(c.getEstado())
                .metodoPago(c.getMetodoPago())
                .diaPago(c.getDiaPago())
                .notas(c.getNotas())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdateAt())
                .build();
    }
}
