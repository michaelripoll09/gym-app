# Datos

## Entidades principales

| Dominio | Entidades |
|---|---|
| Identidad | users, profiles, training_profiles, profile_secondary_interests, consents, sessions |
| Entrenamiento | exercises, workout_plans, workout_days, workout_exercises, workout_sessions, set_logs, measurements, progress_photos, goals |
| IA | ai_requests, ai_recommendations, prompt_versions, model_usage |
| Negocio personal | personal_subscriptions, store_transactions |
| Gimnasios | organizations, gym_locations, staff_memberships, member_links, membership_plans, memberships |
| Operación | payments, payment_attempts, payment_receipts, check_ins, access_tokens, audit_events |
| Plataforma | notifications, outbox_events, idempotency_keys |

## Reglas de modelado

- UUID como identificador externo; claves internas según necesidades del motor.
- Todas las tablas B2B incluyen `organization_id` y, cuando aplica, `gym_location_id`.
- Fechas en UTC; visualización en zona horaria configurada por la sede.
- Dinero en enteros de centavos y moneda ISO `COP`; nunca `float`.
- `deleted_at` para borrado lógico donde se requiera auditoría; fotos se borran físicamente tras el plazo de retención definido.
- Índices compuestos para consultas por sede, membresía activa, vencimiento y asistencia.
- `training_profiles` conserva nivel, perfil principal, objetivo, disponibilidad y fecha de vigencia; `profile_secondary_interests` admite como máximo dos intereses activos por perfil.
- Los catálogos de perfiles e intereses son versionados y no se almacenan como texto libre en las rutinas; esto permite reglas de compatibilidad y analítica consistente.

## Propiedad y retención

La cuenta personal es propiedad del usuario. Desvincular una sede conserva su entrenamiento y elimina el acceso administrativo según políticas de retención. Los registros financieros y de auditoría se conservan por el plazo legal y operativo que se defina con asesoría local.
