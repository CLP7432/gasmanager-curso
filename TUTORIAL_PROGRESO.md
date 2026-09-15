# GasManager - Progreso del Tutorial

## Contexto del Proyecto
- **Proyecto original (referencia):** F:\marcocarrasco.org
- **Proyecto que estamos construyendo (AQUI):** F:\gasmanager-curso
- **Objetivo:** Reconstruir el proyecto paso a paso para aprender a programar
- **Método:** El usuario teclea cada línea de código, el tutor explica qué hace cada cosa y detecta errores/buenas prácticas
- **Decisión tomada:** El Dashboard y el Sidebar muestran SOLO los módulos que tienen backend (usuarios, roles, permisos). No los 9 módulos del original (la mayoría no existen en este proyecto aún).

## Arquitectura del Sistema
- **Backend:** Spring Boot 3.1.5 + Spring Cloud 2022.0.4 + MySQL (puerto 3308)
- **Frontend:** React Vite + axios + react-router-dom + Bootstrap (instalado vía npm, NO CDN)
- **Patrón:** Microservicios (Eureka, Gateway) + JWT Auth
- **Regla:** NO copiar/pegar código. El usuario teclea todo manualmente.

## Puertos
- MySQL: 3308 (bases: gasmanager_curso_users, gasmanager_curso_clientes)
- Eureka: 8761
- Gateway: 8085 (proxy a /api/**)
- microservice-users: 8081
- microservice-clientes: 8093
- microservice-nomina: 8096
- microservice-ventas: 8095
- Frontend Vite: 5173

---

## ✅ COMPLETADO

### Backend - Contrato API (todo el módulo users compila: BUILD SUCCESS)
- [x] `UsuarioRepository` - Eliminado método roto `Long id(Long id)`
- [x] `UsuarioService` - listarActivos, listarUsuariosBloqueados, actualizarUsuario (merge parcial), desactivarUsuario (soft delete)
- [x] `UsuarioController` - 8 endpoints: activos, bloqueados, validar-token, PUT, DELETE, test, publico, requiere-reset
- [x] `RolService` - listarRolesActivos (streams), actualizarRol, eliminarRol
- [x] `RolController` - GET /activos, PUT /{id}, DELETE /{id}
- [x] `PermisoService` - actualizarPermiso (merge parcial)
- [x] `PermisoController` - PUT /{id}
- [x] `AuditoriaService` - listarPorRango (findByFechaHoraBetween)
- [x] `AuditoriaController` - GET /rango con @RequestParam + @DateTimeFormat ISO.DATE_TIME
- [x] Compilado: `mvnw -pl microservice-users compile` → BUILD SUCCESS (29 archivos)

### Backend - Auditoría cableada (el registro se genera en cada acción)
- [x] `UsuarioService` inyecta `AuditoriaService`: registra CREAR (crearUsuario), ACTUALIZAR (actualizarUsuario), ELIMINAR (desactivarUsuario), y VALIDAR en autenticar (login exitoso / login fallido - Intento #N / USUARIO BLOQUEADO a los 3 intentos)
- [x] `RolService` inyecta `AuditoriaService`: CREAR, ACTUALIZAR, ELIMINAR
- [x] `PermisoService` inyecta `AuditoriaService`: CREAR (ejecutor null → "Sistema"), ACTUALIZAR, ELIMINAR (ejecutor null)
- [x] Corrección de variables: `usuarioGuardado`/`rolGuardado`/`permisoGuardado` para evitar conflicto con el parámetro del método (no se puede redeclarar `rolActualizado`/`permisoActualizado`)
- [x] Auditoría se registra DESPUÉS del `save()` (si el guardado fallara no quedaría registro falso)
- [x] Verificado: `GET /api/auditorias` devuelve registros reales; un login incorrecto genera `401` + registro "Login fallido - Intento #1" (comportamiento correcto, no es bug)

### Backend - Seguridad (verificado con curl tras reinicio)
- [x] `Usuario.password` con `@JsonProperty(Access.WRITE_ONLY)` → el JSON de `/api/usuarios` ya NO muestra el hash (campo oculto en salida)
- [x] `application.yml`: `server.error.include-message: always` → los errores 409/404 ahora traen `message` real (ej. "No se puede eliminar el rol porque está asignado...")
- [x] `RolService.eliminarRol()` valida uso con `usuarioRepository.countByRolId(id)` → 409 si está en uso, 404 si no existe, 200 si elimina limpio
- [x] `DataInitializer` idempotente: `run()` siempre llama a crearPermisos/crearRoles/crearAdmin, cada una con guardas propias
- [x] Login verificado: `admin@gasmanager.com` / `Cambiami123!` → 200 OK

### Backend - Módulo de Nómina (microservice-nomina)
- [x] Entidades: `Puesto`, `Departamento`, `Empleado`, `EmpleadoPuestoHistorial` + enum `RiesgoPuesto`
- [x] `DataInitializer` idempotente: `crearPuestos()` / `crearDepartamentos()` / `crearEmpleados()` con guardas propias por nombre (`existsByNombre*`)
- [x] Bug arreglado: el seed guardaba `Empleado` sin `codigoEmpleado` → `Column 'codigo_empleado' cannot be null` mataba el arranque; ahora genera el código `EMP-%04d` con `do/while` + `existsByCodigoEmpleado` (mismo patrón que `EmpleadoService`)
- [x] Verificado en BD (MySQL 3308, `gasmanager_curso_nomina`): 4 empleados, 4 distintos, si reiniicias NO duplica (EMP-0001 Carlos, EMP-0002 Maria, EMP-0003 Juan, EMP-0004 Ana)
- [x] Compilado: `mvnw -pl microservice-nomina compile` → BUILD SUCCESS

### Frontend - Módulo de Nómina
- [x] `src/api/nomina/auth.js` - 5 servicios API (empleadosService, puestosService, departamentosService, incidenciasService, nominasService)
- [x] `src/pages/nomina/ModuloNominaPage.jsx` - página de submódulos (Empleados, Puestos, Departamentos, Incidencias, Nóminas)
- [x] Empleados: `EmpleadosPage.jsx` + `EmpleadosList.jsx` - CRUD con selects dinámicos de puesto/departamento, desactivar/reactivar
- [x] Puestos: `PuestosPage.jsx` + `PuestosList.jsx` - CRUD + toggle + eliminar
- [x] Departamentos: `DepartamentosPage.jsx` + `DepartamentosList.jsx` - CRUD + toggle + eliminar
- [x] Incidencias: `IncidenciasPage.jsx` + `IncidenciasList.jsx` - CRUD de faltas/retardos/horas extra/bonos con select de empleado
- [x] Nóminas: `NominasPage.jsx` + `NominasList.jsx` - procesar periodo (inicio/fin/fecha pago/obs), filtro por estado, detalle con desglose por empleado, marcar pagada, cancelar con motivo
- [x] Rutas `/nomina/**` en App.jsx + enlace "Nómina" en Sidebar (permiso NOMINA_VER) + tarjeta en Dashboard
- [x] Permiso `NOMINA_VER` agregado: seed del users DataInitializer + `INSERT` aplicado en BD (`gasmanager_curso_users.permisos`)
- [x] `npm run build` → BUILD SUCCESS (515 módulos)

### Frontend - API services
- [x] `src/api/users/auth.js` - authService (login, logout, getCurrentUser, validarToken) + usuarioService + rolService + permisoService + auditoriaService
- [x] Bugs corregidos: typo `reponse`, URL literal `{id}`, URL auditoria incompleta

### Frontend - Context

- [x] `src/contexts/AuthContext.jsx` - useAuth + AuthProvider (user, loading, error, login, logout, isAuthenticated, isAdmin)
- [x] MEJORA vs original: isAdmin = `user?.rol === 'ADMIN'` (1 comparación, no 4). El backend es la única fuente de la verdad del rol.
- [x] Bug corregido: typo `isAuthenticared` → `isAuthenticated`

### Frontend - Componentes y páginas (router completo)
- [x] `src/components/users/Login.jsx` - formulario con Bootstrap, usa useAuth().login
- [x] `src/pages/users/LoginPage.jsx` - wrapper delgado
- [x] `src/components/common/Layout.jsx` - esqueleto (Sidebar + Navbar + children). IMPORTANTE: recibe `{ children }`
- [x] `src/components/common/Sidebar.jsx` - NavLink con isActive, logout, solo módulos existentes (Dashboard, Usuarios)
- [x] `src/components/common/Navbar.jsx` - useLocation + getPageTitle (fallback 'GasManager')
- [x] `src/components/users/Dashboard.jsx` - modules.map(), onClick navigate, solo gestor de usuarios
- [x] `src/pages/users/DashboardPage.jsx` - wrapper: <Layout><Dashboard /></Layout>
- [x] `src/App.jsx` - AuthProvider > Router > AppRoutes, PrivateRoute, rutas /login /dashboard / y *
- [x] `npm run build` → BUILD SUCCESS (90 módulos) - todo el frontend compila

### Frontend - Módulo de Usuarios (CRUD completo)
- [x] `src/pages/users/UsuariosPage.jsx` + `src/components/users/UsuariosLista.jsx` - listar (filtros todos/activos/bloqueados), crear, editar, desactivar, reactivar automático al editar usuario inactivo
- [x] `src/pages/users/RolesPage.jsx` + `src/components/users/RolesLista.jsx` - listar roles con permisos, crear/editar con checkboxes de permisos, eliminar (409 si rol en uso)
- [x] `src/pages/users/PermisosPage.jsx` + `src/components/users/PermisosLista.jsx` - CRUD permiso (código, nombre, descripción), botón Eliminar solo si activo
- [x] Rutas `/usuarios`, `/roles`, `/permisos` en App.jsx (todas con PrivateRoute)
- [x] Sidebar enlaces Usuarios / Roles / Permisos (grupo ADMINISTRACIÓN)
- [x] Dashboard con 3 tarjetas navegables (Usuarios → /usuarios, Roles → /roles, Permisos → /permisos)
- [x] CRUD de Permisos verificado en navegador (crear, editar, eliminar funcionando)
- [x] `npm run build` → BUILD SUCCESS - todo el frontend compila

### Frontend - Página de Auditoría
- [x] `src/pages/users/AuditoriaPage.jsx` + `src/components/users/AuditoriaLista.jsx` - tabla de auditoría con badges por tipo (CREAR success, ACTUALIZAR warning, ELIMINAR danger, LEER/VALIDAR info)
- [x] Filtros: por ID de usuario (solo admin, usa `listarPorUsuario`), por rango de fechas (datetime-local + `${fecha}:00` para cumplir formato ISO con segundos), y Limpiar
- [x] Ruta `/auditoria` en App.jsx (con PrivateRoute) + enlace en Sidebar
- [x] Badges faltantes agregados en index.css: `.badge-info`, `.badge-secondary`
- [x] Botones neutrales con `.btn-secondary` (gris): Limpiar + 3 Cancelar (Usuarios/Roles/Permisos/Auditoría) → cambia `className="btn"` que se veía como texto plano
- [x] `npm run build` → BUILD SUCCESS (98 módulos)

### Bootstrap configurado
- [x] Instalado vía npm (bootstrap@5.3.8) en gasmanager-frontend
- [x] Limpiado instalación accidental en raíz F:\gasmanager-curso (package.json + node_modules falsos eliminados)
- [x] CDN eliminado de index.html
- [x] Importado en `src/main.jsx`: bootstrap css + bundle js, antes del index.css
- [x] IntelliSense de Bootstrap en IntelliJ: (usuario confirmó que funciona)

### Backend - Catálogo de Dispensarios (microservice-ventas) — PIEZA 1 ✅
- [x] Entidades: `Dispensario` (numero único, nombre, ubicacion, activo, despachadorId/Nombre, caras) → `CaraDispensario` (codigo A/B, nombre, activo, mangueras) → `Manguera` (codigo, nombre, tipoCombustible, combustibleId, lecturaActual, activo). Mejora vs base: solo `activo` boolean (la base duplicaba `estado`+`activo`) y sin `tieneDosCaras` (se deriva de los datos). Repetición de combustible EN LA MISMA cara es válida (ej. A1+A2 = MAGNA, o 4 mangueras Magna).
- [x] `OneToMany(mappedBy=...)` + `cascade=ALL` + `orphanRemoval=true`: un solo `save(dispensario)` persiste/borra todo el árbol; la FK vive en el hijo.
- [x] Repos: `DispensarioRepository` (findByActivoTrueOrderByIdAsc), `CaraDispensarioRepository`, `MangueraRepository` (findByActivoTrue).
- [x] DTOs: `DispensarioDTO`→`CaraDTO`→`MangueraDTO` (con id para saber qué mangueras ya existen al editar) + `AsignarDespachadorDTO`.
- [x] `DispensarioService`: listarCompletos/obtenerCompleto con `@Transactional(readOnly=true)` (evita `LazyInitializationException` porque caras/mangueras son LAZY), crearCompleto (arma el árbol enlazando padres), actualizarCompleto (con `clear()` + rebuild y **conserva lecturas** por llave `codigoCara:codigoManguera`; la base las perdía), asignarDespachador, cambiarActivo.
- [x] `DispensarioController`: `GET/POST /api/dispensarios/completo`, `PUT /completo/{id}`, `PUT /{id}/despachador`, `PUT /{id}/activo`, `GET /completos`.
- [x] Verificado: tablas creadas por `ddl-auto: update` con FKs; POST de dispensario 4 mangueras (MAGNA repetida en cara A) OK; PUT despachador persiste (el 400 inicial fue por la "é" en consola Windows, no bug).
- [x] Conceptos #35-38: `@OneToMany(mappedBy)` + `cascade/orphanRemoval`, `@Transactional(readOnly=true)` vs `LazyInitializationException`, `orphanRemoval` para rebuild, DTOs con `id` para edición.

### Backend - Turnos y Ventas con turno (microservice-ventas) — PIEZA 2 ✅
- [x] `EstadoTurno` (ABIERTO/CERRADO) + `Turno` (codigoTurno `TUR-%05d`, nombre, fechaTurno, horaInicio/Fin, estado, supervisorId/Nombre).
- [x] `TurnoRepository`: findFirstByOrderByIdDesc (para generar código), tiene listar por fecha DESC y por estado, y `existsByEstado` (guard de un solo ABIERTO).
- [x] `TurnoDTO` / `AbrirTurnoDTO` / `TurnoService` (abrir, cerrar, listar con filtro por estado) / `TurnoController` (`GET /api/turnos[?estado]`, `GET /{id}`, `POST`, `POST /{id}/cerrar`).
- [x] `Venta` ampliada: `turnoId`, `despachadorId/Nombre`, `dispensarioId`, `mangueraId` + validaciones en `registrar()`: turno requerido y ABIERTO, despachador debe coincidir con el asignado a la isla, manguera debe pertenecer al dispensario.
- [x] Gateway `msvc-ventas`: `Path=/api/ventas/**,/api/turnos/**,/api/dispensarios/**`.
- [x] Verificado: abrir turno (guard = no puede haber dos ABIERTOS), cerrar, listar por estado; venta `VEN-00019` registrada correctamente (Surtidor 10, turno 3).
- [x] Conceptos #39-41: `existsBy` como guard sobre datos existentes, patrón de generación de folio/serie (`findFirstByOrderByIdDesc`), validaciones de negocio en el service (no en el front).

### Backend - Cortes de turno con incidencia FALTANTE — PIEZA 3 ✅
- [x] `EstadoCorte` (PENDIENTE/VALIDADO/CERRADO) + `Corte` (codigoCorte `CORTE-%05d`, turno+dispensario+despachador, numeroVentas, totalLitros, totalVentas, esperado/declarado por método de pago, diferenciaEfectivo, estado, validadoPor/Fecha, createdAt con `@PrePersist`).
- [x] `CorteRepository`: existsByTurnoIdAndDispensarioId (un corte por turno+isla), listar por id DESC / por turno / por estado.
- [x] `VentaRepository`: `findByTurnoIdOrderByFechaHoraAsc` + variante con dispensario → material de `CorteService` (suma con Stream, **excluye vendas CANCELADAS**).
- [x] `RegistrarIncidenciaDTO` en el paquete **dto** (corrección del usuario: no va en clients) + `NominaClient` (Feign `@FeignClient`, `@PostMapping("/api/incidencias")`).
- [x] `CorteService`: generar (valida turno/dispensario/existente, calcula diferencia), validar (con `diferenciaEfectivo > 0` → incidencia FALTANTE en nómina), cerrar (requiere VALIDADO), listar.
- [x] `CorteController`: `GET /api/cortes[?estado]`, `GET /turno/{turnoId}`, `GET /{id}`, `POST /generar`, `POST /{id}/validar?autorizadoPor=`, `POST /{id}/cerrar`. `VentaController`: `GET /api/ventas/turno/{turnoId}`.
- [x] **Two `- Path=` clausulas en la ruta del gateway = AND** → `/api/cortes/**` devolvía 404 hasta borrar la línea vieja; solo debe quedar UNA línea con todos los patrones.
- [x] Signo de la diferencia: `diferencia = esperado - declarado`; `> 0` = entregó menos = **FALTANTE** → incidencia; `< 0` = sobrante. (El primer borrador del CorteService traía `< 0` invertido.)
- [x] El `despachadorId` persistido en ventas debe ser el `empleadoId` REAL de nómina. En la prueba, el dispensario apuntaba al id 3 (dato de demo) y nómina respondió "Empleado no encontrado con id 3" → se corrigió a `empleadoId` 30 (EMP-0003 Juan Ramirez Torres).
- [x] Verificación E2E completa: venta EFECTIVO $239.90 en turno abierto → corte con `efectivoRecibido=100` → diferencia +139.90 → **validar crea incidencia FALTANTE $139.90 en nómina** (empleado 30, referencia CORTE-00001) → cerrar. Todo vía gateway (8085).
- [x] Conceptos #42-46: Feign (`@FeignClient` = HTTP con interface `@PostMapping`), signo de diferencias y `compareTo`, transacciones entre microservicios NO existen (ventas llama a nómina pero cada uno persiste lo suyo), lista de predicates AND en Spring Cloud Gateway, y que el id referenciado entre servicios debe existir de verdad.

---

## ⏳ PENDIENTE

### Probar la app de punta a punta (backend + frontend)
Orden de arranque:
1. MySQL en puerto 3308 (debe existir BD gasmanager_curso_users)
2. microservice-eureka (8761)
3. microservice-gateway (8085)
4. microservice-users (8081)
5. Frontend: `npm run dev` (5173)
- NOTA: al momento de la última sesión NADA estaba corriendo (puertos 3308, 8761, 8085, 8081, 8093, 5173 sin uso)
- Verificar DataInitializer de users: crea datos iniciales (app.initial-data.enabled=true)

### Frontend - páginas faltantes (backend ya existe)
- [ ] Agregar estas rutas al App.jsx y al Sidebar progresivamente

### Módulos futuros (sin backend aún)
- Ventas, Inventarios, Administración completa, Facturación, Compras, Reportes, Lealtad
- (Dashboard y Sidebar muestran solo módulos con backend)

### Ventas — piezas siguientes (Pieza 4 frontend ✅ COMPLETADA 2026-09-10)
- [x] **Pieza 4 (frontend):** Catálogo Dispensarios (`DispensariosPage.jsx` + `DispensariosLista.jsx`), Turnos (`TurnosPage.jsx` + `TurnosLista.jsx` abrir/cerrar), PuntoVenta multi-isla (`PuntoVentaPage.jsx` + `PuntoVenta.jsx` litros/pesos), Cortes (`CortesPage.jsx` + `CortesLista.jsx` 816 líneas generar/validar/cerrar + `EditarCorteModal.jsx` 494 líneas con aceites/créditos).
- [x] Refinamiento Cortes 10-11 sep: `CorteService` (894 líneas, aceites + créditos + notas), `VentaService` (folio VEN-XXXXX, `obtenerPorFolio`), frontend `npm run build` → SUCCESS (534 módulos).

### Facturación — microservicio nuevo (creado 2026-09-11, verificado ✅)
- [x] `microservice-facturacion` puerto 8092, registrado en Eureka UP, ruta gateway `/api/facturas/**,/api/clientes-fiscales/**`, módulo agregado al `pom.xml` padre.
- [x] Entidades `Factura`, `FacturaConcepto`, `ClienteFiscal` + enums `EstadoFactura`, `OrigenConceptoFactura`; Feign `VentasClient` (`GET /api/ventas/folio/{folio}` verificado VEN-00001 OK) + `ClientesClient` (`GET /api/notas-credito/cliente/{clienteId}` verificado OK).
- [x] `FacturaController`: listar, obtener, por-cliente, disponibles, facturar, cancelar, xml, pdf, enviar-correo (simulación). `GET /api/facturas` vía gateway → `[]` OK; `disponibles/1` → `{"notas":[]}` OK (solo ACTIVA, la NOTA-20260911 es PAGADA).
- [x] Verificado 2026-09-11 sin reiniciar app: `./mvnw compile` → BUILD SUCCESS (8/8 módulos), `npm run build` frontend → SUCCESS (534 módulos).
- [x] Frontend facturación prueba (2026-09-11, con kernel): `src/api/facturacion/auth.js` (facturasService + clientesFiscalesService), `components/facturacion/FacturasList.jsx` (crear con TICKET/NOTA/MANUAL_ACEITE, detalle, XML/PDF blob, cancelar, correo simulado) + `ClientesFiscalesList.jsx` (modo Cliente registrado con autocompletado + modo Cliente nuevo en un solo guardado, RFC/razón/C.P./correo jalados del módulo clientes), `pages/facturacion/` (Modulo + Facturas + ClientesFiscales), rutas `/facturacion/**` + Sidebar + Dashboard. `npm run build` → SUCCESS (540 módulos).
- [x] Ciclo pago→factura (2026-09-11): `LiquidarNotas` muestra panel de éxito con [Facturar ahora] que abre `/facturacion/facturas` con el formulario abierto, cliente fiscal elegido y conceptos NOTA_CREDITO con desglose combustible/aceite + subtotal/IVA/total visibles; card `Notas por facturar` en el módulo recupera pagos pendientes (ACTIVA o PAGADA no facturada) para facturar después; backend acepta notas ACTIVA o PAGADA (el guard `yaFacturado` evita doble facturación). NOTA: reiniciar solo `microservice-facturacion` (8092) para que aplique el cambio de validación (ACTIVA/PAGADA + duplicados por renglón) y el endpoint `GET /api/facturas/emisor`.
- [x] Vista imprimible de factura (2026-09-11): `FacturaVista.jsx` (representación impresa con emisor/receptor/conceptos/totales/UUID + botones Imprimir con CSS `@media print`, Exportar PDF y Enviar correo); botón Vista en la tabla de facturas.
- [x] Precios con IVA incluido (2026-09-11): el P. unit. del renglón viaja NETO sin IVA a 4 decimales (como el concepto impreso); los conceptos TICKET se anclan al subtotal/IVA del ticket a prorrata (último absorbe redondeo) para total exacto. Requiere reiniciar microservice-facturacion (8092).
- [x] Asistente IA en todos los módulos (2026-09-12, adaptado de F:/marcocarrasco.org/microservice-ia y mejorado): `microservice-ia` (8097, Ollama local qwen2.5-coder:7b —sin API keys, offline— en vez de Gemini), contexto de DATOS REALES por módulo (ventas/turno, créditos, stock bajo, incidencias, facturas), ruta gateway `/api/ia/**`, botón flotante `AsistenteIA` en el Layout con sugerencias por módulo. `mvnw compile` 9/9 + frontend SUCCESS. Falta: levantar 8097, reiniciar gateway (8085) y recargar frontend.
- [x] Lealtad: puntos por ticket (2026-09-12, adaptado de F:/marcocarrasco.org/microservice-lealtad y mejorado): `microservice-lealtad` (8098, BD gasmanager_curso_lealtad), programa único activable con vigencia y puntos por litro, acumular nunca falla sin programa (0 puntos), idempotente por ticket; `PuntoVenta` acumula best-effort y el ticket imprime ⭐ puntos solo si hay programa; módulo frontend (programas + cuentas) con rutas `/lealtad/**`. `mvnw compile` + frontend SUCCESS. Falta: levantar 8098, reiniciar gateway y recargar frontend.
- [x] Precio compra vs venta separados (2026-09-12): la compra actualizaba `precioActual` (pisaba la venta con el costo). Nuevo campo `precioCompra` (lo actualiza cada compra + historial limpio solo de venta); compra prellena con costo, catálogo y Cambios de precio muestran Compra/Venta/Margen. Requiere reiniciar microservice-inventarios (8094) y corregir precios de venta actuales.
- [x] Liquidar descuenta el saldo (2026-09-12): modelo revolvente coherente (disponible = límite − deuda de notas no pagadas; al pagar se restaura y la línea sigue ACTIVA) en `listarActivosConSaldo`, `registrarAbono`, `liquidarNotas`, `aDTO` y corrección automática al arrancar, sin botones. Requiere reiniciar microservice-clientes (8093).
- [x] Dockerización producción (2026-09-13, basado en referencia y mejorado): Dockerfiles multi-stage por servicio + nginx, `docker-compose.yml` con MySQL persistente (volumen + script init de las 7 BDs), healthchecks y `depends_on`, BDs renombradas `gasmanager_*` (sin curso) en ymls + reinicio, URLs entre servicios y eureka por env (Feign ya no trae localhost quemado; se corrigió eureka `/doc` de clientes), `compose config` válido, `mvnw compile` 10/10. LEVANTADO 2026-09-13: 9/9 en Eureka, login admin OK, frontend en :80. (mysql-curso detenido, conservado con tus datos viejos).
- [x] Reportes sin microservicio (2026-09-13, guiado de la base): `api/reportes` agrega ventas/compras/créditos/facturas en frontend + CSV; Resumen (tarjetas + margen), Ventas (filtros + gráfica por día) y Compras; rutas `/reportes/**`. Redesplegado en docker.
- [x] Reinicio ampliado (2026-09-11): `AdminResetService` ahora borra `gasmanager_curso_facturacion` (factura_conceptos, facturas, clientes_fiscales) con AUTO_INCREMENT=1 y en próximos IDs + `ANALYZE TABLE` para que MySQL 8 no muestre IDs viejos cacheados. Verificado: clientes/ventas en [] y nuevo registro arranca en ID 1 / CLI-0001. Usuarios/roles/permisos y catálogos (dispensarios, aceites, combustibles, puestos) se conservan como antes. Requiere reiniciar microservice-ventas (8095) y pulsar Reiniciar una vez más.
- [x] Surtidor de aceites sin despachador + islas se liberan al cerrar turno (2026-09-11): crear surtidor ya no arrastra el despachador de la isla (campo eliminado del formulario y del payload); `TurnoService.cerrar` limpia `despachadorId/Nombre` de todas las islas (el vínculo solo vive durante el turno y se reasigna al abrir el siguiente). Dispensarios siguen fuera del reinicio (pendiente al final).
- [x] Incidencias FALTANTE/SOBRANTE por corte (2026-09-12): validar genera FALTANTE (>0) o SOBRANTE (<0) al despachador; el despachador sale de las ventas del turno (no del vínculo vivo) y se bloquea generar/validar sin despachador; sin botón Nueva (solo automáticas). OJO: la columna era ENUM nativo sin SOBRANTE → se convirtió a VARCHAR (los ENUM nativos no admiten valores nuevos con ddl-auto:update). Sin reinicios pendientes.
- [x] Corte busca aceites por isla, no por despachador (2026-09-11): `aceitesCorteDelSurtidor` y el cálculo del corte usan `findByDispensarioIdAndActivoTrue` (antes por despachador, que quedaba nulo y dejaba recibidos/sobrante en cero). Requiere reiniciar microservice-ventas (8095).
- [ ] Pendiente facturación: `CorreoService` en simulación (falta SMTP); Feign usa URL fija localhost (8095/8093) en vez de `lb://`.

### Ventas — decisiones de diseño tomadas
- El despachador se asigna POR ISLA (persistido en el dispensario), mismo patrón que la base (`PUT /dispensarios/{id}/despachador`).
- El simulador del frontend vende por **litros o pesos** (pesos → litros = pesos/precioLitro); la venta siempre guarda `cantidad = litros`.
- El corte por ahora NO usa lecturas manuales: `esperado = Σ ventas del turno`, el supervisor declara lo recibido, y la diferencia dispara la incidencia `FALTANTE` en nómina. (Cuando exista hardware se agrega la lectura física del contador.)

---

## Estructura del frontend (estado actual)

```
gasmanager-frontend/src/
  api/users/auth.js                          ✅ COMPLETO
  contexts/AuthContext.jsx                   ✅ COMPLETO
  components/
    users/Login.jsx                          ✅ COMPLETO
    users/Dashboard.jsx                      ✅ COMPLETO (3 tarjetas navegables)
    users/UsuariosLista.jsx                  ✅ COMPLETO (CRUD + reactivar)
    users/RolesLista.jsx                     ✅ COMPLETO (CRUD + permisos)
    users/PermisosLista.jsx                  ✅ COMPLETO (CRUD)
    users/AuditoriaLista.jsx                 ✅ COMPLETO (tabla + filtros)
    common/Layout.jsx                        ✅ COMPLETO
    common/Sidebar.jsx                       ✅ COMPLETO (Dashboard, Usuarios, Roles, Permisos, Auditoría)
    common/Navbar.jsx                        ✅ COMPLETO
  pages/
    users/LoginPage.jsx                      ✅ COMPLETO
    users/DashboardPage.jsx                  ✅ COMPLETO
    users/UsuariosPage.jsx                   ✅ COMPLETO
    users/RolesPage.jsx                      ✅ COMPLETO
    users/PermisosPage.jsx                   ✅ COMPLETO
    users/AuditoriaPage.jsx                  ✅ COMPLETO
    (LoginPage.jsx solía estar en pages/ raíz - se movió a pages/users/)
  index.css                                 ✅ COMPLETO (login, layout, sidebar, navbar, cards, tablas, badges incl. info/secondary, botones incl. secondary, checklist, animaciones, responsive)
  App.jsx                                   ✅ COMPLETO (rutas /usuarios /roles /permisos /auditoria)
  main.jsx                                  ✅ COMPLETO (bootstrap importado)
  index.html                                ✅ COMPLETO (CDN eliminado)
```

## Conceptos clave enseñados
1. **API Contract** - Frontend y backend acuerdan URLs exactas. Segmento de más/menos = 404.
2. **Derived query methods** - Spring genera SQL desde el nombre del método del Repository (findBy..., existsBy...).
3. **Architecture en capas** - Controller (HTTP) → Service (lógica) → Repository (datos). El service no sabe SQL.
4. **JWT + Interceptor** - Token en header Authorization, interceptor agrega automáticamente.
5. **Soft delete** - desactivar (activo=false) en vez de borrar, conserva historial.
6. **PUT parcial / merge** - solo pisa campos != null.
7. **ResponseStatusException vs try/catch** - dos formas de manejar errores HTTP.
8. **Streams Java** - findAll().stream().filter(...).toList() vs bucle for.
9. **HTTP status codes** - 200, 401, 404, 409 y cuándo usar cada uno.
10. **Template literals** - backticks `${var}` para URLs dinámicas.
11. **localStorage + JSON.parse/stringify** - persistencia navegador.
12. **Context API React** - createContext + Provider + useContext/useAuth custom hook.
13. **Composición con children** - <Layout><Dashboard/></Layout>, Layout recibe { children }.
14. **NavLink + isActive** - resaltar menú según ruta actual.
15. **useLocation + getPageTitle** - título dinámico según ruta, con fallback.
16. **Array.map() para listas** - renderizar listas con key.
17. **PrivateRoute** - proteger rutas: isAuthenticated ? children : <Navigate to="/login">.
18. **CORS** - navegador bloquea orígenes cruzados; backend permite localhost:5173.
19. **Bootstrap vía npm** - importar CSS+JS en main.jsx (producción), no CDN.
20. **CDN vs npm** - diferencia (funciona vs IntelliSense + offline).
21. **Databinding en formularios** - input tipo text con `value={estado.campo}` + `onChange` que actualiza con spread `{...estado, campo: valor}`.
22. **Doble estado para alta/edición** - `editandoId` para saber si se actualiza (PUT) o crea (POST) + estado para el formulario.
23. **checkbox-list** - checkbox del permiso con toggle (agrega/remueve del array `permisosSeleccionados`).
24. **Contrato de campo de entidad** - el backend devuelve `permiso.id` (no `idPermiso` como el original); el frontend debe usar el campo exacto.
25. **Uso de `asíncrono` (async/await)** - cargar datos con setLoading(true/false) alrededor del try/catch.
26. **Cableado (wiring)** - un método/servicio existe pero no escribe hasta que otro lo inyecta y lo llama; `AuditoriaService.registrar()` no servía hasta inyectarlo en cada service.
27. **Inyección por constructor** - `@RequiredArgsConstructor` + campo `final` hace que Spring inyecte sin `new`.
28. **Colisión de nombres** - no se puede redeclarar el parámetro del método (`rolActualizado` ya existe) → usar nombre distinto (`rolGuardado`).
29. **Orden de auditoría vs persistencia** - registrar DESPUÉS del `save()` para no dejar registro falso.
30. **`datetime-local` + contrato ISO** - el input del navegador da `YYYY-MM-DDThh:mm`; el backend `@DateTimeFormat(ISO.DATE_TIME)` exige segundos → concatenar `:00`.
31. **`getTipoBadge` (mapa clase→CSS)** - diccionario que traduce un enum a clases `.badge-*` para colorear según el tipo.
32. **`401` ≠ bug** - login con password incorrecta responde 401 (contrato HTTP); el frontend lo muestra como error normal.
33. **Clase base `.btn` sin fondo** - `.btn` plano no define `background-color`; usar `.btn-secondary` (u otra variante) para que se vea como botón.
34. **Seed idempotente + columnas NOT NULL** - el `DataInitializer` debe poder correrse varias veces (guardas `existsBy*`) PERO también llenar los campos obligatorios: el código único se genera igual que en el service (cuenta + `do/while` + `existsByCodigoEmpleado`).

## Notas técnicas para la contraparte
- IDs son `Long`, no `Integer` (a diferencia del original).
- El enum de auditoría es `TipoAccion` (no `TipoAcccion` como el original que tiene typo).
- `SecurityConfig` usa `anyRequest().permitAll()` (no bloquea). La validación JWT es manual en los controllers/services.
- CORS permite localhost:5173/5174/5175.
- La entidad `Rol` y `Usuario` tienen campo `Boolean activo`.
- El frontend usa Bootstrap con clases tipo `container`, `card`, `form-control`, `btn btn-primary`, etc.
- El usuario confirmó que el autocompletado de Bootstrap ya funciona en IntelliJ.
- `DataInitializer` es idempotente (cada guarda es independiente), el `run()` no tiene condiciones globales.
- Errores 400/404/409 del backend viajan al frontend con `error.response?.data?.message` gracias a `include-message: always`.
- `AuditoriaService.registrar(idUsuarioEjecutor, TipoAccion, descripcion, modulo, origen)` es el helper único; los servicios no saben quién es el usuario logueado (ejecutor a veces es el id del recurso afectado o `null` → se muestra "Sistema"). Base de auditoría: `AuditoriaAccion` con campos `id, idUsuarioEjecutor, tipoAccion, descripcion, moduloAfectado, fechaHora, datosAnteriores, datosNuevos, origen`.
- El badge `VALIDAR/LEER` usa `badge-info`; `CREAR/ACTUALIZAR/ELIMINAR` usan `badge-success/warning/danger`. El `401` del login = password incorrecta (verificado en auditoría), no un fallo.

## Archivos originales de referencia (NO TOCAR, solo leer)
- F:\marcocarrasco.org\gasmanager-frontend\src\...
- F:\marcocarrasco.org\microservice-users\src\main\java\com\gasmanager\users\...
```
