# Medidas y progreso corporal Android

## Objetivo

Permitir que las personas que usan Gym App de forma independiente registren sus medidas corporales y observen una tendencia privada junto a su progreso de entrenamiento, para evaluar avances hacia su objetivo sin depender de un gimnasio.

## Restricciones

- Implementar primero el flujo Android y el contrato backend mínimo para peso corporal y medidas opcionales; mantener los datos privados, no incorporar fotos, IA, nutrición, pagos ni funcionalidades de gimnasios en este bloque.

## Listo cuando

- [x] Una persona autenticada puede crear, editar y eliminar un registro fechado de peso corporal y medidas opcionales desde Android.
- [x] Los registros se validan de forma clara y no admiten valores imposibles, fechas vacías ni medidas duplicadas para el mismo día.
- [x] La pantalla de progreso muestra el último valor y la tendencia temporal de peso y de cada medida registrada, con estado vacío, carga, error recuperable y reintento.
- [x] La información se guarda y consulta mediante endpoints autenticados, asociados solo a la cuenta de la persona y sin exponerla a un gimnasio.
- [x] El historial de medidas se conserva tras cerrar y abrir la aplicación y refleja las modificaciones o eliminaciones realizadas.
- [x] Pruebas de backend y Android cubren validación, privacidad por usuario, creación, edición, eliminación y representación de tendencias; una validación en emulador confirma el flujo completo.
