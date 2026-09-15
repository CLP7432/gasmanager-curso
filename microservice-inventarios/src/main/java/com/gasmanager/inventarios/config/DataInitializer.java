package com.gasmanager.inventarios.config;

import com.gasmanager.inventarios.entities.Aceite;
import com.gasmanager.inventarios.repositories.AceiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AceiteRepository aceiteRepository;

    @Value("${app.initial-data.enabled:true}")
    private boolean initialDataEnabled;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (!initialDataEnabled) return;

        System.out.println("=====INICIALIZANDO PRODUCTOS DE INVENTARIO=====");
        crearAceites();
        System.out.println("=====INICIALIZACION COMPLETADA=====");
    }

    private void crearAceites() {
        List<Aceite> aceites = List.of(
                producto("ACE-0001", "Aceite de Motor 20W-50", null, "MOTOR", 12, "1 L", "Mineral multigrado para motor a gasolina/diesel"),
                producto("ACE-0002", "Aceite de Motor 15W-40", null, "MOTOR", 12, "1 L", "Heavy Duty para motores diesel"),
                producto("ACE-0003", "Aceite de Motor 10W-30", null, "MOTOR", 12, "1 L", "Semisintético para uso ligero"),
                producto("ACE-0004", "Aceite de Motor 10W-40", null, "MOTOR", 12, "1 L", "Semisintético multigrado"),
                producto("ACE-0005", "Aceite de Motor 5W-30", null, "MOTOR", 12, "1 L", "Sintético para motores modernos"),
                producto("ACE-0006", "Aceite de Transmisión ATF", null, "TRANSMISION", 12, "1 L", "Líquido automático para transmisión"),
                producto("ACE-0007", "Aceite Hidráulico", null, "HIDRAULICO", 12, "1 L", "Para sistemas hidráulicos"),
                producto("ACE-0008", "Líquido de Frenos DOT 3", null, "FRENOS", 12, "500 ml", "Para sistemas de frenos hidráulicos"),
                producto("ACE-0009", "Líquido de Frenos DOT 4", null, "FRENOS", 12, "500 ml", "Para sistemas ABS"),
                producto("ACE-0010", "Aditivo para Combustible", null, "ADITIVO", 12, "473 ml", "Limpia y mejora la combustión"),
                producto("ACE-0011", "Aditivo Limpia Inyectores", null, "ADITIVO", 12, "250 ml", "Limpieza de inyectores"),
                producto("ACE-0012", "Agua Destilada para Baterías", null, "AGUA", 12, "1 L", "Para baterías de plomo-ácido")
        );
        aceites.stream()
                .filter(a -> aceiteRepository.findByCodigo(a.getCodigo()).isEmpty())
                .forEach(a -> {
                    aceiteRepository.save(a);
                    System.out.println("Producto: " + a.getCodigo() + " - " + a.getNombre());
                });
    }

    private Aceite producto(String codigo, String nombre, String marca, String tipoAceite,
                            Integer unidadesPorCaja, String presentacion, String descripcion) {
        Aceite aceite = new Aceite();
        aceite.setCodigo(codigo);
        aceite.setNombre(nombre);
        aceite.setMarca(marca);
        aceite.setTipoAceite(tipoAceite);
        aceite.setUnidadesPorCaja(unidadesPorCaja);
        aceite.setPresentacion(presentacion);
        aceite.setDescripcion(descripcion);
        aceite.setPrecioCompra(BigDecimal.ZERO);
        aceite.setPrecioVenta(BigDecimal.ZERO);
        aceite.setStockActual(0);
        aceite.setStockMinimo(5);
        aceite.setStockMaximo(50);
        aceite.setActivo(true);
        return aceite;
    }
}