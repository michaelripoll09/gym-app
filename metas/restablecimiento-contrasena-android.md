# Restablecimiento de contrasena Android

## Objetivo

Permitir que una persona que no recuerda su contrasena solicite un enlace seguro por correo y defina una clave nueva desde Android. El flujo gratuito de pruebas ya fue validado de extremo a extremo con Resend y emulador; la migracion a dominio propio queda diferida hasta el despliegue.

## Restricciones

- El token es de un solo uso, vence a los 15 minutos y solo se guarda su hash; la solicitud responde siempre de forma neutra.
- `RESEND_API_KEY` solo vive como variable de entorno local o secreto de despliegue; nunca se agrega al repositorio.
- Desarrollo usa `onboarding@resend.dev` y `gymapp://reset-password`; produccion requiere un dominio verificado, `RESEND_FROM_EMAIL` y un App Link HTTPS configurado en `GYM_RESET_URL`.
- Para pruebas se acepta el remitente gratuito de Resend y el deep link personalizado; esta modalidad solo debe usarse con el correo propietario de la cuenta de Resend.
- Solucion del requisito de produccion: comprar o usar un dominio propio, crear el dominio en Resend, copiar sus registros DNS (SPF/DKIM) al proveedor del dominio hasta que Resend lo verifique, publicar `assetlinks.json` en ese dominio y cambiar los secretos `RESEND_FROM_EMAIL` y `GYM_RESET_URL` en el despliegue.
- No incluir Premium, gimnasios, redes sociales, cambio de correo ni revocacion remota de JWT existentes.

## Listo cuando

- [x] Acceso permite solicitar recuperacion, valida el correo y muestra una confirmacion neutra.
- [x] Backend crea tokens con hash, vencimiento y un solo uso; permite iniciar con la nueva contrasena y rechaza la anterior.
- [x] Android recibe el deep link `gymapp://reset-password?token=...`, valida la nueva contrasena y confirma el cambio.
- [x] Pruebas backend y Android pasan para solicitud neutra, token usado y validaciones locales.
- [x] Se prueba en el emulador un correo real de Resend con la cuenta de desarrollo y se confirma que abre Android y restablece la contrasena.
- [x] Se prueba y documenta el rechazo de un token vencido.
- [ ] Diferido hasta el despliegue: verificar un dominio propio en Resend, configurar sus registros DNS, publicar `assetlinks.json` y sustituir el deep link por un App Link HTTPS en `GYM_RESET_URL`; esto resuelve la limitacion del remitente de desarrollo.
