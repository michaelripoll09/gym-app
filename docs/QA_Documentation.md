# Calidad y pruebas

## Pirámide de pruebas

- Unitarias: reglas de progresión, membresías, autorizaciones, dinero, QR y validadores IA.
- Integración: API, PostgreSQL, migraciones, webhooks, colas y almacenamiento privado.
- Contrato: OpenAPI y clientes móviles/web contra respuestas versionadas.
- End-to-end: registro, primera rutina, sesión, Premium, vínculo a gimnasio, pago, check-in y vencimiento.
- Manuales: accesibilidad, idioma, dispositivos reales, conectividad intermitente y flujos de tienda.

## Escenarios críticos de aceptación

1. Un usuario independiente completa una sesión y conserva los datos sin conexión al recuperar red.
2. La IA propone una rutina válida, explica la propuesta y exige confirmación antes de activarla.
3. Un usuario sin Premium no accede a una capacidad reservada, pero conserva sus datos al vencer la suscripción.
4. Un socio acepta una invitación y obtiene QR solo con membresía activa.
5. Un QR vencido, manipulado o de otra sede es rechazado y auditado.
6. Un webhook repetido no duplica pagos ni extiende dos veces una membresía.
7. Recepción no puede ver fotos, conversaciones ni datos de progreso privados.
8. Un usuario puede desvincularse de una sede sin perder sus rutinas ni historial.
