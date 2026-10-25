# FitnessRun

Aplicación Android de bienestar desarrollada con **Kotlin, XML, Material Components y Firebase**. Proyecto de portafolio en desarrollo: conecta registro, perfil, rutinas, alimentación y progreso, con una comunidad local de demostración.

## Funciones y alcance

| Función | Con cuenta Firebase | Como invitado |
|---|---|---|
| Registro, acceso y recuperación de contraseña | Firebase Authentication por correo | No requiere cuenta |
| Perfil, descripción y avatar | Documento privado en Firestore | Memoria durante la sesión |
| Rutinas, comidas y progreso | Guardado por usuario/fecha e historial | Memoria durante la sesión |
| Publicaciones, adjuntos y reacciones | Locales durante la sesión | Locales durante la sesión |
| Usuario / Coach | Etiqueta declarada, sin privilegios adicionales | Selección para explorar |

Incluye catálogo de alimentos y filtros de preferencias/restricciones. Las rutinas y menús son ejemplos de software, no planes clínicos. No hay chat, comunidad remota, asignación real de coach ni Google Sign-In.

## Ejecutar

1. Abre la carpeta en Android Studio con soporte para la versión de AGP configurada en `gradle/libs.versions.toml`.
2. Instala Android SDK 37. El dispositivo debe tener Android 7.0 / API 24 o superior.
3. El Wrapper usa Gradle 9.5.0 y el daemon está configurado para JDK 25. Permite descargar esa toolchain o instala un JDK que coincida.
4. Sin Firebase, ejecuta `app` y pulsa **Continuar como invitado**. Firebase es opcional para compilar.
5. Para cuentas, sigue [FIREBASE.md](FIREBASE.md) y coloca tu propio `google-services.json` en `app/`. Está excluido de Git.

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug
```

En macOS/Linux usa `bash ./gradlew assembleDebug testDebugUnitTest lintDebug`. APK: `app/build/outputs/apk/debug/app-debug.apk`.

Si esta laptop Windows devuelve `Unable to establish loopback connection`, antes del comando usa:

```powershell
$env:JAVA_TOOL_OPTIONS='-Djdk.net.unixdomain.tmpdir=./build/gradle-tcp-fallback'
```

Es un ajuste del entorno, no un requisito general. No crees esa carpeta temporal de fallback.

## Estructura

- `app/src/main/java/com/example/fitnessrun/`: Activities, dominio y acceso Firebase.
- `app/src/main/res/`: pantallas XML, textos y gráficos.
- `CloudProfiles`, `CloudProgress`: cuentas, perfil y progreso remoto.
- `ProfileRequestViewModel`: solicitudes de perfil durante rotaciones.
- `DemoSession`, `DemoProgress`: estado compartido y progreso visible.
- `FoodCatalog`, `PersonalizationEngine`, `ProgressSnapshot`: lógica de dominio.
- `firestore.rules` y `tests/`: permisos y pruebas locales de Firestore.

Usa Activities y View Binding con lógica separada y estado compartido en memoria. No implementa una arquitectura MVVM completa.

## Pruebas

```powershell
.\gradlew.bat testDebugUnitTest lintDebug
npm ci
npm run test:rules
```

Las pruebas de reglas necesitan Node.js y Java (preferiblemente JDK 21 o superior). Usan el proyecto ficticio `demo-fitnessrun` en un emulador local; no escriben en Firebase real.

Los recorridos Android están en `app/src/androidTest/`. `connectedDebugAndroidTest` requiere un dispositivo de pruebas sin cuentas personales. Consulta [DEMO.md](DEMO.md) y [GITHUB_REVIEW.md](GITHUB_REVIEW.md) para el recorrido y la validación.

## Privacidad y límites

- Authentication administra las credenciales; no se guardan contraseñas en Firestore ni se pasan entre Activities.
- Perfil y progreso tienen permisos por UID. La etiqueta coach no concede acceso a otros usuarios.
- El avatar se reduce y recodifica sin metadatos antes de guardarse en el perfil privado.
- Las publicaciones no se envían a otros usuarios. El estado de invitado desaparece al salir o terminar el proceso.
- Las lecturas de perfil e historial tienen un límite de 20 segundos. El historial muestra errores y permite reintentar.
- Las pruebas locales no certifican la configuración y reglas desplegadas en un proyecto remoto.

Usa datos ficticios. Antes de operar con usuarios reales faltan completar la gestión de cuentas, validar los flujos remotos y definir un aviso de privacidad adecuado.

## Pendientes

- Validar en dispositivo registro, acceso, perfil y progreso con dos cuentas de prueba.
- Añadir capturas del recorrido con datos ficticios.
- Reducir advertencias de accesibilidad, textos y recursos de Android Lint.
- Decidir la licencia antes de conceder permisos explícitos de reutilización. Actualmente no se incluye una licencia de código abierto.
