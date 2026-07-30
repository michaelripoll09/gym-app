# Edicion de rutinas Android

## Objetivo

Permitir que una persona autenticada abra y actualice una rutina existente desde Android para ajustar su nombre, dias, ejercicios, series, repeticiones y descanso conforme evoluciona su entrenamiento.

## Restricciones

- La actualizacion autenticada solo puede modificar rutinas pertenecientes a la persona autenticada.
- Reutilizar el editor y las validaciones existentes; no crear una segunda experiencia de edicion.
- Mantener fuera de alcance eliminacion, duplicacion, plantillas, recomendaciones automaticas y cambios a sesiones historicas.

## Listo cuando

- [x] El backend expone PUT /workout-plans/{planId}, reemplaza los dias y ejercicios de la rutina del propietario y no crea otro registro.
- [x] Una sesion valida de pruebas se inicia en el AVD con correo y contrasena verificados separadamente.
- [x] La lista de rutinas abre una rutina existente en modo edicion con sus valores actuales cargados en el AVD.
- [x] La persona puede cambiar nombre, dias, ejercicios, series, repeticiones y descanso con las mismas validaciones que al crear una rutina.
- [x] Android envia una actualizacion autenticada y muestra la rutina modificada en la lista sin crear una segunda rutina.
- [x] El backend rechaza actualizar una rutina de otra persona con respuesta 403 y mantiene las validaciones como 422.
- [x] Un error de guardado es recuperable y conserva los cambios introducidos para reintentar.
- [x] Las pruebas Android y backend cubren actualizacion, propiedad, validacion y error; ambas suites completas finalizan correctamente.
- [x] La validacion final en el AVD edita una rutina existente, verifica la lista actualizada y reinicia la app sin perder la rutina.
