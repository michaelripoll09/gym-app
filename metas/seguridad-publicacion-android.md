# Seguridad de publicación Android

## Objetivo

Endurecer la aplicación Android y la configuración pública del repositorio para que las personas autenticadas no expongan sus tokens ni usen tráfico HTTP en compilaciones de lanzamiento, mientras el flujo local de desarrollo y el CI siguen funcionando de forma verificable.

## Restricciones

- Guardar el token de acceso únicamente mediante almacenamiento cifrado respaldado por Android Keystore; no registrar ni mostrar su valor en interfaz, logs, errores, pruebas ni archivos del repositorio.
- Una instalación que conserve un token legado en `SharedPreferences` ordinarias debe eliminarlo y requerir autenticación de nuevo en lugar de copiarlo sin cifrar.
- Permitir HTTP claro solo en una configuración de depuración dirigida al endpoint local del emulador; la variante release debe rechazar HTTP claro.
- No incorporar proveedores externos de autenticación, analítica, IA ni cambios al contrato backend existente.
- Mantener secretos de CI como variables o secretos del entorno y habilitar alertas de dependencias para el repositorio público; no añadir valores estáticos de producción o prueba al control de versiones.

## Listo cuando

- [ ] El token de sesión Android se guarda, lee y elimina exclusivamente mediante almacenamiento cifrado respaldado por Android Keystore, y ninguna ruta de producción lo escribe en `SharedPreferences` ordinarias.
- [ ] Al detectar un token legado no cifrado, la aplicación lo elimina y exige iniciar sesión de nuevo sin mostrar, registrar ni migrar su valor.
- [ ] La compilación release rechaza tráfico HTTP claro y la compilación debug limita cualquier excepción HTTP al endpoint local necesario para el emulador.
- [ ] La configuración de CI y los archivos rastreados no contienen secretos estáticos, y las alertas de Dependabot están habilitadas para el repositorio público.
- [ ] Las pruebas Android cubren almacenamiento cifrado, eliminación del token legado y restricciones de red por variante; las suites Android y backend, el build release y el chequeo de formato finalizan en PASS.
