# Arquitectura

## Decisión tecnológica

La solución será nativa y empresarial:

- iOS: Swift, SwiftUI y arquitectura MVVM/Clean Architecture.
- Android: Kotlin, Jetpack Compose y arquitectura MVVM/Clean Architecture.
- Web administrativa: Next.js y TypeScript.
- Backend: Kotlin, Spring Boot, API REST versionada y procesamiento asíncrono.
- Datos: PostgreSQL como fuente transaccional; almacenamiento de objetos privado para fotos y archivos.

## Principios

- Monolito modular primero: dominios aislados dentro de un único despliegue; no microservicios prematuros.
- API como contrato único para iOS, Android, web y app operativa.
- Multi-tenant: organización y sede forman parte del contexto de toda operación B2B.
- Roles y autorización aplicados en backend, nunca solo en interfaz.
- Integraciones externas detrás de adaptadores: IA, pagos, correo y notificaciones no contaminan lógica de negocio.

## Módulos de dominio

| Módulo | Responsabilidad |
|---|---|
| Identidad | cuentas, sesiones, recuperación y consentimiento |
| Entrenamiento | ejercicios, rutinas, sesiones y progresión |
| IA | generación, coach, análisis, límites y trazabilidad |
| Suscripciones | Premium personal y estado de tienda móvil |
| Organizaciones | gimnasios, sedes, equipo y roles |
| Membresías | socios, planes, vínculos y vigencias |
| Pagos | cobros, comprobantes, conciliación y webhooks |
| Asistencia | QR dinámicos, accesos y rechazos |
| Notificaciones | recordatorios, vencimientos y eventos |
| Auditoría | acciones administrativas y eventos sensibles |

## Integraciones

- Tiendas Apple y Google para la suscripción Premium en móvil.
- Proveedor colombiano de pagos para membresías digitales; el adaptador permitirá sustituirlo sin modificar el dominio.
- Proveedor de modelos de IA detrás de un `AI Gateway` con límites de gasto, versionado de prompts y trazabilidad.
- Servicio de correo y notificaciones push mediante proveedores intercambiables.
- Dataset de ejercicios: importación reproducible desde un commit fijado, validada contra JSON Schema y almacenada localmente; la aplicación nunca depende del repositorio de origen durante una sesión de usuario.

## Entornos

Desarrollo, pruebas, staging y producción estarán separados. Producción usa secretos administrados, copias de seguridad verificadas y observabilidad centralizada.
