# Plataforma para dueños de gimnasio

## Roles

| Rol | Capacidades |
|---|---|
| Propietario | sedes, facturación B2B, configuración, equipo, reportes y auditoría |
| Administrador | socios, membresías, planes, pagos, reportes y equipo según sede |
| Recepción | búsqueda de socios, escaneo QR, check-in y registro de pagos autorizado |
| Entrenador | consulta de socios asignados y datos que cada socio haya compartido |

## Módulos

- Onboarding de organización y sedes.
- Gestión de equipo y permisos.
- Directorio de socios, invitaciones y estados.
- Planes de membresía, renovaciones, congelaciones y vencimientos.
- Caja: pagos manuales, pagos online, comprobantes y conciliación.
- Asistencia: escáner QR, historial y motivos de rechazo.
- Reportes: membresías activas/vencidas, ingresos, métodos de pago, asistencia y retención.
- Auditoría de operaciones administrativas.

## Reglas operativas

- Una sede no puede consultar ni modificar datos de otra sede salvo rol habilitado explícitamente.
- Un QR válido no reemplaza la validación en backend de una membresía activa.
- Toda modificación de pago o membresía conserva actor y motivo.
- Los reportes aplican filtros por rango de fecha, organización y sede; las cifras financieras se calculan desde movimientos conciliados.
