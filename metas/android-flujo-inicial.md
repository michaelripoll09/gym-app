# Android flujo inicial

## Objetivo

Permitir que una persona nueva en Android cree una cuenta, defina su perfil de entrenamiento y vea el catálogo filtrado de ejercicios desde el emulador.

## Restricciones

- Android nativo con Jetpack Compose y el backend Kotlin/Spring existente.
- El emulador usa `10.0.2.2`; no incluir IA, pagos, gimnasios ni funciones sociales.
- Usar únicamente Retrofit y serialización ya aprobados para la red.

## Listo cuando

- [ ] Una persona puede registrarse desde Android y la app conserva el JWT durante la sesión.
- [ ] La persona selecciona perfil, objetivo y disponibilidad, y la app guarda el perfil mediante la API autenticada.
- [ ] Tras guardar el perfil, la app carga y muestra ejercicios filtrados para el perfil principal.
- [ ] El build y las pruebas unitarias Android pasan usando el emulador configurado.
