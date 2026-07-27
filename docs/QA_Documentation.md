# Calidad y pruebas

## Pirámide de pruebas

- Unitarias: reglas de progresión, membresías, autorizaciones, dinero, QR y validadores IA.
- Integración: API, PostgreSQL, migraciones, webhooks, colas y almacenamiento privado.
- Contrato: OpenAPI y clientes móviles/web contra respuestas versionadas.
- End-to-end: registro, primera rutina, sesión, Premium, vínculo a gimnasio, pago, check-in y vencimiento.
- Manuales: accesibilidad, idioma, dispositivos reales, conectividad intermitente y flujos de tienda.

## Escenarios críticos de aceptación

1. Un principiante de calistenia recibe una rutina con progresiones técnicas apropiadas, no una plantilla genérica de pesas.
2. Un runner con fuerza como interés secundario recibe sesiones compatibles con su volumen de carrera y disponibilidad declarada.
3. La IA rechaza o reconcilia una combinación de perfiles incompatible, explica el motivo y mantiene el perfil principal como prioridad.
4. Un usuario independiente completa una sesión y conserva los datos sin conexión al recuperar red.
5. La IA propone una rutina válida, explica la propuesta y exige confirmación antes de activarla.
6. Un usuario sin Premium no accede a una capacidad reservada, pero conserva sus datos al vencer la suscripción.
7. Un socio acepta una invitación y obtiene QR solo con membresía activa.
8. Un QR vencido, manipulado o de otra sede es rechazado y auditado.
9. Un webhook repetido no duplica pagos ni extiende dos veces una membresía.
10. Recepción no puede ver fotos, conversaciones ni datos de progreso privados.
11. Un usuario puede desvincularse de una sede sin perder sus rutinas ni historial.
