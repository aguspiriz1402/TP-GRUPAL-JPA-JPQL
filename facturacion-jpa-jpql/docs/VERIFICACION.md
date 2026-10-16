# Verificación realizada

Fecha: 14 de octubre de 2026, zona horaria America/Buenos_Aires.

Entorno: Windows, JDK 21.0.11, Maven 3.9.11 y compilación con `--release 17`.

| Comprobación | Resultado |
| --- | --- |
| Compilación y empaquetado con `package` | `BUILD SUCCESS`; generado `target/facturacion-jpa-1.0.0.jar` |
| Nueve pruebas de integración con H2 2.3.232 | 9 ejecutadas, 0 fallos, 0 errores, 0 omitidas |
| Demo alternativa H2 | Factura ID 1, dos detalles, total 3551,00 |
| Las mismas nueve pruebas con PostgreSQL 16.4 | 9 ejecutadas, 0 fallos, 0 errores, 0 omitidas |
| `Main` con PostgreSQL y configuración `update` | Factura y dos detalles recuperados después de vaciar el contexto JPA |
| Lanzador PowerShell | Validada su selección de JDK y ejecución de Maven |

Salida de `Main` observada con PostgreSQL:

```text
Factura ID=1 | numero=1 | estado=BORRADOR
  Cuaderno: 2 x 1250,50 = 2501,00 (ID=1)
  Lapicera: 3 x 350,00 = 1050,00 (ID=2)
TOTAL: 3551,00 | SALDO: 3551,00
VERIFICADO: factura y 2 detalles recuperados de la base.
[INFO] BUILD SUCCESS
```

Las pruebas de PostgreSQL se ejecutaron en un clúster descartable, limitado a localhost y detenido al terminar. Se usaron los binarios de prueba `io.zonky.test.postgres:embedded-postgres-binaries-windows-amd64:16.4.0`, sin agregarlos como dependencia del proyecto ni instalar un servicio de Windows.

Los errores de restricción provocados deliberadamente por las pruebas (columna obligatoria nula y contacto duplicado) fueron capturados como resultados esperados. El informe final de JUnit no registró fallos.

El archivo `compose.yaml` se entrega como opción para quien tenga Docker. No se ejecutó Docker en este entorno; la comprobación con PostgreSQL se hizo con el motor nativo temporal.

El ZIP de entrega contiene código, configuración, lanzador y documentación. Excluye las descargas `.tools`, los datos temporales de PostgreSQL y el directorio `target`. El JAR es un empaquetado estándar de clases, no un ejecutable con dependencias incluidas; el comando recomendado para ejecutar sigue siendo `compile exec:java`.
