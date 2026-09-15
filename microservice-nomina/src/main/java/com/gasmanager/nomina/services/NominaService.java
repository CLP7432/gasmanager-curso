package com.gasmanager.nomina.services;

import com.gasmanager.nomina.dto.NominaDTO;
import com.gasmanager.nomina.dto.NominaDetalleDTO;
import com.gasmanager.nomina.dto.ProcesarNominaRequestDTO;
import com.gasmanager.nomina.entities.Empleado;
import com.gasmanager.nomina.entities.Nomina;
import com.gasmanager.nomina.entities.NominaDetalle;
import com.gasmanager.nomina.entities.Incidencia;
import com.gasmanager.nomina.enums.EstadoNomina;
import com.gasmanager.nomina.enums.TipoIncidencia;
import com.gasmanager.nomina.exceptions.RecursoNoEncontradoException;
import com.gasmanager.nomina.exceptions.ValidacionException;
import com.gasmanager.nomina.repositories.EmpleadoRepository;
import com.gasmanager.nomina.repositories.IncidenciaRepository;
import com.gasmanager.nomina.repositories.NominaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NominaService {

    private static final int DIAS_PERIODO = 30;
    private static final BigDecimal SEGURO_SOCIAL_PCT = new BigDecimal("0.0400");
    private static final BigDecimal INFONAVIT_PCT = new BigDecimal("0.0250");
    private static final BigDecimal CUOTA_SINDICAL_PCT = new BigDecimal("0.0100");

    private static final BigDecimal[][] TARIFA_ISR_MENSUAL = {
            {new BigDecimal("0.01"), BigDecimal.ZERO, new BigDecimal("0.0192")},
            {new BigDecimal("746.05"), new BigDecimal("14.32"), new BigDecimal("0.0640")},
            {new BigDecimal("6332.06"), new BigDecimal("371.83"), new BigDecimal("0.1088")},
            {new BigDecimal("11128.02"), new BigDecimal("893.63"), new BigDecimal("0.1600")},
            {new BigDecimal("12935.83"), new BigDecimal("1382.72"), new BigDecimal("0.1792")},
            {new BigDecimal("15487.73"), new BigDecimal("1839.83"), new BigDecimal("0.2136")},
            {new BigDecimal("31236.50"), new BigDecimal("5213.38"), new BigDecimal("0.2352")},
            {new BigDecimal("49233.01"), new BigDecimal("9422.45"), new BigDecimal("0.3000")},
            {new BigDecimal("93993.91"), new BigDecimal("22648.23"), new BigDecimal("0.3200")},
            {new BigDecimal("125325.21"), new BigDecimal("32667.24"), new BigDecimal("0.3400")},
            {new BigDecimal("375975.62"), new BigDecimal("117812.43"), new BigDecimal("0.3500")}
    };

    private final NominaRepository nominaRepository;
    private final EmpleadoRepository empleadoRepository;
    private final IncidenciaRepository incidenciaRepository;

    private Nomina buscar(Long id) {
        return nominaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Nómina no encontrada con id " + id));
    }

    private BigDecimal dos(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularISR(BigDecimal totalGravado) {
        if (totalGravado.compareTo(BigDecimal.valueOf(0.01)) < 0) {
            return BigDecimal.ZERO;
        }
        for (int i = TARIFA_ISR_MENSUAL.length - 1; i >= 0; i--) {
            BigDecimal limiteInferior = TARIFA_ISR_MENSUAL[i][0];
            if (totalGravado.compareTo(limiteInferior) >= 0) {
                BigDecimal cuotaFija = TARIFA_ISR_MENSUAL[i][1];
                BigDecimal porcentaje = TARIFA_ISR_MENSUAL[i][2];
                BigDecimal excedente = totalGravado.subtract(limiteInferior);
                return dos(cuotaFija.add(excedente.multiply(porcentaje)));
            }
        }
        return BigDecimal.ZERO;
    }

    private String generarFolio() {
        String marca = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return String.format("NOM-%s-%03d", marca, nominaRepository.count() + 1);
    }

    private NominaDetalleDTO detalleDTO(NominaDetalle detalle) {
        Empleado empleado = detalle.getEmpleado();
        return NominaDetalleDTO.builder()
                .id(detalle.getId())
                .empleadoId(empleado.getId())
                .empleadoCodigo(empleado.getCodigoEmpleado())
                .empleadoNombre(empleado.getNombreCompleto())
                .puestoNombre(empleado.getPuesto() != null ? empleado.getPuesto().getNombre() : null)
                .departamentoNombre(empleado.getDepartamento() != null ? empleado.getDepartamento().getNombre() : null)
                .diasTrabajados(detalle.getDiasTrabajados())
                .sueldoBase(detalle.getSueldoBase())
                .horasExtras(detalle.getHorasExtras())
                .horasExtrasMonto(detalle.getHorasExtrasMonto())
                .faltas(detalle.getFaltas())
                .faltasDescuento(detalle.getFaltasDescuento())
                .retardoDescuento(detalle.getRetardoDescuento())
                .permisoSinGoceDescuento(detalle.getPermisoSinGoceDescuento())
                .bonos(detalle.getBonos())
                .totalGravado(detalle.getTotalGravado())
                .isr(detalle.getIsr())
                .cuotaSindical(detalle.getCuotaSindical())
                .seguroSocial(detalle.getSeguroSocial())
                .infonavit(detalle.getInfonavit())
                .otrasDeducciones(detalle.getOtrasDeducciones())
                .sobrantesMonto(detalle.getSobrantesMonto())
                .totalDeducciones(detalle.getTotalDeducciones())
                .netoPagar(detalle.getNetoPagar())
                .build();
    }

    private NominaDTO aDTO(Nomina nomina) {
        return NominaDTO.builder()
                .id(nomina.getId())
                .folioNomina(nomina.getFolioNomina())
                .periodoInicio(nomina.getPeriodoInicio())
                .periodoFin(nomina.getPeriodoFin())
                .fechaPago(nomina.getFechaPago())
                .fechaProcesamiento(nomina.getFechaProcesamiento())
                .totalEmpleados(nomina.getTotalEmpleados())
                .totalSueldos(nomina.getTotalSueldos())
                .totalHorasExtras(nomina.getTotalHorasExtras())
                .totalBonos(nomina.getTotalBonos())
                .totalDeducciones(nomina.getTotalDeducciones())
                .totalImpuestos(nomina.getTotalImpuestos())
                .totalNeto(nomina.getTotalNeto())
                .estado(nomina.getEstado().name())
                .observaciones(nomina.getObservaciones())
                .detalles(nomina.getDetalles().stream().map(this::detalleDTO).toList())
                .build();
    }

    @Transactional(readOnly = true)
    public List<NominaDTO> listar() {
        return nominaRepository.listarTodas().stream().map(this::aDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<NominaDTO> listarPorEstado(String estado) {
        EstadoNomina enumEstado;
        try {
            enumEstado = EstadoNomina.valueOf(estado);
        } catch (Exception e) {
            throw new ValidacionException("Estado inválido: usa PROCESADA, PAGADA o CANCELADA");
        }
        return nominaRepository.listarPorEstado(enumEstado).stream().map(this::aDTO).toList();
    }

    @Transactional(readOnly = true)
    public NominaDTO obtenerPorId(Long id) {
        return aDTO(buscar(id));
    }

    @Transactional
    public NominaDTO procesar(ProcesarNominaRequestDTO request) {
        if (request.getPeriodoInicio().isAfter(request.getPeriodoFin())) {
            throw new ValidacionException("El periodo de inicio no puede ser posterior al de fin");
        }
        boolean traslape = nominaRepository.countByPeriodoInicioLessThanEqualAndPeriodoFinGreaterThanEqual(
                request.getPeriodoFin(), request.getPeriodoInicio()) > 0;
        if (traslape) {
            throw new ValidacionException("Ya existe una nómina procesada para un periodo que se traslapa");
        }

        List<Empleado> empleados = empleadoRepository.listarActivos();
        if (empleados.isEmpty()) {
            throw new ValidacionException("No hay empleados activos para procesar la nómina");
        }

        Nomina nomina = Nomina.builder()
                .folioNomina(generarFolio())
                .periodoInicio(request.getPeriodoInicio())
                .periodoFin(request.getPeriodoFin())
                .fechaPago(request.getFechaPago() != null ? request.getFechaPago() : LocalDate.now())
                .fechaProcesamiento(LocalDateTime.now())
                .totalEmpleados(empleados.size())
                .totalSueldos(BigDecimal.ZERO)
                .totalHorasExtras(BigDecimal.ZERO)
                .totalBonos(BigDecimal.ZERO)
                .totalDeducciones(BigDecimal.ZERO)
                .totalImpuestos(BigDecimal.ZERO)
                .totalNeto(BigDecimal.ZERO)
                .estado(EstadoNomina.PROCESADA)
                .observaciones(request.getObservaciones())
                .build();

        nominaRepository.save(nomina);

        BigDecimal totalSueldos = BigDecimal.ZERO;
        BigDecimal totalHorasExtras = BigDecimal.ZERO;
        BigDecimal totalBonos = BigDecimal.ZERO;
        BigDecimal totalDeducciones = BigDecimal.ZERO;
        BigDecimal totalImpuestos = BigDecimal.ZERO;
        BigDecimal totalNeto = BigDecimal.ZERO;

        for (Empleado empleado : empleados) {
            NominaDetalle detalle = calcularNominaEmpleado(nomina, empleado, request.getPeriodoInicio(), request.getPeriodoFin());
            nomina.addDetalle(detalle);
            totalSueldos = totalSueldos.add(detalle.getSueldoBase());
            totalHorasExtras = totalHorasExtras.add(detalle.getHorasExtrasMonto());
            totalBonos = totalBonos.add(detalle.getBonos());
            totalDeducciones = totalDeducciones.add(detalle.getTotalDeducciones());
            totalImpuestos = totalImpuestos.add(detalle.getIsr());
            totalNeto = totalNeto.add(detalle.getNetoPagar());
        }

        nomina.setTotalSueldos(dos(totalSueldos));
        nomina.setTotalHorasExtras(dos(totalHorasExtras));
        nomina.setTotalBonos(dos(totalBonos));
        nomina.setTotalDeducciones(dos(totalDeducciones));
        nomina.setTotalImpuestos(dos(totalImpuestos));
        nomina.setTotalNeto(dos(totalNeto));

        return aDTO(nominaRepository.save(nomina));
    }

    private NominaDetalle calcularNominaEmpleado(Nomina nomina, Empleado empleado, LocalDate inicio, LocalDate fin) {
        // Días reales del periodo elegido (semana, quincena o mes)
        long diasPeriodo = ChronoUnit.DAYS.between(inicio, fin) + 1;
        if (diasPeriodo < 1) diasPeriodo = 1;
        BigDecimal salarioDiario = empleado.getSalarioDiario();
        BigDecimal sueldoBase = dos(salarioDiario.multiply(BigDecimal.valueOf(diasPeriodo)));

        BigDecimal horasExtras = BigDecimal.ZERO;
        BigDecimal horasExtrasMonto = BigDecimal.ZERO;
        BigDecimal faltas = BigDecimal.ZERO;
        BigDecimal faltasDescuento = BigDecimal.ZERO;
        BigDecimal retardoDescuento = BigDecimal.ZERO;
        BigDecimal permisoSinGoceDescuento = BigDecimal.ZERO;
        BigDecimal bonos = BigDecimal.ZERO;
        BigDecimal sobrantes = BigDecimal.ZERO;
        BigDecimal otrasDeducciones = BigDecimal.ZERO;

        BigDecimal valorHora = salarioDiario.divide(BigDecimal.valueOf(8), 4, RoundingMode.HALF_UP);

        List<Incidencia> incidencias = incidenciaRepository.listarPorEmpleadoYPeriodo(empleado.getId(), inicio, fin);
        for (Incidencia incidencia : incidencias) {
            BigDecimal cantidad = incidencia.getCantidad() != null ? incidencia.getCantidad() : BigDecimal.ZERO;
            BigDecimal monto = incidencia.getMonto() != null ? incidencia.getMonto() : BigDecimal.ZERO;
            switch (incidencia.getTipo()) {
                case HORA_EXTRA_DOBLE:
                    horasExtras = horasExtras.add(cantidad);
                    horasExtrasMonto = horasExtrasMonto.add(dos(valorHora.multiply(BigDecimal.valueOf(2)).multiply(cantidad)));
                    break;
                case HORA_EXTRA_TRIPLE:
                    horasExtras = horasExtras.add(cantidad);
                    horasExtrasMonto = horasExtrasMonto.add(dos(valorHora.multiply(BigDecimal.valueOf(3)).multiply(cantidad)));
                    break;
                case FALTA:
                    faltas = faltas.add(cantidad);
                    faltasDescuento = faltasDescuento.add(dos(salarioDiario.multiply(cantidad)));
                    break;
                case RETARDO:
                    retardoDescuento = retardoDescuento.add(dos(valorHora.multiply(cantidad)));
                    break;
                case PERMISO_SIN_GOCE:
                    permisoSinGoceDescuento = permisoSinGoceDescuento.add(dos(salarioDiario.multiply(cantidad)));
                    break;
                case BONO:
                    bonos = bonos.add(dos(monto));
                    break;
                case FALTANTE:
                    otrasDeducciones = otrasDeducciones.add(dos(monto));
                    break;
                case SOBRANTE:
                    sobrantes = sobrantes.add(dos(monto));
                    break;
                default:
                    break;
            }
        }

        BigDecimal percepciones = sueldoBase.add(horasExtrasMonto).add(bonos);
        BigDecimal descuentosPorIncidencias = faltasDescuento.add(retardoDescuento).add(permisoSinGoceDescuento);
        BigDecimal totalGravado = percepciones.subtract(descuentosPorIncidencias);
        if (totalGravado.signum() < 0) {
            totalGravado = BigDecimal.ZERO;
        }
        totalGravado = dos(totalGravado);

        BigDecimal isr = calcularISR(totalGravado);
        BigDecimal seguroSocial = dos(totalGravado.multiply(SEGURO_SOCIAL_PCT));
        BigDecimal infonavit = dos(totalGravado.multiply(INFONAVIT_PCT));
        BigDecimal cuotaSindical = dos(totalGravado.multiply(CUOTA_SINDICAL_PCT));

        BigDecimal totalDeducciones = isr.add(seguroSocial).add(infonavit).add(cuotaSindical).add(otrasDeducciones);
        totalDeducciones = dos(totalDeducciones);

        BigDecimal netoPagar = totalGravado.subtract(totalDeducciones);
        if (netoPagar.signum() < 0) {
            netoPagar = BigDecimal.ZERO;
        }
        // Sobrantes: directo al bolsillo, sin ISR ni cuotas
        netoPagar = dos(netoPagar.add(sobrantes));

        return NominaDetalle.builder()
                .nomina(nomina)
                .empleado(empleado)
                .diasTrabajados(BigDecimal.valueOf(diasPeriodo))
                .sueldoBase(sueldoBase)
                .horasExtras(dos(horasExtras))
                .horasExtrasMonto(dos(horasExtrasMonto))
                .faltas(dos(faltas))
                .faltasDescuento(dos(faltasDescuento))
                .retardoDescuento(dos(retardoDescuento))
                .permisoSinGoceDescuento(dos(permisoSinGoceDescuento))
                .bonos(dos(bonos))
                .totalGravado(totalGravado)
                .isr(isr)
                .seguroSocial(seguroSocial)
                .infonavit(infonavit)
                .cuotaSindical(cuotaSindical)
                .otrasDeducciones(dos(otrasDeducciones))
                .sobrantesMonto(dos(sobrantes))
                .totalDeducciones(totalDeducciones)
                .netoPagar(netoPagar)
                .build();
    }

    @Transactional
    public NominaDTO marcarComoPagada(Long id) {
        Nomina nomina = buscar(id);
        if (nomina.getEstado() != EstadoNomina.PROCESADA) {
            throw new ValidacionException("Solo se puede pagar una nómina en estado PROCESADA");
        }
        nomina.setEstado(EstadoNomina.PAGADA);
        return aDTO(nominaRepository.save(nomina));
    }

    @Transactional
    public NominaDTO cancelar(Long id, String motivo) {
        Nomina nomina = buscar(id);
        if (nomina.getEstado() == EstadoNomina.PAGADA) {
            throw new ValidacionException("No se puede cancelar una nómina ya pagada");
        }
        if (nomina.getEstado() == EstadoNomina.CANCELADA) {
            throw new ValidacionException("La nómina ya está cancelada");
        }
        String linea = motivo != null && !motivo.isBlank() ? motivo : "Cancelación solicitada";
        nomina.setObservaciones(nomina.getObservaciones() != null
                ? nomina.getObservaciones() + "\n[CANCELADA] Motivo: " + linea
                : "[CANCELADA] Motivo: " + linea);
        nomina.setEstado(EstadoNomina.CANCELADA);
        return aDTO(nominaRepository.save(nomina));
    }
}