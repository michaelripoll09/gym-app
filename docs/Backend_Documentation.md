# Backend

## API

API REST bajo `/api/v1`, con OpenAPI como contrato publicado. El servidor valida toda entrada, devuelve errores consistentes y no expone detalles internos.

## Autenticación y autorización

- Registro con correo y contraseña; diseño extensible para inicio con Apple y Google.
- Tokens de acceso de corta duración y renovación revocable.
- Roles B2B: propietario, administrador, recepción y entrenador.
- Las reglas de organización/sede se verifican en cada consulta y mutación.

## Reglas esenciales

- Un `User` puede existir sin gimnasio y tener varios vínculos históricos a sedes.
- Un vínculo activo define la membresía y habilita QR; no concede acceso automático a datos privados de entrenamiento.
- Cada QR es de vida corta, firmado y de un solo contexto de sede; el acceso se valida siempre en servidor.
- Los webhooks de pago son idempotentes y verifican firma antes de afectar una membresía.
- Toda mutación administrativa relevante genera un evento de auditoría con actor, fecha, entidad y antes/después resumido.

## Procesos asíncronos

Un worker procesa notificaciones, vencimientos, conciliación, análisis de IA, reintentos controlados y limpieza de datos. Los eventos incluyen una clave de idempotencia y una cola de fallidos revisable.

## Contratos principales

- `POST /workout-plans/generate`: solicita propuesta IA; no activa una rutina.
- `POST /workouts/{id}/sessions`: inicia y registra una sesión.
- `POST /gym-links/accept`: acepta vínculo de gimnasio.
- `GET /access-qr`: emite QR dinámico al socio autorizado.
- `POST /check-ins/scan`: valida QR y registra intento de acceso.
- `POST /payment-webhooks/{provider}`: recibe eventos autenticados del proveedor.
