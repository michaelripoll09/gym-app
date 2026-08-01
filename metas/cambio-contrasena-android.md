# Cambio de contrasena Android

## Objetivo

Permitir que una persona autenticada cambie su contrasena desde su perfil al confirmar su clave actual, para mantener el control de su cuenta sin depender todavia de un proveedor externo de correo para recuperacion.

## Restricciones

- Requerir contrasena actual, nueva contrasena y confirmacion; reutilizar el hash BCrypt y la sesion autenticada existentes.
- Tras un cambio exitoso, cerrar la sesion local para exigir un nuevo inicio con la clave actualizada; no implementar revocacion remota de JWT ni envio de correos.
- No incluir recuperacion por correo, eliminacion de cuenta, Premium, gimnasios ni funciones sociales.

## Listo cuando

- [x] Android muestra una accion de cambiar contrasena desde el perfil, solicita clave actual, nueva y confirmacion, y valida que la nueva sea valida y coincida antes de enviar.
- [x] Un endpoint autenticado cambia exclusivamente la contrasena de la cuenta propietaria cuando la clave actual es correcta, sin modificar perfiles, rutinas, sesiones, medidas ni objetivos.
- [x] Una clave actual incorrecta o una nueva clave invalida muestra un error recuperable y conserva la sesion y los datos remotos sin cambios.
- [x] Al cambiar con exito, Android limpia la sesion local y permite iniciar con la nueva contrasena, mientras que la anterior deja de funcionar.
- [x] Las pruebas backend cubren propiedad, clave actual incorrecta y nuevo inicio; las pruebas Android cubren validacion y limpieza; el emulador valida el flujo con una cuenta desechable.
