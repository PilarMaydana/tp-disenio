# CU 028: orden de mérito

Esta guía describe la implementación disponible para probar el CU 28 en desarrollo local: búsqueda de puestos, selección de cuestionarios, reporte con resultados reales, persistencia de respuestas y pruebas automatizadas. La base de datos configurada es H2 en memoria y vuelve a cargarse desde el seed cada vez que se inicia la API.

## Requisitos

- JDK 21 (incluye `java` y el compilador `javac`). Un JRE solo ejecuta Java y no compila.
- Node.js y npm para ejecutar el frontend.
- No hace falta instalar Maven: el backend incluye `mvnw.cmd`.

En el entorno de desarrollo usado para validar esta guía ya existe JDK 21 en `C:\Users\valem\.jdks\openjdk-21.0.2`. La consola de Windows inicialmente elegía el Java 8 Runtime de Oracle y no encontraba `javac`; por eso hay que seleccionar el JDK en cada nueva terminal o configurar `JAVA_HOME` en IntelliJ.

En PowerShell, antes de compilar o arrancar el backend:

```powershell
$env:JAVA_HOME = 'C:\Users\valem\.jdks\openjdk-21.0.2'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
javac -version
```

Ambos comandos deben mostrar versión 21. Esta selección dura mientras la terminal esté abierta. Para persistirla en IntelliJ, abrir **File > Project Structure** y elegir el JDK 21 como **Project SDK** y nivel de lenguaje; en **Settings > Build, Execution, Deployment > Build Tools > Maven > Runner**, elegir el mismo JDK en **JRE**. Abrir una terminal nueva luego de cambiar variables de entorno.

Si JDK 21 no está instalado, instalar un **JDK 21** (por ejemplo, Temurin/OpenJDK) y cambiar `JAVA_HOME` a su carpeta. En esta máquina ya está instalado, así que no es necesario descargar otro.

## Arrancar la aplicación

### Backend

En una primera ventana de PowerShell:

```powershell
$env:JAVA_HOME = 'C:\Users\valem\.jdks\openjdk-21.0.2'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
cd 'C:\Desarrollo de sistemas_codigo\tp-disenio\api'
.\mvnw.cmd spring-boot:run
```

La API queda en `http://localhost:8080`; Swagger UI está en `http://localhost:8080/swagger-ui/index.html` y la consola H2 en `http://localhost:8080/h2-console`. Al reiniciar la API se recrea la base en memoria con `db/schema.sql` y `db/seed.sql`.

### Frontend

En otra ventana de PowerShell:

```powershell
cd 'C:\Desarrollo de sistemas_codigo\tp-disenio\frontend\capital_humano_consultora'
npm ci
npm run dev
```

Abrir `http://localhost:3000/orden-meritos`. `npm ci` instala las dependencias fijadas por `package-lock.json`; en ejecuciones posteriores alcanza con `npm run dev`. El frontend usa `http://localhost:8080/api` de forma predeterminada. Para usar otra API, definir `NEXT_PUBLIC_API_URL` antes de iniciar Next.js.

## Qué incluye

### CU 028: endpoints de consulta

Todos los endpoints de orden de mérito tienen prefijo `/api`.

| Método y ruta | Función |
| --- | --- |
| `GET /ordenes-merito/empresas` | Lista las empresas que tienen puestos activos. |
| `GET /ordenes-merito/puestos?codigo=&nombrePuesto=&empresa=` | Busca puestos activos. Los filtros son opcionales y se combinan. |
| `GET /ordenes-merito/puestos/{puestoId}/evaluaciones` | Lista los cuestionarios asociados al puesto seleccionado. En esta implementación, cada fila refiere a un cuestionario porque todavía no existe una entidad de evaluación separada. |
| `GET /ordenes-merito/puestos/{puestoId}/reporte?evaluacionIds=1&evaluacionIds=2` | Devuelve los candidatos seleccionados, sus puntajes, cumplimiento de mínimos y estado de mérito. Rechaza IDs ajenos al puesto con HTTP 400. |

Clasificación del reporte:

- `APTO`: cuestionario `COMPLETO` y puntaje igual o superior al mínimo en todas las competencias requeridas.
- `NO_ALCANZA_MINIMOS`: cuestionario completo, con al menos una competencia por debajo del mínimo.
- `PENDIENTE_PUNTAJE`: cuestionario completo sin todos los resultados de competencia.
- `CUESTIONARIO_INCOMPLETO`: cuestionario que todavía no está completo.

Los aptos aparecen primero, ordenados por puntaje total descendente. El total es la suma de los puntajes por competencia. En el frontend se puede buscar por código, nombre y empresa, seleccionar uno o varios cuestionarios, generar el reporte e imprimirlo. La impresión oculta los controles y adapta la tabla al ancho de página.

### Guardado de respuestas y cálculo de resultados

Estos endpoints del cuestionario persisten las respuestas del candidato. Requieren `Authorization: Bearer <token>` y el token debe corresponder al ID del cuestionario de la ruta.

| Método y ruta | Función |
| --- | --- |
| `GET /cuestionarios/{id}/resumen` | Estado, avance, fechas y puntajes ya calculados. |
| `GET /cuestionarios/{id}/preguntas` | Preguntas y opciones del puesto, además de las opciones elegidas previamente. No expone ponderaciones. |
| `POST /cuestionarios/{id}/respuestas` | Guarda respuestas parciales o completa el cuestionario, calcula puntajes y los persiste. |

El cuerpo del POST puede contener un subconjunto de respuestas para guardar el avance; una pregunta y su opción deben pertenecer al cuestionario y no se aceptan IDs repetidos en el mismo envío:

```json
{
  "respuestas": [
    { "preguntaId": 3, "opcionId": 5 },
    { "preguntaId": 4, "opcionId": 8 }
  ]
}
```

Al guardar un subconjunto, el cuestionario queda `EN_PROCESO` y las respuestas se recuperan al volver a consultar las preguntas. Al responder todas, queda `COMPLETO` y se actualiza `resultado_competencia`. El puntaje se normaliza a escala 0–10: suma de ponderaciones elegidas sobre suma de máximos disponibles para las preguntas de esa competencia, multiplicada por 10.

## Prueba automatizada

Desde `api` y con JDK 21 seleccionado en la terminal:

```powershell
.\mvnw.cmd -Dtest=OrdenMeritoControllerTest test
```

Las cuatro pruebas cubren orden de aptos por puntaje, cuestionario incompleto y puntaje pendiente, mínimo no alcanzado y rechazo de IDs de cuestionarios que no pertenecen al puesto. Para ejecutar todas las pruebas del backend:

```powershell
.\mvnw.cmd test
```

También se pueden ejecutar desde IntelliJ con el botón de ejecución de `OrdenMeritoControllerTest.java`.

## Prueba manual del reporte CU 028

Con la API en ejecución, desde PowerShell:

```powershell
Invoke-RestMethod 'http://localhost:8080/api/ordenes-merito/empresas'
Invoke-RestMethod 'http://localhost:8080/api/ordenes-merito/puestos?codigo=DEV'
Invoke-RestMethod 'http://localhost:8080/api/ordenes-merito/puestos/1/evaluaciones'
Invoke-RestMethod 'http://localhost:8080/api/ordenes-merito/puestos/1/reporte?evaluacionIds=3&evaluacionIds=4&evaluacionIds=5'
```

Con el seed recién cargado, los IDs son: puesto `1` = `DEV-01`; cuestionarios `1` a `5` pertenecen a ese puesto. El reporte de `3,4,5` debe mostrar a los cuestionarios 3 y 4 como aptos con puntajes totales 19 y 17, y al 5 como `NO_ALCANZA_MINIMOS` con total 18. El cuestionario 5 no alcanza el mínimo de trabajo en equipo aunque su total sea mayor que el del cuestionario 4: la aptitud requiere cumplir cada mínimo individual.

Para ver además los casos incompletos, incluir los IDs 1 y 2:

```powershell
Invoke-RestMethod 'http://localhost:8080/api/ordenes-merito/puestos/1/reporte?evaluacionIds=1&evaluacionIds=2&evaluacionIds=3&evaluacionIds=4&evaluacionIds=5'
```

El 1 está `ACTIVO` y el 2 `EN_PROCESO`, por lo tanto ambos se clasifican como `CUESTIONARIO_INCOMPLETO`.

Para validar que un cuestionario de otro puesto se rechaza, enviar un ID inexistente o que no pertenezca al puesto:

```powershell
try {
  Invoke-RestMethod 'http://localhost:8080/api/ordenes-merito/puestos/1/reporte?evaluacionIds=999'
} catch {
  $_.Exception.Response.StatusCode
}
```

La respuesta esperada es HTTP 400.

## Prueba manual de autenticación, guardado y reanudación

El candidato seed para probar el guardado es DNI `12345678`, clave `CLAVE123`; su cuestionario es el ID `1` (puesto `DEV-01`). Primero iniciar sesión y conservar el token devuelto:

```powershell
$login = Invoke-RestMethod -Method Post `
  -Uri 'http://localhost:8080/api/auth/candidato' `
  -ContentType 'application/json' `
  -Body '{"tipoDocumento":"DNI","numeroDocumento":"12345678","clave":"CLAVE123"}'
$token = $login.token
$headers = @{ Authorization = "Bearer $token" }
```

Inspeccionar el estado y las preguntas. Los IDs y las opciones del seed son preguntas 3–6, con opciones 5–7, 8–10, 11–13 y 14–16 respectivamente:

```powershell
Invoke-RestMethod 'http://localhost:8080/api/cuestionarios/1/resumen' -Headers $headers
$preguntas = Invoke-RestMethod 'http://localhost:8080/api/cuestionarios/1/preguntas' -Headers $headers
$preguntas | Select-Object id, enunciado, opcionSeleccionadaId
```

Guardar solo parte de las respuestas. Esta llamada debe dejar el cuestionario `EN_PROCESO`:

```powershell
$parcial = '{"respuestas":[{"preguntaId":3,"opcionId":5},{"preguntaId":4,"opcionId":8}]}'
Invoke-RestMethod -Method Post `
  -Uri 'http://localhost:8080/api/cuestionarios/1/respuestas' `
  -Headers $headers -ContentType 'application/json' -Body $parcial
```

Volver a consultar las preguntas y confirmar que se conservaron las opciones. Luego completar las cuatro preguntas:

```powershell
$completo = '{"respuestas":[{"preguntaId":3,"opcionId":5},{"preguntaId":4,"opcionId":8},{"preguntaId":5,"opcionId":11},{"preguntaId":6,"opcionId":14}]}'
Invoke-RestMethod -Method Post `
  -Uri 'http://localhost:8080/api/cuestionarios/1/respuestas' `
  -Headers $headers -ContentType 'application/json' -Body $completo
Invoke-RestMethod 'http://localhost:8080/api/cuestionarios/1/resumen' -Headers $headers
```

El resultado debe ser `COMPLETO`, con cuatro respuestas y puntajes por competencia. Al consultar de nuevo el reporte del puesto 1 incluyendo `evaluacionIds=1`, el candidato 1 debe aparecer como apto si alcanzó cada mínimo.

El endpoint también debe rechazar una pregunta u opción ajena con HTTP 400. Reutilizar el token del cuestionario 1 en `/api/cuestionarios/2/...` debe fallar con HTTP 401. El token y los cambios de las respuestas son de desarrollo; al reiniciar la API se restaura el seed.

## Verificación realizada hasta ahora

- JDK instalado comprobado en `C:\Users\valem\.jdks\openjdk-21.0.2`; `java -version` y `javac -version` informaron 21.0.2.
- Compilación del proyecto/pruebas con IntelliJ completada sin errores.
- `OrdenMeritoControllerTest`: 4 pruebas, 0 fallos y 0 errores. También pasó `mvnw.cmd -Dtest=OrdenMeritoControllerTest test` después de seleccionar temporalmente el JDK 21 en PowerShell.
- Se verificó el flujo de respuestas parciales, reanudación, finalización, cálculo de puntajes y rechazo de token inválido o asociado a otro cuestionario en la API local.
- La página del reporte tiene estilos de impresión. El build/lint de Next.js no está registrado como ejecutado en esta verificación; puede comprobarse desde la carpeta del frontend con `npm run lint` y `npm run build`.

## Datos del seed y límites

- El seed de CU 28 usa puesto `DEV-01` / `DESARROLLADOR SENIOR`, empresa `TECH SOLUTIONS`, con mínimos 8 en comunicación y 9 en trabajo en equipo.
- Los cuestionarios 3, 4 y 5 tienen resultados y respuestas de muestra; los cuestionarios 1 y 2 sirven para probar el flujo activo/en proceso.
- H2 está en memoria. Un reinicio de la API descarta las respuestas guardadas y vuelve a ejecutar el seed.
- Si se envían todas las respuestas para el cuestionario 1, este queda completado. Para repetir exactamente los casos iniciales del seed, reiniciar la API antes de la prueba.
- La integración real de una entidad de evaluación separada no está incluida: la pantalla selecciona cuestionarios individuales asociados al puesto.
