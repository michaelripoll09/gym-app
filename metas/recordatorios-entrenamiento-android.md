# Recordatorios de entrenamiento Android

## Objetivo

Ayudar a las personas autenticadas que tienen una programación semanal activa a recordar su entrenamiento previsto mediante recordatorios locales configurables en Android, para mejorar la adherencia sin depender de que la aplicación permanezca abierta.

## Restricciones

- Implementar únicamente en Android, reutilizando la programación semanal existente y notificaciones locales; no crear un servicio remoto, pagos ni modificar automáticamente rutinas o sesiones.
- Respetar el permiso de notificaciones de Android y no enviar recordatorios si la persona los desactiva o no tiene entrenamiento programado para ese día.

## Listo cuando

- [x] La aplicación permite activar o desactivar recordatorios y escoger una hora para los días con entrenamiento programado.
- [x] Android solicita el permiso de notificaciones solo cuando es necesario y comunica claramente el estado si se deniega.
- [x] Con los recordatorios activos, Android programa una notificación local para cada próximo día de entrenamiento configurado, incluso si la aplicación está cerrada.
- [x] La notificación identifica la rutina prevista y al tocarla abre el flujo de Entrenamiento de hoy sin iniciar ni modificar una sesión automáticamente.
- [x] Cambiar la hora, desactivar los recordatorios, editar una rutina o archivarla actualiza o cancela los avisos afectados sin dejar duplicados.
- [x] La interfaz incluye estados de carga, recordatorios desactivados y permiso denegado, con una acción clara para reintentar o abrir ajustes cuando corresponda.
- [x] Las pruebas Android cubren programación, cancelación, reprogramación, ausencia de entrenamiento y navegación desde la notificación; una validación en emulador confirma el flujo con una notificación de prueba.
