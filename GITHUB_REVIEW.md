# Preparación para GitHub — 24 de octubre de 2026

## Cambios aplicados

- Eliminada la segunda regla permisiva de progreso. Solo permanece la validación estricta para crear y actualizar documentos del propietario.
- Ampliada la prueba de datos inválidos para comprobar tanto creación como actualización.
- Lecturas del perfil e historial limitadas a 20 segundos con `Tasks.withTimeout`. Un resultado tardío no sustituye el error del Task que consume la pantalla. No se cancelan ni se dan por fallidas anticipadamente escrituras o transacciones.
- El historial usa lectura del servidor, diferencia un error de una lista vacía y ofrece Reintentar. Descarta resultados cuando la Activity fue destruida o cambió la sesión.
- README, DEMO y FIREBASE reescritos según las funciones actuales. El informe de seguridad de octubre queda identificado como histórico.
- Configuración Firebase, cachés del IDE/Kotlin y APK excluidos. El archivo Firebase local se conserva para el desarrollo.
- Repositorio local inicializado en la rama `main`. No se creó repositorio remoto ni se publicó contenido.

## Validación

- Reglas Firestore: **6/6 pruebas aprobadas**, usando únicamente `demo-fitnessrun` en el emulador local. El fallo original de progreso inválido quedó corregido.
- Compilación de app y APK de pruebas completada: `BUILD SUCCESSFUL`. **42 pruebas unitarias aprobadas**. `lintDebug` completado con 168 advertencias; quedan mejoras de textos, recursos y accesibilidad.
- Pruebas Android del límite de espera y respuesta tardía: APK instalado, pero dos intentos terminaron con `Process crashed` antes de completar los casos. El registro del primer intento muestra un ANR de arranque (`failed to complete startup`). No se consideran aprobadas; repetir en un emulador estable o dispositivo de pruebas. La instalación del APK actualizado conservó los datos de la app.
- Búsqueda por patrones en los archivos candidatos a Git: sin coincidencias de claves privadas, tokens GitHub o claves API Firebase. Verificado que `google-services.json`, dependencias y archivos locales no aparecen entre los candidatos. Esto no constituye una auditoría exhaustiva de secretos.

## Antes de presentar una demo remota

1. Publicar las reglas corregidas en el Firebase correcto. Los cambios locales no modifican la consola.
2. Validar registro, inicio de sesión, edición, progreso e historial con dos cuentas de prueba. Incluye pérdida de red y recreación de pantalla.
3. Añadir capturas de Login, Comunidad, Rutina, Perfil y Logros sin datos personales.
4. Elegir licencia y destino del repositorio antes de publicarlo.

La espera de Authentication sigue administrada por el SDK; el límite añadido cubre las lecturas de perfil e historial. No se garantiza resolver cualquier congelamiento del emulador. La arquitectura y los límites funcionales están descritos en README: comunidad local, persistencia de perfil/progreso con Firebase y etiqueta coach sin privilegios adicionales.
