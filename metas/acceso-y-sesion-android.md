# Acceso y sesión Android

## Objetivo

Permitir que una persona que ya creó su cuenta inicie sesión desde Android y recupere de forma segura su catálogo, rutinas e historial, para que la aplicación sea utilizable más allá del primer registro.

## Restricciones

- Reutilizar el endpoint de inicio de sesión y el almacenamiento local de token existentes, manteniendo la estética oscura con acento verde lima.
- Mantener fuera de alcance recuperación de contraseña, acceso social, verificación por correo, biometría, edición de cuenta y funciones Premium.

## Listo cuando

- [ ] La pantalla de acceso permite alternar de forma visible entre crear una cuenta e iniciar sesión con correo y contraseña.
- [ ] Un inicio de sesión válido guarda el token y lleva a la persona al flujo principal sin crear una cuenta ni perfil duplicados.
- [ ] Unas credenciales inválidas muestran un mensaje recuperable y permiten volver a intentarlo sin cerrar la aplicación.
- [ ] Una sesión expirada o no autorizada elimina el token local y devuelve a la pantalla de acceso sin provocar un cierre inesperado.
- [ ] Tras reiniciar la aplicación con una sesión válida, se recuperan y pueden consultarse las rutinas y el historial ya existentes del propietario.
- [ ] Las pruebas Android cubren inicio de sesión exitoso, error de credenciales y expiración de sesión, y la suite Android completa finaliza correctamente.
- [ ] En el emulador se valida el recorrido de una cuenta existente: iniciar sesión, abrir Mis rutinas y consultar Historial.
