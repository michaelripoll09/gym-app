# Infraestructura y despliegue

## Base operativa

- Repositorios y pipelines separados por responsabilidad, con versionado de API y migraciones de base de datos.
- CI ejecuta pruebas, análisis estático, revisión de dependencias y construcción reproducible de cada cliente.
- CD publica a staging tras aprobación; producción requiere validación manual, migración compatible y posibilidad de rollback.

## Observabilidad

- Logs estructurados sin datos sensibles.
- Métricas de latencia, tasa de error, colas, pagos, QR, consumo de IA y conversiones.
- Trazas distribuidas con identificador de solicitud.
- Alertas accionables para errores de pago, fallos de webhook, indisponibilidad de IA, aumento de rechazos QR y copias de seguridad.

## Continuidad

- Copias de PostgreSQL cifradas y pruebas periódicas de restauración.
- Versionado y política de retención para archivos.
- Objetivos de recuperación se fijarán antes de producción según presupuesto y volumen real.
