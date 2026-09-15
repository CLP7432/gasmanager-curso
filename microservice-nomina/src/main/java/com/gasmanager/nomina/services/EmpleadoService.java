package com.gasmanager.nomina.services;

import com.gasmanager.nomina.dto.EmpleadoDTO;
import com.gasmanager.nomina.dto.EmpleadoPuestoHistorialDTO;
import com.gasmanager.nomina.entities.Departamento;
import com.gasmanager.nomina.entities.Empleado;
import com.gasmanager.nomina.entities.EmpleadoPuestoHistorial;
import com.gasmanager.nomina.entities.Puesto;
import com.gasmanager.nomina.exceptions.RecursoNoEncontradoException;
import com.gasmanager.nomina.exceptions.ValidacionException;
import com.gasmanager.nomina.repositories.DepartamentoRepository;
import com.gasmanager.nomina.repositories.EmpleadoRepository;
import com.gasmanager.nomina.repositories.PuestoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final PuestoRepository puestoRepository;
    private final DepartamentoRepository departamentoRepository;

    private EmpleadoPuestoHistorialDTO historialDTO(EmpleadoPuestoHistorial historial) {
        return EmpleadoPuestoHistorialDTO.builder()
                .id(historial.getId())
                .puestoId(historial.getPuesto() != null ? historial.getPuesto().getId() : null)
                .puestoNombre(historial.getPuesto() != null ? historial.getPuesto().getNombre() : null)
                .salarioDiario(historial.getSalarioDiario())
                .salarioMensual(historial.getSalarioMensual())
                .fechaInicio(historial.getFechaInicio())
                .fechaFin(historial.getFechaFin())
                .activo(historial.getActivo())
                .motivoCambio(historial.getMotivoCambio())
                .build();
    }

    private EmpleadoDTO aDTO(Empleado empleado) {
        return EmpleadoDTO.builder()
                .id(empleado.getId())
                .codigoEmpleado(empleado.getCodigoEmpleado())
                .nombre(empleado.getNombre())
                .apellidoPaterno(empleado.getApellidoPaterno())
                .apellidoMaterno(empleado.getApellidoMaterno())
                .nombreCompleto(empleado.getNombreCompleto())
                .rfc(empleado.getRfc())
                .curp(empleado.getCurp())
                .nss(empleado.getNss())
                .email(empleado.getEmail())
                .telefono(empleado.getTelefono())
                .celular(empleado.getCelular())
                .fechaNacimiento(empleado.getFechaNacimiento())
                .fechaIngreso(empleado.getFechaIngreso())
                .fechaBaja(empleado.getFechaBaja())
                .activo(empleado.getActivo())
                .usuarioId(empleado.getUsuarioId())
                .puestoId(empleado.getPuesto() != null ? empleado.getPuesto().getId() : null)
                .puestoNombre(empleado.getPuesto() != null ? empleado.getPuesto().getNombre() : null)
                .departamentoId(empleado.getDepartamento() != null ? empleado.getDepartamento().getId() : null)
                .departamentoNombre(empleado.getDepartamento() != null ? empleado.getDepartamento().getNombre() : null)
                .tipoContrato(empleado.getTipoContrato())
                .tipoJornada(empleado.getTipoJornada())
                .salarioDiario(empleado.getSalarioDiario())
                .salarioMensual(empleado.getSalarioMensual())
                .numeroCuenta(empleado.getNumeroCuenta())
                .banco(empleado.getBanco())
                .direccion(empleado.getDireccion())
                .historial(empleado.getHistorialPuestos().stream().map(this::historialDTO).toList())
                .build();
    }

    private Empleado buscar(Long id) {
        return empleadoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Empleado no encontrado con id " + id));
    }

    private Puesto buscarPuesto(Long id) {
        return puestoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Puesto no encontrado con id " + id));
    }

    private Departamento buscarDepartamento(Long id) {
        return departamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Departamento no encontrado con id " + id));
    }

    @Transactional(readOnly = true)
    public List<EmpleadoDTO> listar() {
        return empleadoRepository.listarTodos().stream().map(this::aDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<EmpleadoDTO> listarActivos() {
        return empleadoRepository.listarActivos().stream().map(this::aDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<EmpleadoDTO> listarPorPuesto(Long puestoId) {
        return empleadoRepository.listarPorPuesto(puestoId).stream().map(this::aDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<EmpleadoDTO> listarPorDepartamento(Long departamentoId) {
        return empleadoRepository.listarPorDepartamento(departamentoId).stream().map(this::aDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<EmpleadoDTO> listarDespachadores() {
        return empleadoRepository.listarTodos().stream()
                .filter(empleado -> empleado.getPuesto() != null
                        && empleado.getPuesto().getNombre().toLowerCase().contains("despachador"))
                .map(this::aDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmpleadoDTO obtenerPorId(Long id) {
        return aDTO(buscar(id));
    }

    private String generarCodigo() {
        return String.format("EMP-%04d", empleadoRepository.count() + 1);
    }

    private void agregarHistorial(Empleado empleado, LocalDate fechaInicio, String motivo) {
        EmpleadoPuestoHistorial historial = EmpleadoPuestoHistorial.builder()
                .empleado(empleado)
                .puesto(empleado.getPuesto())
                .salarioDiario(empleado.getSalarioDiario())
                .salarioMensual(empleado.getSalarioMensual())
                .fechaInicio(fechaInicio)
                .activo(true)
                .motivoCambio(motivo)
                .build();
        empleado.addHistorialPuesto(historial);
    }

    @Transactional
    public EmpleadoDTO crear(EmpleadoDTO dto) {
        String rfc = dto.getRfc();
        if (rfc != null && !rfc.isBlank() && empleadoRepository.existsByRfc(rfc)) {
            throw new ValidacionException("Ya existe un empleado con el RFC " + rfc);
        }
        String codigo = dto.getCodigoEmpleado();
        if (codigo == null || codigo.isBlank()) {
            do {
                codigo = generarCodigo();
            } while (empleadoRepository.existsByCodigoEmpleado(codigo));
        } else if (empleadoRepository.existsByCodigoEmpleado(codigo)) {
            throw new ValidacionException("Ya existe un empleado con el código " + codigo);
        }

        Empleado empleado = Empleado.builder()
                .codigoEmpleado(codigo)
                .nombre(dto.getNombre())
                .apellidoPaterno(dto.getApellidoPaterno())
                .apellidoMaterno(dto.getApellidoMaterno())
                .rfc(dto.getRfc())
                .curp(dto.getCurp())
                .nss(dto.getNss())
                .email(dto.getEmail())
                .telefono(dto.getTelefono())
                .celular(dto.getCelular())
                .fechaNacimiento(dto.getFechaNacimiento())
                .fechaIngreso(dto.getFechaIngreso())
                .activo(true)
                .usuarioId(dto.getUsuarioId())
                .puesto(dto.getPuestoId() != null ? buscarPuesto(dto.getPuestoId()) : null)
                .departamento(dto.getDepartamentoId() != null ? buscarDepartamento(dto.getDepartamentoId()) : null)
                .tipoContrato(dto.getTipoContrato())
                .tipoJornada(dto.getTipoJornada())
                .salarioDiario(dto.getSalarioDiario())
                .salarioMensual(dto.getSalarioMensual() != null
                        ? dto.getSalarioMensual()
                        : dto.getSalarioDiario().multiply(BigDecimal.valueOf(30)))
                .numeroCuenta(dto.getNumeroCuenta())
                .banco(dto.getBanco())
                .direccion(dto.getDireccion())
                .build();

        agregarHistorial(empleado, dto.getFechaIngreso(), "Alta");
        return aDTO(empleadoRepository.save(empleado));
    }

    @Transactional
    public EmpleadoDTO actualizar(Long id, EmpleadoDTO dto) {
        Empleado empleado = buscar(id);
        boolean cambioPuesto = dto.getPuestoId() != null
                && (empleado.getPuesto() == null || !empleado.getPuesto().getId().equals(dto.getPuestoId()));
        boolean cambioSalario = dto.getSalarioDiario() != null
                && !dto.getSalarioDiario().equals(empleado.getSalarioDiario());

        if (dto.getCodigoEmpleado() != null
                && !dto.getCodigoEmpleado().equals(empleado.getCodigoEmpleado())
                && empleadoRepository.existsByCodigoEmpleado(dto.getCodigoEmpleado())) {
            throw new ValidacionException("Ya existe un empleado con el código " + dto.getCodigoEmpleado());
        }
        if (dto.getRfc() != null
                && !dto.getRfc().equals(empleado.getRfc())
                && empleadoRepository.existsByRfc(dto.getRfc())) {
            throw new ValidacionException("Ya existe un empleado con el RFC " + dto.getRfc());
        }

        if (dto.getCodigoEmpleado() != null) empleado.setCodigoEmpleado(dto.getCodigoEmpleado());
        if (dto.getNombre() != null) empleado.setNombre(dto.getNombre());
        if (dto.getApellidoPaterno() != null) empleado.setApellidoPaterno(dto.getApellidoPaterno());
        if (dto.getApellidoMaterno() != null) empleado.setApellidoMaterno(dto.getApellidoMaterno());
        if (dto.getRfc() != null) empleado.setRfc(dto.getRfc());
        if (dto.getCurp() != null) empleado.setCurp(dto.getCurp());
        if (dto.getNss() != null) empleado.setNss(dto.getNss());
        if (dto.getEmail() != null) empleado.setEmail(dto.getEmail());
        if (dto.getTelefono() != null) empleado.setTelefono(dto.getTelefono());
        if (dto.getCelular() != null) empleado.setCelular(dto.getCelular());
        if (dto.getFechaNacimiento() != null) empleado.setFechaNacimiento(dto.getFechaNacimiento());
        if (dto.getFechaIngreso() != null) empleado.setFechaIngreso(dto.getFechaIngreso());
        if (dto.getUsuarioId() != null) empleado.setUsuarioId(dto.getUsuarioId());
        if (dto.getPuestoId() != null) empleado.setPuesto(buscarPuesto(dto.getPuestoId()));
        if (dto.getDepartamentoId() != null) empleado.setDepartamento(buscarDepartamento(dto.getDepartamentoId()));
        if (dto.getTipoContrato() != null) empleado.setTipoContrato(dto.getTipoContrato());
        if (dto.getTipoJornada() != null) empleado.setTipoJornada(dto.getTipoJornada());
        if (dto.getSalarioDiario() != null) empleado.setSalarioDiario(dto.getSalarioDiario());
        if (dto.getSalarioMensual() != null) empleado.setSalarioMensual(dto.getSalarioMensual());
        if (dto.getNumeroCuenta() != null) empleado.setNumeroCuenta(dto.getNumeroCuenta());
        if (dto.getBanco() != null) empleado.setBanco(dto.getBanco());
        if (dto.getDireccion() != null) empleado.setDireccion(dto.getDireccion());

        if (cambioPuesto || cambioSalario) {
            empleado.getHistorialPuestos().stream()
                    .filter(h -> Boolean.TRUE.equals(h.getActivo()) && h.getFechaFin() == null)
                    .forEach(h -> { h.setFechaFin(LocalDate.now()); h.setActivo(false); });
            agregarHistorial(empleado, LocalDate.now(), "Cambio de puesto/salario");
        }
        return aDTO(empleadoRepository.save(empleado));
    }

    @Transactional
    public EmpleadoDTO desactivar(Long id, LocalDate fechaBaja, String motivo) {
        Empleado empleado = buscar(id);
        empleado.setActivo(false);
        empleado.setFechaBaja(fechaBaja != null ? fechaBaja : LocalDate.now());
        empleado.getHistorialPuestos().stream()
                .filter(h -> Boolean.TRUE.equals(h.getActivo()) && h.getFechaFin() == null)
                .forEach(h -> { h.setFechaFin(empleado.getFechaBaja()); h.setActivo(false); });
        if (motivo != null && !motivo.isBlank()) {
            agregarHistorial(empleado, empleado.getFechaBaja(), motivo);
        }
        return aDTO(empleadoRepository.save(empleado));
    }

    @Transactional
    public EmpleadoDTO reactivar(Long id) {
        Empleado empleado = buscar(id);
        empleado.setActivo(true);
        empleado.setFechaBaja(null);
        return aDTO(empleadoRepository.save(empleado));
    }
}