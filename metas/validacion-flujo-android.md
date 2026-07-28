# Validación flujo Android

## Objetivo

Comprobar que una persona nueva puede usar en el emulador Android el flujo completo de crear cuenta, guardar su perfil y ver ejercicios filtrados desde el backend local.

## Restricciones

- Usar el emulador Android disponible, `10.0.2.2`, Retrofit y el backend Kotlin/Spring local.
- Limitarse a cuenta, perfil y catálogo; no añadir IA, pagos, gimnasios ni funciones sociales.

## Listo cuando

- [ ] La app debug actualizada se compila, se instala y se inicia en el emulador.
- [ ] La pantalla muestra un resultado legible tras intentar crear una cuenta.
- [ ] Un perfil primario se guarda autenticadamente desde Android.
- [ ] El catálogo filtrado del perfil se muestra después de guardar el perfil.
- [ ] La suite `:app:testDebugUnitTest` termina correctamente.
