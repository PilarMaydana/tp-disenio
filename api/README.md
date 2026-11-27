# Backend - Capit@l Humano API

API REST desarrollada con Spring Boot y base de datos H2 en memoria.

## Cómo levantar

Para iniciar la aplicación:

En Linux / macOS:
```bash
./mvnw spring-boot:run
```

En Windows:
```cmd
mvnw.cmd spring-boot:run
```

### Base de datos

- La base de datos es **H2 en memoria**, por lo que los datos se recrean en cada arranque ejecutando automáticamente `schema.sql` y `seed.sql` ubicados en `src/main/resources/db/`.
- La consola web de H2 está habilitada en: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  - **JDBC URL:** `jdbc:h2:mem:capitalhumano`
  - **Usuario:** `sa`
  - **Contraseña:** *(vacía / sin contraseña)*

### Documentación de la API

- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **Endpoint de verificación (Ping):** [http://localhost:8080/api/ping](http://localhost:8080/api/ping)

### Orden de mérito (CU 028)

- `GET /api/ordenes-merito/puestos/{puestoId}/reporte?evaluacionIds={id}` devuelve los candidatos clasificados para las evaluaciones seleccionadas.
- Cada candidato con estado `NO_ALCANZA_MINIMOS` incluye `minimosNoAlcanzados`, una lista con la competencia, el puntaje obtenido y el puntaje mínimo requerido. La interfaz presenta este detalle en una tabla dentro de la sección «No alcanzan los mínimos requeridos».
- Para ejecutar las pruebas del clasificador, desde `api/` usar `./mvnw -Dtest=ClasificadorMeritoTest test` (Windows: `mvnw.cmd -Dtest=ClasificadorMeritoTest test`).

### Producción

Iniciar con `SPRING_PROFILES_ACTIVE=prod` y configurar estas variables antes de arrancar. La aplicación no inicia si faltan valores obligatorios, el LDAP no usa LDAPS o la clave JWT tiene menos de 32 caracteres.

| Variable | Uso |
|---|---|
| `APP_JWT_SECRET` | Secreto aleatorio de al menos 32 caracteres para firmar JWT. |
| `APP_LDAP_URL` | URL del directorio LDAP, por ejemplo `ldaps://ldap.ejemplo.org:636`. |
| `APP_LDAP_USER_DN_PATTERN` | DN del usuario con `{0}` como marcador, por ejemplo `uid={0},ou=personas,dc=ejemplo,dc=org`. |
| `APP_CORS_ALLOWED_ORIGINS` | Orígenes permitidos del frontend, separados por coma si hay más de uno. |

El patrón DN depende del directorio institucional y debe coincidir con su estructura. Las credenciales del usuario se validan mediante un bind LDAP; no se guardan en la aplicación. En producción se deshabilitan H2 Console y Swagger.
