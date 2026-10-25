# Revisión de seguridad y pendientes — 6 de octubre de 2026

> Documento histórico. Para el estado del 24 de octubre de 2026 consulta `GITHUB_REVIEW.md`, `README.md` y `FIREBASE.md`. Las afirmaciones de pendientes y validación siguientes corresponden a la revisión original.

## Alcance
Revisión del código local y configuración Android/Firestore. No se verificó la consola remota, las reglas efectivamente publicadas, los permisos IAM, las restricciones de la clave API ni App Check. Este informe no certifica el servicio desplegado.

## Protecciones comprobadas en código
- Firestore limita lectura, creación, actualización y borrado al UID propietario; no permite listar usuarios y deniega las otras colecciones.
- Las reglas restringen campos, tipos, longitudes y correo del perfil al token autenticado; updatedAt debe ser la hora del servidor.
- Contraseñas fuera de Firestore, Intents y estado guardado; borrador temporal de registro en memoria.
- Caché Firestore en memoria, actividades internas no exportadas y copias de seguridad deshabilitadas.

## Cambios de esta revisión
- HTTP deshabilitado explícitamente en el manifiesto.
- WebView: archivos y content URIs deshabilitados, contenido mixto bloqueado, identificadores de video validados y navegación limitada a embeds HTTPS del host exacto de YouTube sin cookies. JavaScript sigue habilitado porque el reproductor lo utiliza; no hay puente JavaScript nativo.
- Exclusiones explícitas en las reglas de backup antiguas, además de las exclusiones existentes para Android 12+.
- Patrones de claves de firma y secretos locales añadidos a .gitignore. Esto no elimina archivos previamente versionados ni sustituye una auditoría del historial.
- Rutinas: bloques de 3 y 2 días, rechazo de índices inválidos, progreso de dos bloques y logros coherentes.
- Menús: ejemplos vegetarianos/veganos; si hay alergias, restricciones o alimentos no deseados escritos libremente, el menú queda pendiente de revisión. No se infieren sustituciones seguras a partir de texto.

## Pendientes prioritarios
1. Verificar Authentication, Firestore y reglas publicadas; probar dos usuarios y accesos no autenticados en el emulador antes de desplegar cambios.
2. Guardar progreso por usuario y fecha. Publicaciones, reacciones, elecciones del fin de semana y avances siguen en memoria.
3. Restauración de sesión, verificación de correo, eliminación de cuenta y edición completa del perfil.
4. Aviso de privacidad y consentimiento real. El flujo actual solicita datos de prueba.
5. Catálogo validado de ejercicios y alimentos; las rutinas siguen siendo ejemplos basados en palabras clave y los menús no evalúan necesidades nutricionales.
6. Comunidad remota con reglas propias, archivos, moderación y privacidad efectiva.
7. Revisar restricciones de claves, permisos administrativos y App Check en el proyecto remoto; no se modificaron desde esta revisión.

## Validación
- Los 41 XML se analizaron correctamente.
- Se actualizaron las pruebas unitarias de progreso y el recorrido instrumentado de demo.
- Se añadieron pruebas de URLs no confiables y menús vegetarianos/restricciones.
- La compilación y ejecución de pruebas con Gradle no se han confirmado: el entorno falla con Unable to establish loopback connection. Las pruebas nuevas no deben considerarse aprobadas hasta ejecutarlas.

## Referencias
- https://developer.android.com/privacy-and-security/risks/unsafe-uri-loading
- https://developer.android.com/privacy-and-security/risks/webview-unsafe-file-inclusion
- https://firebase.google.com/docs/firestore/security/test-rules-emulator

## Validación posterior: alimentos y coach

Se ejecutaron 26 pruebas unitarias de dominio con Kotlin/JUnit locales: todas aprobadas. Incluyen catálogo, exclusiones, preferencias, progreso y política de URLs. Los 42 XML actuales son válidos. Se añadieron recorridos instrumentados para selección de alimentos, recreación de pantalla y perfil del coach; aún no ejecutados. Gradle continúa fallando al iniciar por `Unable to establish loopback connection`, por lo que el APK y las pantallas nuevas siguen pendientes de compilación y prueba en dispositivo. No se cambió ninguna regla ni configuración remota de Firebase.

## Identidad de usuario y coach

Se añadieron tipo de cuenta inmutable (`user` / `coach`), descripción limitada y avatar JPEG reducido. `@coach` es una etiqueta declarada y no otorga permisos adicionales. Las nuevas reglas mantienen el aislamiento por UID. Foto sin EXIF, sin URL externa y sin permisos generales sobre la galería: el usuario elige el archivo. Estas reglas aún no se publicaron ni se verificaron contra el servicio remoto.

Última comprobación de identidad: 33 pruebas de dominio aprobadas, 44 XML válidos y APK generado correctamente desde Android Studio el 6 de octubre de 2026. Pendiente: publicar reglas y verificar registro, selector de tipo, carga de foto y guardado de descripción con cuentas reales; no se acredita seguridad del despliegue remoto.
