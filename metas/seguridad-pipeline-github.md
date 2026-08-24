# Endurecimiento de GitHub Actions

## Objetivo

Reducir el riesgo de cadena de suministro del repositorio público fijando las acciones externas de GitHub Actions a revisiones inmutables y aplicando permisos mínimos, para que quienes mantienen Gym App puedan ejecutar CI reproducible sin ampliar el acceso del token de automatización.

## Restricciones

- Fijar cada acción externa usada en workflows a un SHA completo de 40 caracteres y documentar junto a ella la versión legible correspondiente; no usar etiquetas móviles como `@v4` o `@main`.
- Declarar permisos mínimos por workflow o job; el valor predeterminado debe ser solo lectura y cualquier permiso adicional debe justificarse por la operación existente.
- No añadir secretos estáticos, tokens personales, despliegues, publicación de paquetes ni nuevas integraciones externas.
- Mantener los disparadores actuales de CI y las validaciones Android/backend existentes; el endurecimiento no debe reducir las pruebas ejecutadas.
- Conservar Dependabot configurado para GitHub Actions y dependencias Gradle sin desactivar alertas ni actualizaciones existentes.

## Listo cuando

- [ ] Todos los usos de acciones externas en `.github/workflows/` están fijados a SHA completos y cada uno conserva una anotación de versión legible.
- [ ] Cada workflow define permisos mínimos y ningún job recibe permisos de escritura salvo que una operación actual lo requiera y quede justificada en el archivo.
- [ ] La configuración de CI no contiene secretos estáticos ni expone valores sensibles en logs, y mantiene los mismos disparadores y tareas Android/backend que existían antes del endurecimiento.
- [ ] `.github/dependabot.yml` mantiene actualizaciones semanales para `github-actions`, Android Gradle y backend Gradle, y las alertas de vulnerabilidades del repositorio continúan habilitadas.
- [ ] Una comprobación automatizada detecta referencias móviles o SHA incompletos en los workflows, y el CI completo, las suites Android/backend y el chequeo de formato finalizan en PASS.
