# Validacion de onboarding de perfil Android

## Objetivo

Validar en un AVD funcional el recorrido completo de una cuenta nueva: registro, definicion de perfil, catalogo y reinicio; comprobando que el perfil elegido se persiste y no se vuelve a solicitar.

## Restricciones

- Usar el AVD Android 15 funcional, la API local y el flujo de onboarding existente, sin ampliar funcionalidades de producto.
- Verificar el correo antes de enviarlo durante la automatizacion para evitar truncamientos.
- Mantener fuera de alcance edicion de perfil, recomendaciones automaticas, Premium, gimnasios y funciones sociales.

## Listo cuando

- [x] Una cuenta nueva se registra desde Android con un correo verificado y llega al onboarding.
- [x] En onboarding se elige Calistenia como disciplina principal, disponibilidad Alta y se guarda el perfil sin error.
- [x] Android muestra el catalogo de Calistenia tras guardar y no vuelve a mostrar onboarding.
- [x] Tras forzar el cierre y reabrir con la misma sesion, Android vuelve al catalogo sin crear ni pedir otro perfil.
- [x] Las suites completas de Android y backend finalizan correctamente durante la validacion.
