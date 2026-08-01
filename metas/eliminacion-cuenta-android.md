# Eliminacion de cuenta Android

## Objetivo

Permitir que una persona autenticada elimine de forma consciente e irreversible su cuenta personal y los datos privados asociados desde Android, para que conserve control sobre sus rutinas, sesiones, medidas y objetivos antes de ampliar la aplicacion con funciones Premium o de gimnasio.

## Restricciones

- Reutilizar la sesion autenticada y las relaciones de datos personales existentes; exigir una confirmacion explicita antes de eliminar.
- No incluir exportacion de datos, recuperacion de cuentas eliminadas, cobros, Premium, vinculos con gimnasios ni funciones sociales.

## Listo cuando

- [x] Android ofrece una accion visible de eliminar cuenta desde el perfil y exige una confirmacion explicita antes de enviar la solicitud.
- [x] Un endpoint autenticado elimina exclusivamente la cuenta propietaria y sus rutinas, sesiones, medidas y objetivos asociados, sin afectar datos de otras cuentas.
- [x] Tras una eliminacion exitosa, Android borra el token y el estado local, vuelve a la pantalla de acceso y no recupera datos al reiniciar la aplicacion.
- [x] Cancelar la confirmacion o recibir un error conserva la cuenta y muestra un estado recuperable sin cerrar la aplicacion.
- [x] Las pruebas de backend verifican autorizacion y eliminacion aislada; las pruebas Android cubren confirmacion, limpieza de sesion y fallo recuperable; la suite completa y el emulador validan el flujo.
