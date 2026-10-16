# Trabajo práctico: ORM, JPA y Hibernate

Aplicación Java de consola que implementa las 14 entidades de la consigna y guarda una factura con dos detalles mediante **una sola llamada a `em.persist(facturaVenta)`**.

La explicación del diseño y de cada paso está en [GUIA-EXPLICADA.md](docs/GUIA-EXPLICADA.md). La correspondencia con la consigna está en [CUMPLIMIENTO.md](docs/CUMPLIMIENTO.md).

La compilación, la demo y las nueve pruebas se verificaron tanto con H2 como con PostgreSQL 16.4. Los resultados están en [VERIFICACION.md](docs/VERIFICACION.md).

## Requisitos

- JDK 17 o 21. El código se compila para Java 17.
- PowerShell en Windows, o Maven 3.9+ si se utiliza otro sistema.
- Internet en la primera ejecución para descargar dependencias.
- PostgreSQL para ejecutar `Main`. Las pruebas y la demo H2 no lo necesitan.

`mvnw.ps1` descarga Maven 3.9.11 dentro de `.tools`, valida su SHA-512 y guarda las dependencias en esa misma carpeta. No instala Maven globalmente. Si ya tenés Maven, podés reemplazar `./mvnw.ps1` por `mvn` en los comandos.

## Probar sin instalar una base de datos

Abrí PowerShell en esta carpeta:

```powershell
cd C:\Proyects\DriagonSystems\facturacion-jpa
.\mvnw.ps1 test
```

Se ejecutan nueve pruebas de integración contra H2 en memoria, usando las mismas entidades y la misma unidad de persistencia, con propiedades de conexión sobreescritas para pruebas. Los datos desaparecen al terminar.

Para ver los `CREATE TABLE`, `INSERT` y la factura recuperada:

```powershell
.\mvnw.ps1 test-compile exec:java '-Dexec.mainClass=ar.edu.prog3.facturacion.DemoMemoria' '-Dexec.classpathScope=test'
```

La última línea de la demo contiene `DEMO H2: factura ID=1, detalles=2, total=3551,00` (el separador decimal depende del idioma del sistema).

Si PowerShell bloquea scripts por su política local, se puede ejecutar este lanzador en un proceso puntual, sin cambiar la política del sistema:

```powershell
powershell -ExecutionPolicy Bypass -File .\mvnw.ps1 test
```

## Ejecutar con PostgreSQL

Opción A: si tenés Docker Desktop, desde esta carpeta:

```powershell
$env:DB_PASSWORD = 'elegi-una-clave-local'
docker compose up -d --wait
```

Esto crea la base `facturacion`, el usuario `facturacion` y un volumen para conservar los datos. `docker compose down` detiene el servidor; el volumen se conserva.

Opción B: usá una instalación PostgreSQL propia. Creá un usuario con permiso para crear tablas en su esquema y una base vacía. Por ejemplo, desde una sesión administrativa de `psql`:

```sql
CREATE USER facturacion WITH PASSWORD 'elegi-una-clave-local';
CREATE DATABASE facturacion OWNER facturacion;
```

Luego, en la misma terminal donde vas a ejecutar Java:

```powershell
$env:DB_URL = 'jdbc:postgresql://localhost:5432/facturacion'
$env:DB_USER = 'facturacion'
$env:DB_PASSWORD = 'elegi-una-clave-local'
.\mvnw.ps1 compile exec:java
```

La URL y el usuario anteriores ya son los valores predeterminados en `persistence.xml`; `DB_PASSWORD` aporta la contraseña. No se lee automáticamente un archivo `.env` desde Java: se usan variables de entorno de la terminal. Docker Compose sí admite su propio `.env`.

Salida relevante, además del SQL generado por Hibernate:

```text
Factura ID=1 | numero=1 | estado=BORRADOR
  Cuaderno: 2 x 1250,50 = 2501,00 (ID=1)
  Lapicera: 3 x 350,00 = 1050,00 (ID=2)
TOTAL: 3551,00 | SALDO: 3551,00
VERIFICADO: factura y 2 detalles recuperados de la base.
```

Los IDs, el orden de los detalles y el separador decimal pueden variar. Podés inspeccionar las tablas con [sql/verificar.sql](sql/verificar.sql), desde pgAdmin o `psql`.

**Cada ejecución de `Main` crea un ejemplo nuevo**, incluidos sus datos de catálogo. No implementa una numeración fiscal ni busca registros existentes. Para aprender a reutilizar referencias, hay una prueba específica con dos facturas que comparten cliente, usuario y artículos.

## Estructura

```text
src/main/java/ar/edu/prog3/facturacion/
  Main.java                Transacción, único persist y consulta de comprobación
  DatosEjemplo.java        Construcción del grafo de objetos de demostración
  ConfiguracionJpa.java    Variables de entorno para la conexión
  modelo/                  14 entidades y 2 superclases mapeadas
src/main/resources/META-INF/persistence.xml
src/test/java/ar/edu/prog3/facturacion/
  PersistenciaTest.java     9 pruebas de integración
  DemoMemoria.java          Demostración alternativa con H2
compose.yaml               PostgreSQL opcional mediante Docker
sql/verificar.sql          Consultas para comprobar los registros
docs/                      Explicación y correspondencia con la consigna
```

## Decisiones de compatibilidad y alcance

- JPA 2.2 (`javax.persistence`) e Hibernate 5.6.15.Final para mantener el espacio de nombres que pide el PDF. No mezclar estas importaciones con `jakarta.persistence` de Hibernate 6/7 sin migrar el proyecto completo.
- PostgreSQL como motor principal. H2 es una alternativa para demostración y pruebas; no reemplaza la configuración de PostgreSQL solicitada.
- Fechas con `LocalDate` y `LocalDateTime`, opción permitida por la consigna.
- Se conservan los campos `double` del modelo docente. En una aplicación de dinero real conviene migrar los importes a `BigDecimal` y columnas `NUMERIC`; el redondeo de la suma en esta demo no elimina las limitaciones de `double`.
- El TP demuestra persistencia. No autentica usuarios, no liquida impuestos, no valida CUIT ante ARCA ni solicita CAE. La clave del usuario y los datos de cliente son ficticios; los campos de respuesta fiscal quedan vacíos.

## Pruebas con una base PostgreSQL descartable

Las mismas nueve pruebas admiten este comando:

```powershell
.\mvnw.ps1 test '-Dtest.db.url=jdbc:postgresql://localhost:5432/facturacion_test' '-Dtest.db.user=facturacion' '-Dtest.db.password=clave-de-la-base-test'
```

**Usar exclusivamente una base de pruebas vacía:** este comando utiliza `create-drop`, que crea y elimina las tablas del modelo. `Main`, en cambio, conserva `hibernate.hbm2ddl.auto=update` como exige la consigna.
