# Progresión guiada por ejercicio Android

## Objetivo

Dar a los usuarios autenticados recomendaciones de progresión claras por ejercicio a partir de sus últimas sesiones completadas, para que sepan si mantener, aumentar o reducir la carga o las repeticiones en su próxima sesión.

## Restricciones

- Usar únicamente el historial de series, cargas y rutinas existentes; no crear un sistema de IA ni una segunda fuente de progreso.
- Las recomendaciones deben ser deterministas, explicables y no modificar automáticamente rutinas ni sesiones.
- Incluir API, Android, pruebas automatizadas y conservar el diseño visual actual.

## Listo cuando

- [ ] La API devuelve recomendaciones solo para ejercicios con al menos dos registros completados del usuario autenticado.
- [ ] Cada recomendación incluye el último registro, el registro anterior, una acción visible (mantener, aumentar o reducir) y una explicación basada en repeticiones y carga.
- [ ] La API excluye series de rutinas archivadas y no expone información de otros usuarios.
- [ ] Android ofrece acceso a la progresión desde el flujo de progreso y muestra estados de carga, vacío, error con reintento y contenido.
- [ ] La pantalla muestra por ejercicio los valores anterior y actual, la recomendación y una explicación entendible antes de iniciar una nueva sesión.
- [ ] Ninguna acción de la pantalla altera automáticamente una rutina, una sesión o una carga registrada.
- [ ] Las pruebas de backend cubren aumento, mantenimiento, reducción, historial insuficiente y exclusión de archivados; las pruebas Android cubren estados y recomendaciones.
