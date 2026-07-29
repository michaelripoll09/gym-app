# Edicion de perfil Android

## Objetivo

Permitir que una persona autenticada consulte y actualice desde Android su perfil de entrenamiento para adaptar el catalogo a sus preferencias actuales sin repetir onboarding ni crear otra cuenta.

## Restricciones

- Reutilizar los endpoints autenticados de perfil y las reglas existentes de validacion, manteniendo la estetica oscura con acento verde lima.
- Conservar el limite de dos disciplinas secundarias y no permitir que una repita la disciplina principal.
- No incluir recomendaciones automaticas, Premium, gimnasios, funciones sociales ni cambios de contrasena.

## Listo cuando

- [x] Una persona autenticada abre Mi perfil desde el catalogo y ve sus valores guardados despues de la carga.
- [x] Puede modificar nivel, disciplina principal, hasta dos secundarias, objetivo, dias y duracion con las mismas reglas de validacion del onboarding.
- [x] Al guardar una disciplina principal valida, Android actualiza el perfil y muestra el catalogo filtrado por ella.
- [x] Los errores de carga o guardado se muestran de forma recuperable sin perder los cambios introducidos.
- [x] Las pruebas Android cubren carga, edicion, validacion y actualizacion del catalogo; las suites Android y backend finalizan correctamente.
