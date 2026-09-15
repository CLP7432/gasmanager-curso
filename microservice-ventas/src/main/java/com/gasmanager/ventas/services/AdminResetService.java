package com.gasmanager.ventas.services;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminResetService {

    private final JdbcTemplate jdbcTemplate;

    private static final List<String> SQL_RESET = Arrays.asList(
            "DELETE FROM gasmanager_ventas.venta_detalles",
            "DELETE FROM gasmanager_ventas.ventas",
            "DELETE FROM gasmanager_ventas.corte_aceites",
            "DELETE FROM gasmanager_ventas.cortes",
            "DELETE FROM gasmanager_ventas.entregas_isla",
            "DELETE FROM gasmanager_ventas.surtidor_aceites_items",
            "DELETE FROM gasmanager_ventas.surtidor_aceites",
            "DELETE FROM gasmanager_ventas.turnos",
            "DELETE FROM gasmanager_ventas.mangueras",
            "DELETE FROM gasmanager_ventas.caras_dispensario",
            "DELETE FROM gasmanager_ventas.dispensarios",
            "DELETE FROM gasmanager_inventarios.compra_detalles",
            "DELETE FROM gasmanager_inventarios.compras",
            "DELETE FROM gasmanager_inventarios.precios_historicos",
            "DELETE FROM gasmanager_inventarios.proveedores",
            "UPDATE gasmanager_inventarios.aceites SET stock_actual = 0",
            "DELETE FROM gasmanager_inventarios.tanques",
            "DELETE FROM gasmanager_inventarios.combustibles",
            "DELETE FROM gasmanager_clientes.abonos_credito",
            "DELETE FROM gasmanager_clientes.creditos",
            "DELETE FROM gasmanager_clientes.items_nota_credito",
            "DELETE FROM gasmanager_clientes.notas_credito",
            "DELETE FROM gasmanager_clientes.clientes",
            "DELETE FROM gasmanager_users.auditorias",
            "DELETE FROM gasmanager_users.password_reset_tokens",
            "DELETE FROM gasmanager_users.sesiones_usuarios",
            "DELETE FROM gasmanager_nomina.empleados_puesto_historial",
            "DELETE FROM gasmanager_nomina.nominas_detalle",
            "DELETE FROM gasmanager_nomina.nominas",
            "DELETE FROM gasmanager_nomina.incidencias",
            "DELETE FROM gasmanager_nomina.empleados",
            "DELETE FROM gasmanager_facturacion.factura_conceptos",
            "DELETE FROM gasmanager_facturacion.facturas",
            "DELETE FROM gasmanager_facturacion.clientes_fiscales",
            "DELETE FROM gasmanager_lealtad.transacciones_puntos",
            "DELETE FROM gasmanager_lealtad.cuentas_puntos",
            "DELETE FROM gasmanager_lealtad.programas"
    );

    private static final List<String> SQL_AUTO_INCREMENT = Arrays.asList(
            "ALTER TABLE gasmanager_ventas.ventas AUTO_INCREMENT = 1",
            "ALTER TABLE gasmanager_ventas.venta_detalles AUTO_INCREMENT = 1",
            "ALTER TABLE gasmanager_ventas.cortes AUTO_INCREMENT = 1",
            "ALTER TABLE gasmanager_ventas.corte_aceites AUTO_INCREMENT = 1",
            "ALTER TABLE gasmanager_ventas.entregas_isla AUTO_INCREMENT = 1",
            "ALTER TABLE gasmanager_ventas.surtidor_aceites AUTO_INCREMENT = 1",
            "ALTER TABLE gasmanager_ventas.surtidor_aceites_items AUTO_INCREMENT = 1",
            "ALTER TABLE gasmanager_ventas.turnos AUTO_INCREMENT = 1",
            "ALTER TABLE gasmanager_ventas.mangueras AUTO_INCREMENT = 1",
            "ALTER TABLE gasmanager_ventas.caras_dispensario AUTO_INCREMENT = 1",
            "ALTER TABLE gasmanager_ventas.dispensarios AUTO_INCREMENT = 1",
            "ALTER TABLE gasmanager_inventarios.compras AUTO_INCREMENT = 1",
            "ALTER TABLE gasmanager_inventarios.compra_detalles AUTO_INCREMENT = 1",
            "ALTER TABLE gasmanager_inventarios.precios_historicos AUTO_INCREMENT = 1",
            "ALTER TABLE gasmanager_inventarios.proveedores AUTO_INCREMENT = 1",
            "ALTER TABLE gasmanager_inventarios.tanques AUTO_INCREMENT = 1",
            "ALTER TABLE gasmanager_inventarios.combustibles AUTO_INCREMENT = 1",
            "ALTER TABLE gasmanager_clientes.clientes AUTO_INCREMENT = 1",
            "ALTER TABLE gasmanager_clientes.creditos AUTO_INCREMENT = 1",
            "ALTER TABLE gasmanager_clientes.abonos_credito AUTO_INCREMENT = 1",
            "ALTER TABLE gasmanager_clientes.notas_credito AUTO_INCREMENT = 1",
            "ALTER TABLE gasmanager_clientes.items_nota_credito AUTO_INCREMENT = 1",
            "ALTER TABLE gasmanager_users.auditorias AUTO_INCREMENT = 1",
            "ALTER TABLE gasmanager_users.password_reset_tokens AUTO_INCREMENT = 1",
            "ALTER TABLE gasmanager_users.sesiones_usuarios AUTO_INCREMENT = 1",
            "ALTER TABLE gasmanager_nomina.empleados AUTO_INCREMENT = 1",
            "ALTER TABLE gasmanager_nomina.incidencias AUTO_INCREMENT = 1",
            "ALTER TABLE gasmanager_nomina.nominas AUTO_INCREMENT = 1",
            "ALTER TABLE gasmanager_nomina.nominas_detalle AUTO_INCREMENT = 1",
            "ALTER TABLE gasmanager_nomina.empleados_puesto_historial AUTO_INCREMENT = 1",
            "ALTER TABLE gasmanager_facturacion.factura_conceptos AUTO_INCREMENT = 1",
            "ALTER TABLE gasmanager_facturacion.facturas AUTO_INCREMENT = 1",
            "ALTER TABLE gasmanager_facturacion.clientes_fiscales AUTO_INCREMENT = 1",
            "ALTER TABLE gasmanager_lealtad.transacciones_puntos AUTO_INCREMENT = 1",
            "ALTER TABLE gasmanager_lealtad.cuentas_puntos AUTO_INCREMENT = 1",
            "ALTER TABLE gasmanager_lealtad.programas AUTO_INCREMENT = 1"
    );

    // MySQL 8 cachea las estadísticas de information_schema hasta por 24h:
    // sin ANALYZE, "próximos IDs" seguiría mostrando los valores viejos
    // aunque las tablas ya estén vacías y en 1.
    private static final List<String> SQL_ANALYZE = Arrays.asList(
            "ANALYZE TABLE gasmanager_ventas.venta_detalles",
            "ANALYZE TABLE gasmanager_ventas.ventas",
            "ANALYZE TABLE gasmanager_ventas.corte_aceites",
            "ANALYZE TABLE gasmanager_ventas.cortes",
            "ANALYZE TABLE gasmanager_ventas.entregas_isla",
            "ANALYZE TABLE gasmanager_ventas.surtidor_aceites",
            "ANALYZE TABLE gasmanager_ventas.surtidor_aceites_items",
            "ANALYZE TABLE gasmanager_ventas.turnos",
            "ANALYZE TABLE gasmanager_ventas.mangueras",
            "ANALYZE TABLE gasmanager_ventas.caras_dispensario",
            "ANALYZE TABLE gasmanager_ventas.dispensarios",
            "ANALYZE TABLE gasmanager_inventarios.compra_detalles",
            "ANALYZE TABLE gasmanager_inventarios.compras",
            "ANALYZE TABLE gasmanager_inventarios.precios_historicos",
            "ANALYZE TABLE gasmanager_inventarios.proveedores",
            "ANALYZE TABLE gasmanager_inventarios.combustibles",
            "ANALYZE TABLE gasmanager_inventarios.aceites",
            "ANALYZE TABLE gasmanager_inventarios.tanques",
            "ANALYZE TABLE gasmanager_inventarios.combustibles",
            "ANALYZE TABLE gasmanager_clientes.abonos_credito",
            "ANALYZE TABLE gasmanager_clientes.creditos",
            "ANALYZE TABLE gasmanager_clientes.items_nota_credito",
            "ANALYZE TABLE gasmanager_clientes.notas_credito",
            "ANALYZE TABLE gasmanager_clientes.clientes",
            "ANALYZE TABLE gasmanager_users.auditorias",
            "ANALYZE TABLE gasmanager_users.password_reset_tokens",
            "ANALYZE TABLE gasmanager_users.sesiones_usuarios",
            "ANALYZE TABLE gasmanager_nomina.empleados",
            "ANALYZE TABLE gasmanager_nomina.incidencias",
            "ANALYZE TABLE gasmanager_nomina.nominas",
            "ANALYZE TABLE gasmanager_nomina.nominas_detalle",
            "ANALYZE TABLE gasmanager_nomina.empleados_puesto_historial",
            "ANALYZE TABLE gasmanager_facturacion.factura_conceptos",
            "ANALYZE TABLE gasmanager_facturacion.facturas",
            "ANALYZE TABLE gasmanager_facturacion.clientes_fiscales",
            "ANALYZE TABLE gasmanager_lealtad.transacciones_puntos",
            "ANALYZE TABLE gasmanager_lealtad.cuentas_puntos",
            "ANALYZE TABLE gasmanager_lealtad.programas"
    );

    public void resetBase() {
        jdbcTemplate.execute((ConnectionCallback<Void>) con -> {
            boolean auto = con.getAutoCommit();
            con.setAutoCommit(false);
            try (Statement st = con.createStatement()) {
                st.execute("SET FOREIGN_KEY_CHECKS=0");
                for (String sql : SQL_RESET) {
                    st.execute(sql);
                }
                for (String sql : SQL_AUTO_INCREMENT) {
                    st.execute(sql);
                }
                for (String sql : SQL_ANALYZE) {
                    st.execute(sql);
                }
                st.execute("SET FOREIGN_KEY_CHECKS=1");
                con.commit();
            } catch (Exception e) {
                try {
                    con.rollback();
                } catch (Exception ignored) {
                }
                throw new RuntimeException("Error al reiniciar la base de datos: " + e.getMessage(), e);
            } finally {
                try {
                    con.setAutoCommit(auto);
                } catch (Exception ignored) {
                }
            }
            return null;
        });
    }

    public List<Map<String, Object>> proximosIds() {
        String sql = """
                SELECT TABLE_SCHEMA AS base, TABLE_NAME AS tabla, AUTO_INCREMENT AS proximo_id
                FROM information_schema.TABLES
                WHERE (TABLE_SCHEMA, TABLE_NAME) IN (
                   ('gasmanager_ventas','turnos'),('gasmanager_ventas','ventas'),('gasmanager_ventas','cortes'),
                   ('gasmanager_ventas','dispensarios'),('gasmanager_ventas','caras_dispensario'),('gasmanager_ventas','mangueras'),
                  ('gasmanager_ventas','surtidor_aceites'),('gasmanager_ventas','surtidor_aceites_items'),
                  ('gasmanager_ventas','entregas_isla'),('gasmanager_ventas','venta_detalles'),('gasmanager_ventas','corte_aceites'),
                  ('gasmanager_inventarios','compras'),('gasmanager_inventarios','compra_detalles'),
                  ('gasmanager_inventarios','proveedores'),('gasmanager_inventarios','precios_historicos'),
                  ('gasmanager_inventarios','aceites'),('gasmanager_inventarios','tanques'),('gasmanager_inventarios','combustibles'),
                  ('gasmanager_clientes','clientes'),('gasmanager_clientes','creditos'),('gasmanager_clientes','abonos_credito'),
                  ('gasmanager_clientes','notas_credito'),('gasmanager_clientes','items_nota_credito'),
                  ('gasmanager_users','usuarios'),('gasmanager_users','roles'),('gasmanager_users','permisos'),
                   ('gasmanager_nomina','empleados'),('gasmanager_nomina','nominas'),('gasmanager_nomina','puestos'),('gasmanager_nomina','departamentos'),
                   ('gasmanager_facturacion','facturas'),('gasmanager_facturacion','factura_conceptos'),('gasmanager_facturacion','clientes_fiscales'),
                   ('gasmanager_lealtad','programas'),('gasmanager_lealtad','cuentas_puntos'),('gasmanager_lealtad','transacciones_puntos')
                )
                ORDER BY base, tabla
                """;
        return jdbcTemplate.queryForList(sql);
    }
}