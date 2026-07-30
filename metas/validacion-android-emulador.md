# Validación Android emulador

## Objetivo

Verificar que una persona nueva pueda completar en el emulador Android el flujo local de cuenta, perfil y catálogo contra el backend Kotlin/Spring.

## Restricciones

- Usar Android nativo y el emulador configurado con `10.0.2.2`.
- Usar el backend local existente y no incluir funciones fuera de cuenta, perfil y catálogo.

## Listo cuando

- [ ] El backend local responde en `/api/v1/health` mientras se ejecuta la validación.
- [ ] La suite `:app:testDebugUnitTest` termina correctamente.
- [ ] Una cuenta nueva puede registrarse desde el emulador y se conserva el JWT durante la sesión.
- [ ] El perfil guardado desde Android produce un catálogo filtrado visible para el perfil principal.
