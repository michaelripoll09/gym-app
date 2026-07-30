# Recuperación de perfil de entrenamiento Android

## Objetivo

Cerrar la validación integrada de la recuperación de perfil para que una persona autenticada conserve su segmentación real al abrir o reiniciar Android y vea el catálogo correspondiente, sin pasar de nuevo por onboarding cuando ya tenga perfil.

## Restricciones

- Reutilizar la autenticación JWT, la API de perfiles existente, el APK de depuración generado y la estética oscura con acento verde lima.
- Antes del recorrido, usar un AVD funcional —recreado o alternativo si `Pixel_10_Pro_XL` mantiene el bloqueo— que acepte `adb install -r` y `pm clear com.gymapp`; si el gestor de paquetes no responde, no se debe atribuir el bloqueo al código ni modificar funciones fuera de este flujo.
- Mantener fuera de alcance edición de perfil, recomendaciones automáticas, funciones Premium, gimnasios y funciones sociales.

## Listo cuando

- [x] La API autenticada recupera exclusivamente el perfil del propietario, con experiencia, perfil principal, secundarios, objetivo y disponibilidad; una cuenta sin perfil recibe `404`.
- [x] Android consulta el perfil antes de cargar el catálogo, utiliza el perfil principal persistido y cubre en pruebas los estados de perfil existente, ausente, no autorizado y recuperable.
- [x] Las suites completas de backend y Android finalizan correctamente con las pruebas de recuperación y aislamiento por propietario.
- [ ] Existe un AVD Android operativo, recreado o alternativo, que permite instalar o reutilizar el APK actual y limpiar los datos de `com.gymapp` sin que ADB o Package Manager queden bloqueados.
- [ ] Con una cuenta existente cuyo perfil principal sea `CALISTHENICS`, el emulador permite iniciar sesión, llegar directamente al catálogo filtrado y comprobar que no aparece onboarding.
- [ ] Tras forzar el cierre y reabrir la app con esa misma sesión, el emulador conserva el perfil de calistenia y vuelve al catálogo sin crear ni solicitar otro perfil.
- [ ] Al usar un token no autorizado, el emulador devuelve a acceso sin cierre inesperado; ante un error recuperable muestra la alternativa de reintento.
