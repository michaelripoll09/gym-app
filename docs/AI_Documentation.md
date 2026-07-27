# Inteligencia artificial

## Capacidades del lanzamiento

1. Generación guiada de rutinas por objetivo, nivel, perfil principal, intereses secundarios, tiempo y equipamiento.
2. Ajuste de una rutina existente con explicación de cambios y de compatibilidad entre perfiles.
3. Coach conversacional que conoce el contexto permitido del usuario.
4. Análisis de progreso basado en sesiones, adherencia y metas.

## Guardrails

- La IA no diagnostica lesiones, enfermedades, trastornos alimentarios ni urgencias.
- Ante dolor intenso, síntomas alarmantes o peticiones médicas, muestra un mensaje de seguridad y recomienda profesional competente.
- No propone ejercicios fuera del catálogo validado ni altera una rutina activa sin confirmación.
- Prioriza un único perfil principal; los intereses secundarios no pueden introducir volumen o progresiones incompatibles con la disciplina prioritaria.
- Si una combinación no es compatible con la disponibilidad o la carga total, explica el conflicto y propone una alternativa en vez de mezclar plantillas sin criterio.
- Usa respuestas estructuradas y validación de esquema antes de mostrar planes.
- Toda recomendación incluye explicación breve, fecha, versión de prompt/modelo y posibilidad de reportar una respuesta inadecuada.

## AI Gateway

El backend concentra proveedor, prompts, filtros, presupuesto por usuario, límites de frecuencia, registro de uso y fallback. Las apps nunca contienen credenciales de IA ni llaman directamente al proveedor.

## Datos y privacidad

Solo se envía al modelo la mínima información necesaria para cada interacción. Fotos de progreso no se analizan en el lanzamiento. Las conversaciones son privadas y el usuario puede eliminarlas; se aplicará una política de retención explícita antes de producción.
