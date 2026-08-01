# Cierre de sesion Android

## Objetivo

Permitir que una persona autenticada cierre su sesion de Gym App desde su perfil de forma segura, para cambiar de cuenta o dejar el dispositivo sin que otra persona pueda recuperar sus datos privados de entrenamiento.

## Restricciones

- Reutilizar el almacenamiento local de sesion, entrenamiento sin conexion y recordatorios existente; no crear revocacion de JWT en servidor ni modificar cuentas o datos remotos.
- No incluir cambio o recuperacion de contrasena, eliminacion de cuenta, Premium, gimnasios ni funciones sociales.

## Listo cuando

- [x] Android muestra una accion visible de cerrar sesion desde el perfil y exige confirmacion explicita antes de limpiar el dispositivo.
- [x] Confirmar el cierre elimina el token, perfil, cache de rutinas, sesiones pendientes y recordatorios locales, y vuelve a la pantalla de acceso.
- [x] Cancelar conserva la sesion y los datos locales; cualquier fallo al limpiar se comunica de forma recuperable sin cerrar la aplicacion.
- [x] Tras cerrar sesion y reiniciar la aplicacion, no se recupera el perfil ni se muestra informacion de la cuenta anterior.
- [x] Las pruebas Android cubren confirmacion, cancelacion, limpieza completa y reinicio; el emulador valida cerrar sesion con una cuenta desechable.
