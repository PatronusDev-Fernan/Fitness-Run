# Configurar Firebase

La demo de invitado compila sin Firebase. Para cuentas, perfil y progreso remoto, aporta tu propia configuración.

## Preparación

1. Crea o selecciona tu proyecto en Firebase.
2. Registra Android con paquete `com.example.fitnessrun`.
3. Descarga `google-services.json` y colócalo en `app/`. Está excluido de Git.
4. Activa Authentication con **Correo electrónico/Contraseña**.
5. Crea Cloud Firestore, base `(default)`, en producción, eligiendo la región adecuada.
6. Ejecuta las pruebas locales y publica `firestore.rules` en tu proyecto. No habilites acceso global.
7. Compila y sigue el recorrido de [DEMO.md](DEMO.md).

La configuración Android contiene identificadores de cliente, no credenciales administrativas. Se excluye para que otras copias no apunten al servicio del autor. No añadas cuentas de servicio, claves privadas ni archivos de firma.

## Datos

```text
Authentication: UID y credenciales
users/{uid}
  profile: mapa de campos registration.*
  updatedAt: fecha del servidor
users/{uid}/dailyProgress/{AAAA-MM-DD}
  date
  completedRoutines
  completedMeals
  exerciseChecks
  weekendRoutine
  weekendFood
  updatedAt: fecha del servidor
```

Los puntos de `registration.*` son parte literal de las claves del mapa `profile`. Incluye datos personales, físicos, preferencias y restricciones, además de:

- `registration.account_type`: user o coach, inmutable, sin privilegios adicionales. Los perfiles antiguos sin campo son usuarios.
- `registration.bio`: hasta 240 caracteres.
- `registration.photo`: JPEG reducido en base64, hasta 60.000 caracteres; vacío elimina la foto. No usa Storage.

Perfil y progreso pertenecen al UID autenticado. No se pueden listar perfiles ajenos. La validación estricta de progreso se aplica tanto a creación como a actualización.

Si Authentication crea la cuenta pero falla el perfil, repetir el registro con las mismas credenciales permite completar el documento faltante. No se sobrescribe un perfil existente.

## Pruebas locales

```powershell
npm ci
npm run test:rules
```

La suite usa `demo-fitnessrun`, acceso anónimo y dos identidades ficticias. Comprueba aislamiento, etiquetas y datos inválidos al crear y actualizar. No usa credenciales administrativas ni modifica la base real.

Si Java falla al abrir sockets locales en esta laptop, utiliza el JBR de Android Studio y el ajuste `JAVA_TOOL_OPTIONS` del README.

## Despliegue y alcance

Editar el archivo local **no publica las reglas automáticamente**. Publica las reglas revisadas en el proyecto correcto y verifica los flujos con cuentas de prueba. Esta preparación para GitHub no modificó ni certificó el servicio remoto.

Publicaciones, adjuntos y reacciones siguen siendo locales. No hay comunidad remota ni acceso de coaches a datos de salud ajenos.

Referencias: [configuración Android](https://firebase.google.com/docs/android/setup), [pruebas de reglas](https://firebase.google.com/docs/firestore/security/test-rules-emulator), [claves Firebase](https://firebase.google.com/docs/projects/api-keys).
