# Onboarding de perfil completo Android

## Objetivo

Permitir que una persona que todavía no tiene perfil complete desde Android su experiencia, disciplinas, objetivo y disponibilidad antes de entrar al catálogo, para que su segmentación de entrenamiento se guarde y se recupere con datos elegidos por ella.

## Restricciones

- Reutilizar `PUT /me/training-profile`, las reglas actuales de perfil y la estética oscura con acento verde lima.
- Limitar los intereses secundarios a dos y no permitir que repitan la disciplina principal.
- Para la validación manual, usar una entrada de texto controlada en el AVD (por ejemplo, `adb shell input text` con un correo literal verificado antes de enviar) para evitar truncamientos de la automatización.
- Mantener fuera de alcance edición posterior del perfil, recomendaciones automáticas de rutinas, funciones Premium, gimnasios y funciones sociales.

## Listo cuando

- [x] Una cuenta sin perfil puede ver un onboarding con nivel de experiencia, disciplina principal, hasta dos disciplinas secundarias, objetivo, días disponibles y duración de sesión.
- [x] Android valida perfil principal, secundarios, días y duración antes del envío, manteniendo un error recuperable.
- [x] Android construye y envía al endpoint autenticado los valores seleccionados, usando la disciplina principal para el catálogo.
- [x] Las pruebas Android cubren selección, límites, validación y construcción de solicitud; las suites Android y backend finalizan correctamente.
- [ ] En un AVD funcional se valida una cuenta nueva con un correo introducido y verificado: crear cuenta, completar un perfil no genérico con disponibilidad personalizada, llegar al catálogo y reiniciar sin volver a onboarding.
