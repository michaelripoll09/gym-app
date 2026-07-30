# Carga por serie Android

## Objetivo

Permitir que las personas autenticadas registren de forma opcional la carga en kilogramos de cada serie al completar una rutina, para que su historial y progreso reflejen el entrenamiento de fuerza real sin perjudicar ejercicios de peso corporal, running o movilidad.

## Restricciones

- Extender el contrato de sesiones existente con un campo opcional de carga por serie y conservar la compatibilidad con sesiones ya guardadas y clientes que solo envían repeticiones.
- Usar kilogramos como unidad inicial; dejar fuera conversión de unidades, récords personales, recomendaciones automáticas, gráficas nuevas y edición posterior de sesiones.
- Validar cargas numéricas no negativas antes de enviar la sesión y mantener el diseño oscuro con acento verde lima.

## Listo cuando

- [ ] La base de datos y la API aceptan y devuelven una carga opcional por serie sin alterar los registros históricos que no la tienen.
- [ ] Android muestra un campo opcional de carga en kg junto a las repeticiones de cada serie durante una sesión.
- [ ] Android bloquea el envío de valores de carga vacíos no válidos, negativos o no numéricos y permite guardar series sin carga.
- [ ] Al guardar una sesión, cada carga válida se envía al backend y se conserva al consultar el historial.
- [ ] El detalle del historial muestra la carga cuando existe y no muestra datos inventados cuando la serie no tiene carga.
- [ ] Las pruebas de backend y Android cubren compatibilidad sin carga, carga válida, validación inválida y representación en historial; sus suites correspondientes finalizan correctamente.
- [ ] El flujo completo se comprueba en el emulador: iniciar rutina, registrar repeticiones y carga, finalizar y consultar el detalle del historial.
