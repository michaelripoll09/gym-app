# Diseño validado: Gym App

## Resumen

Gym App se lanzará inicialmente en Colombia como una aplicación personal de entrenamiento para iOS y Android, complementada por una plataforma web y móvil para operar gimnasios. El usuario puede usar el producto de forma independiente y vincularse voluntariamente a un gimnasio sin ceder su información privada de entrenamiento.

## Decisiones de producto

- Personalización por nivel, perfil principal único (fitness general, culturismo/bodybuilding, powerlifting, running, CrossFit o calistenia), hasta dos intereses secundarios, disponibilidad y objetivo; todas las dimensiones son editables desde el perfil.
- Rutinas por tres vías: planes curados, creador manual y generación/ajuste con IA.
- IA completa en lanzamiento: generador guiado, coach conversacional y análisis de progreso; con controles de seguridad y sin asesoría médica.
- Freemium para personas y suscripción por sede para gimnasios.
- Membresías: registro manual y pagos online; asistencia mediante QR dinámico.
- Socios: app nativa iOS/Android. Gimnasios: panel web y aplicación móvil operativa.

## Arquitectura acordada

SwiftUI para iOS, Kotlin/Jetpack Compose para Android, Next.js/TypeScript para web y Kotlin/Spring Boot/PostgreSQL para el backend. El backend comienza como monolito modular, API REST versionada y adaptadores para IA, pagos, notificaciones y almacenamiento.

## Límites y seguridad

La información personal de entrenamiento permanece privada. El gimnasio solo recibe información de membresía, pagos y asistencia, salvo consentimiento expreso del usuario. Las capacidades de IA no sustituyen a profesionales de salud y se someten a límites, validación estructurada y auditoría.

## Aprobación

El usuario aprobó el flujo funcional el 2026-07-21 como base para documentación y planificación.
