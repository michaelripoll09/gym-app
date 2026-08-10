# Correccion de sesiones Android

## Objetivo

Permitir que una persona autenticada corrija o elimine una sesion ya registrada desde su historial, para que la adherencia, la progresion y los indicadores del entrenamiento se calculen con datos fiables.

## Restricciones

- El alcance cubre solo sesiones completadas y sincronizadas; no duplica ni cambia el flujo actual de sesiones pendientes sin conexion.
- Una persona solo puede consultar, editar o eliminar sus propias sesiones; las solicitudes sobre sesiones ajenas no deben revelar informacion.
- La edicion permite corregir repeticiones, carga, esfuerzo percibido y nota, sin modificar la rutina programada ni una sesion que este activa.
- Se validan repeticiones mayores que cero, carga igual o mayor que cero, esfuerzo ausente o entre 1 y 10, y notas privadas de longitud acotada.
- La eliminacion requiere una confirmacion explicita y no ofrece restauracion en este bloque.
- Al modificar o eliminar una sesion se deben refrescar los datos locales que dependan de ella, incluidos historial, progreso, calendario y resumen semanal.

## Listo cuando

- [ ] El backend expone operaciones autenticadas para actualizar y eliminar una sesion completada, aplica propiedad por usuario y responde de forma segura ante identificadores inexistentes o ajenos.
- [ ] El backend valida el contenido corregido y conserva la integridad de los registros de series, esfuerzo y nota.
- [ ] El detalle de una sesion en Android permite iniciar la correccion, editar sus valores y muestra errores recuperables durante el guardado.
- [ ] Android ofrece eliminar una sesion desde su detalle con un dialogo de confirmacion claro y vuelve al historial al completarse la operacion.
- [ ] Tras una correccion o eliminacion, Android actualiza historial, progreso por ejercicio, calendario de adherencia y resumen semanal sin exigir reiniciar la aplicacion.
- [ ] Las pruebas de backend y Android cubren autorizacion, validaciones, correccion, eliminacion y refresco de estado, y las suites correspondientes finalizan en PASS.
