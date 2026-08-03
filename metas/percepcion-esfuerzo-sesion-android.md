# Percepcion de esfuerzo por sesion Android

## Objetivo

Permitir que las personas autenticadas registren al finalizar una sesion su esfuerzo percibido en una escala de 1 a 10 y una nota opcional, para que su historial personal refleje como se sintio el entrenamiento ademas de series, repeticiones y carga.

## Restricciones

- Ampliar unicamente el registro, consulta e historial de sesiones existentes; no anadir IA, recomendaciones nuevas, Premium, funciones sociales ni datos medicos.
- La percepcion de esfuerzo es opcional, debe validarse entre 1 y 10 cuando exista, y la nota debe seguir siendo privada para la persona duena de la sesion.
- Mantener la experiencia Android oscura con verde lima, los estados de carga, error recuperable y el funcionamiento actual sin conexion.
- La validacion manual debe usar una sesion de prueba vigente. Si el token queda invalido tras reinstalar el APK, cerrar sesion o eliminar solamente la preferencia `gym_app_session` de la app debug y crear una cuenta de prueba nueva; no borrar la cola `gym_app_offline_training` para conservar la evidencia de sincronizacion offline.

## Listo cuando

- [x] La API permite crear y consultar una sesion con esfuerzo opcional de 1 a 10 y una nota opcional, conservando las sesiones existentes que no tienen esos datos.
- [x] La API rechaza un esfuerzo fuera del rango y nunca expone el esfuerzo ni la nota de una sesion de otra persona.
- [x] La pantalla de finalizacion de sesion permite seleccionar o omitir el esfuerzo y escribir o omitir una nota antes de guardar.
- [x] El historial y el detalle de sesion muestran el esfuerzo y la nota solo cuando fueron registrados, sin alterar el resumen de cargas existente.
- [x] Una sesion registrada sin conexion conserva y sincroniza exactamente esos campos al recuperar conectividad.
- [x] Las pruebas de backend y Android cubren validacion, propiedad, persistencia, sincronizacion y compatibilidad con sesiones anteriores; las suites completas terminan correctamente.
- [x] En el emulador, una cuenta de prueba vigente completa una sesion con esfuerzo y nota, y confirma que ambos aparecen en su historial y detalle.
