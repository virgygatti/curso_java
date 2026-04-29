# Facturación — API REST (Spring Boot)

API REST para gestión de clientes, productos, comprobantes de venta y líneas. Persistencia en **MySQL** con **JPA**. Documentación interactiva con **Swagger UI** (springdoc-openapi).

## Requisitos

- **JDK 17** o superior (para ejecutar el JAR o compilar).
- **MySQL** con una base llamada `facturacion` y el esquema creado según `sql/schema.sql`.
- Maven opcional si compilás con IntelliJ (usa el Maven integrado).

## Configuración de base de datos

1. Creá la base `facturacion` en MySQL.
2. Ejecutá el script DDL: `sql/schema.sql`.
3. Usuario por defecto en `application.properties`: `root`. Contraseña: definí la variable de entorno **`MYSQL_PASSWORD`** (recomendado) o ajustá los valores en un perfil local sin subirlos al repositorio.

Variables útiles:

| Variable           | Uso                                      |
|--------------------|------------------------------------------|
| `MYSQL_PASSWORD`   | Contraseña del usuario MySQL             |
| `API_BASE_URL`     | Base URL para los scripts `test-api` (por defecto `http://localhost:8080`) |

Puerto HTTP por defecto: **8080**.

## Cómo ejecutar

### Desde IntelliJ

Abrir el proyecto como proyecto Maven, ejecutar la clase principal de Spring Boot (`FacturacionApplication`).

### JAR ejecutable

Generar el artefacto con Maven (**Lifecycle → package**) o:

```bash
mvn -DskipTests package "-Dfacturacion.apellido=TuApellido"
```

El JAR queda en `target/` con nombre:

`FacturacionEntregaProyectoFinal+<Apellido>.jar`

Si no pasás `facturacion.apellido`, el `pom.xml` usa el valor por defecto `DESARROLLO`.

Ejecutar (ajustá el nombre del archivo):

```bash
java -jar target/FacturacionEntregaProyectoFinal+DESARROLLO.jar
```

En Windows PowerShell, antes si hace falta:

```powershell
$env:MYSQL_PASSWORD = "tu_clave"
```

## Documentación de la API

Con la aplicación en marcha:

- **Swagger UI:** http://localhost:8080/swagger-ui/index.html  
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs  

Prefijos de endpoints REST: `/api/clientes`, `/api/productos`, `/api/comprobantes`, `/api/lineas-comprobante`.

Respuestas de error de negocio o validación: **HTTP 409** con cuerpo `{ "errores": [ "..." ] }`.

## Colección Postman

Archivo: `postman/Facturacion.postman_collection.json`

**Importar en Postman:**

1. Abrí Postman.
2. **Import** (botón superior izquierdo).
3. Arrastrá el archivo `Facturacion.postman_collection.json` o elegilo con **Upload Files**.
4. Confirmá **Import**.

La colección define la variable **`baseUrl`** (`http://localhost:8080`). Podés cambiarla en la colección si corrés la API en otro host o puerto.

Actualizá las variables **`clienteId`**, **`productoId`**, **`productoId2`**, **`comprobanteId`** y **`lineaId`** con los valores que devuelvan los POST/GET después de crear datos, o editá los bodies de ejemplo (documentos y códigos únicos) antes de enviar.

## Scripts de prueba HTTP

- **Bash (Git Bash / Linux / macOS):** `scripts/test-api.sh`

Requieren la API levantada. Opcional: `API_BASE_URL=http://127.0.0.1:8080 ./scripts/test-api.sh`

## Estructura relevante del proyecto

| Ruta | Descripción |
|------|-------------|
| `src/main/java/com/facturacion/` | Código de la aplicación |
| `src/main/resources/application.properties` | Configuración Spring |
| `src/main/resources/data.sql` | Datos iniciales (semilla) |
| `sql/schema.sql` | DDL MySQL |
| `postman/` | Colección Postman |
| `scripts/` | Scripts de smoke test contra la API |
