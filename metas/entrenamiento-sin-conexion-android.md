# Entrenamiento sin conexión Android

## Objetivo

Permitir que las personas autenticadas completen una sesión de una rutina ya cargada aunque pierdan conectividad en el gimnasio, guardando el registro localmente y sincronizándolo de forma segura al recuperar internet para que no pierdan su progreso.

## Restricciones

- Implementar únicamente en Android reutilizando el contrato existente de sesiones; no modificar automáticamente rutinas, series guardadas ni crear una segunda fuente permanente de historial.
- La sincronización debe ser explícita para la persona o ejecutarse al recuperar conectividad, y no puede duplicar una sesión ya enviada al backend.

## Listo cuando

- [x] La aplicación conserva localmente las rutinas activas necesarias para iniciar y registrar una sesión cuando no hay conexión.
- [x] Una sesión terminada sin conexión queda claramente identificada como pendiente y conserva ejercicios, series, repeticiones y cargas tras cerrar y abrir la aplicación.
- [x] Al recuperar conectividad, la persona puede sincronizar las sesiones pendientes y recibe un resultado claro por cada sesión enviada o que no pudo enviarse.
- [x] La sincronización utiliza el endpoint actual de creación de sesiones y evita que una misma sesión pendiente se registre más de una vez.
- [x] El historial y el progreso muestran de forma coherente las sesiones pendientes y se actualizan al completarse la sincronización, sin alterar rutinas existentes.
- [x] La interfaz incluye estados de carga, sin conexión, cola vacía, error recuperable y reintento, conservando el diseño visual actual.
- [x] Las pruebas Android cubren persistencia local, cola pendiente, sincronización exitosa, reintento tras error y prevención de duplicados; una validación en emulador confirma completar una sesión sin red y sincronizarla al restaurarla.
