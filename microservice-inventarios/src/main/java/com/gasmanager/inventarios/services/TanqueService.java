package com.gasmanager.inventarios.services;

import com.gasmanager.inventarios.dto.TanqueDTO;
import com.gasmanager.inventarios.entities.Combustible;
import com.gasmanager.inventarios.entities.Tanque;
import com.gasmanager.inventarios.exceptions.RecursoNoEncontradoException;
import com.gasmanager.inventarios.repositories.CombustibleRepository;
import com.gasmanager.inventarios.repositories.TanqueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TanqueService {

    public static final BigDecimal CAPACIDAD_MINIMA_LITROS = new BigDecimal("40000");

    private final TanqueRepository tanqueRepository;
    private final CombustibleRepository combustibleRepository;

    public TanqueDTO aDTO(Tanque t) {
        return TanqueDTO.builder()
                .id(t.getId())
                .nombre(t.getNombre())
                .tipoCombustible(t.getTipoCombustible() != null ? t.getTipoCombustible()
                        : (t.getCombustible() != null ? t.getCombustible().getTipo() : null))
                .combustibleId(t.getCombustible() != null ? t.getCombustible().getId() : null)
                .capacidadLitros(t.getCapacidadLitros())
                .stockLitros(t.getStockLitros())
                .activo(t.getActivo())
                .build();
    }

    @Transactional(readOnly = true)
    public List<TanqueDTO> listar() {
        return tanqueRepository.findAllByOrderByNombreAsc().stream()
                .map((this::aDTO)).toList();
    }

    @Transactional(readOnly = true)
    public List<TanqueDTO> listarActivos() {
        return tanqueRepository.findByActivoTrue().stream()
                .map(this::aDTO).toList();
    }

    @Transactional(readOnly = true)
    public TanqueDTO obtenerPorId(Long id) {
        return aDTO(tanqueRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Tanque no encontrado con id: " + id)));
    }

    @Transactional
    public TanqueDTO crear(TanqueDTO dto, Long idUsuario) {
        tanqueRepository.findByNombre(dto.getNombre())
                .ifPresent(t -> {
                    throw new IllegalArgumentException("Ya existe un tanque con el nombre: " + dto.getNombre());
                });
        Combustible combustible = resolverCombustible(dto.getCombustibleId());
        Tanque tanque = Tanque.builder()
                .nombre(dto.getNombre())
                .combustible(combustible)
                .tipoCombustible(combustible.getTipo())
                .capacidadLitros(dto.getCapacidadLitros())
                .stockLitros(dto.getStockLitros())
                .activo(true)
                .createdBy(idUsuario)
                .updatedBy(idUsuario)
                .build();
        validarCapacidadMinima(tanque);
        validarStockMenorIgualCapacidad(tanque);
        return aDTO(tanqueRepository.save(tanque));
    }

    @Transactional
    public TanqueDTO actualizar(Long id, TanqueDTO dto, Long idUsuario) {
        Combustible combustible = resolverCombustible(dto.getCombustibleId());
        Tanque tanque = tanqueRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Tanque no encontrado con id: " + id));
        tanque.setNombre(dto.getNombre());
        tanque.setCombustible(combustible);
        tanque.setTipoCombustible(combustible.getTipo());
        tanque.setCapacidadLitros(dto.getCapacidadLitros());
        tanque.setUpdatedBy(idUsuario);       

        validarCapacidadMinima(tanque);
        validarStockMenorIgualCapacidad(tanque);
        return aDTO(tanqueRepository.save(tanque));
    }

    @Transactional
    public TanqueDTO cargarLitros(Long id, BigDecimal litros, Long idUsuario) {
        Tanque tanque = tanqueRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Tanque no encontrado con id: " + id));
        if (litros == null || litros.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Los litros a cargar deben ser mayores a 0");
        }
        BigDecimal nuevo = tanque.getStockLitros().add(litros);
        if (nuevo.compareTo(tanque.getCapacidadLitros()) > 0) {
            throw new IllegalArgumentException("Rebasa la capacidad del tanque");
        }
        tanque.setStockLitros(nuevo);
        tanque.setUpdatedBy(idUsuario);
        return aDTO(tanqueRepository.save(tanque));
    }

    @Transactional
    public TanqueDTO descargarLitros(Long id, BigDecimal litros, Long idUsuario) {
        Tanque tanque = tanqueRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Tanque no encontrado con id: " + id));
        if (litros == null || litros.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Los litros a descargar deben ser mayores a 0");
        }
        if (tanque.getStockLitros().subtract(litros).compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Stock insuficiente en el tanque");
        }
        tanque.setStockLitros(tanque.getStockLitros().subtract(litros));
        tanque.setUpdatedBy(idUsuario);
        return aDTO(tanqueRepository.save(tanque));
    }

    @Transactional
    public void toggleActivo(Long id, Long idUsuario) {
        Tanque tanque = tanqueRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Tanque no encontrado con id: " + id));
        tanque.setActivo(!tanque.getActivo());
        tanque.setUpdatedBy(idUsuario);
        tanqueRepository.save(tanque);
    }

    private void validarCapacidadMinima(Tanque tanque) {
        if (tanque.getCapacidadLitros().compareTo(CAPACIDAD_MINIMA_LITROS) < 0) {
            throw new IllegalArgumentException(
                    "La capacidad debe ser al menos 40,000 litros para recibir una pipa de 30,000 litros");
        }
    }

    private void validarStockMenorIgualCapacidad(Tanque tanque) {
        if (tanque.getStockLitros().compareTo(tanque.getCapacidadLitros()) > 0) {
            throw new IllegalArgumentException("El stock no puede exceder la capacidad del tanque");
        }
    }

    private Combustible resolverCombustible(Long id) {
        return combustibleRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Combustible no encontrado con id: " + id));
    }
}
