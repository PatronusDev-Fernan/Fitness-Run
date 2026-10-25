# Recorrido de FitnessRun

## Sin cuenta

1. Abre la app y pulsa **Continuar como invitado**. El perfil inicial es Invitado.
2. Crea una publicación de prueba en Comunidad y encuéntrala con la búsqueda.
3. Abre Rutina y completa los pasos del bloque de tres días. Logros debe mostrar un bloque de dos completado. Repetir la acción no suma otro bloque.
4. En Alimentación, revisa y marca una comida disponible. Las exclusiones no reconocidas pueden bloquear el menú hasta revisarlas.
5. En Perfil, edita nombre, descripción y foto. Vuelve a Comunidad y comprueba el cambio de identidad.
6. Desde Logros, prepara una publicación de avance. Los adjuntos permanecen locales.
7. Cierra sesión y entra como invitado otra vez: no deben conservarse las publicaciones ni el progreso anterior.

## Con Firebase propio

Configura [FIREBASE.md](FIREBASE.md). Usa correos y datos ficticios que controles.

1. Crea una cuenta desde la app, completa los formularios y confirma. Comprueba `users/{uid}`.
2. Edita el perfil y confirma el guardado. Cierra sesión e inicia de nuevo: los cambios deben recuperarse.
3. Marca progreso y confirma su guardado. Vuelve a iniciar sesión y revisa el historial.
4. Prueba contraseña incorrecta y selección de tipo de cuenta incorrecto. Debe permanecer en Login mostrando el error.
5. Sin red, abre el historial: debe mostrar un error con Reintentar, no una lista vacía. Reactiva la red y reintenta.
6. Con una sesión autenticada, abre Login sin red. Si la lectura no termina antes, a los 20 segundos debe devolver el control con un aviso. Una respuesta tardía no debe navegar después del aviso.
7. Usa una segunda cuenta para comprobar que no recibe perfil ni progreso de la primera.

El límite corresponde a lecturas de perfil/historial; no cancela transacciones ni sustituye los tiempos de Authentication. Las publicaciones y reacciones son locales también con cuenta.

## Evidencia para el portafolio

Capturas pendientes: Login, Comunidad, Rutina, Perfil y Logros con datos ficticios. Evitar correos personales, consolas, claves y fotos de terceros. Las pruebas manuales remotas no se presentan como realizadas.
