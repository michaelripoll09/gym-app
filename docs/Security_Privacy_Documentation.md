# Seguridad y privacidad

## Protección de cuentas y datos

- Contraseñas con hash fuerte y nunca registradas en logs.
- TLS para todo tráfico; cifrado de datos y objetos en reposo.
- Secretos en gestor dedicado, rotación y mínimo privilegio.
- Autorización basada en rol, organización y sede; pruebas obligatorias contra escalamiento de privilegios.
- Límites de tasa, detección de abuso y bloqueo progresivo de intentos de acceso.

## Privacidad

- Consentimiento granular para términos, privacidad, comunicaciones y compartición con entrenadores.
- Solicitud de acceso, corrección y eliminación desde el perfil, con flujo administrativo verificable.
- Minimización de datos: no se solicitan diagnósticos médicos para generar rutinas.
- Las fotos usan URLs firmadas de vida corta; no son públicas ni se incluyen en logs.
- Antes de producción se realizará revisión jurídica colombiana de política de tratamiento de datos, términos, consentimiento y retención.

## Incidentes

Se documentará un runbook con detección, contención, evaluación de alcance, recuperación, comunicación y revisión posterior. Los eventos de seguridad tendrán alertas y trazabilidad.
