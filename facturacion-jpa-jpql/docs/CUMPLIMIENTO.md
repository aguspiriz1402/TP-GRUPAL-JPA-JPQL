# Correspondencia con la consigna

| Requisito | Implementación |
| --- | --- |
| `EntityId` y `AuditoriaApp` como `@MappedSuperclass` | Ambas clases en `modelo/`; no hay tablas propias para ellas |
| ID con generación `IDENTITY` | `EntityId.id`, heredado por las 14 entidades |
| Entidades con `@Entity` y `@Table` | Usuario, FacturaVenta, FacturaVentaDetalle, PuntoVenta, TipoMoneda, Cliente, CondicionIva, Contacto, Domicilio, ListaPrecioArticulo, Articulo, ListaPrecio, Marca, Rubro |
| Campos escalares obligatorios | `@Column(nullable = false)` en cada campo marcado obligatorio en el modelo |
| Fechas correctamente mapeadas | `LocalDate` para emisión y vencimiento CAE; `LocalDateTime` para auditoría y anulación |
| Auditoría a Usuario | Tres `ManyToOne`; carga y modificación obligatorios, baja opcional |
| Factura a PuntoVenta, CondicionIva y TipoMoneda | `ManyToOne` obligatorios |
| Factura a Cliente | `ManyToOne` opcional como indica el código provisto |
| Detalle a Factura y ListaPrecioArticulo | `ManyToOne` obligatorios |
| ListaPrecioArticulo a ListaPrecio y Articulo | `ManyToOne` obligatorios |
| Articulo a Rubro y Marca | `ManyToOne` opcionales |
| Factura a detalles | `OneToMany(mappedBy = "factura", cascade = ALL)` |
| Cliente a Contacto y Domicilio | `OneToOne` obligatorios con claves foráneas únicas |
| Unidad `FacturacionPU` | `src/main/resources/META-INF/persistence.xml` |
| Proveedor Hibernate | `org.hibernate.jpa.HibernatePersistenceProvider` |
| Conexión MySQL o PostgreSQL | Se elige PostgreSQL, driver JDBC incluido en Maven |
| Propiedades `javax.persistence.jdbc.*` | Driver, URL, usuario y contraseña definidos en XML; sobreescritura opcional por entorno |
| SQL visible, formateado y esquema `update` | Configurados en la unidad principal |
| `public static void main(String[] args)` | `Main.java` |
| Creación del contenedor y EntityManager | `Persistence.createEntityManagerFactory("FacturacionPU", ...)` y `createEntityManager()` |
| Transacción `begin` / `commit` | En `Main`, con `rollback` y cierre de recursos ante errores |
| Crear objetos necesarios | `DatosEjemplo.crearFactura()`, llamado después de iniciar la transacción |
| Asociación bidireccional de cabecera e ítems | `agregarDetalle` sincroniza colección y referencia a factura |
| Un solo `em.persist(facturaVenta)` | Única invocación en `Main`; no hay persistencias ocultas en los datos de ejemplo |
| Verificar que se guardan los detalles | Limpieza del contexto, consulta posterior y comprobación de dos ítems; nueve pruebas adicionales |

Se conserva el modelo de atributos y los tipos numéricos `double`. Las fechas se cambian a `java.time`, alternativa expresamente admitida por el PDF.

Para persistir desde una sola raíz incluso los catálogos nuevos del ejemplo, se incorpora `PERSIST` en las referencias. No se incorpora `REMOVE` hacia datos compartidos. Se añade `orphanRemoval` en la colección de detalles y métodos de ayuda para mantener sus dos extremos sincronizados.

La consigna no requiere interfaz web, API REST, CRUD completo ni integración con ARCA. Los atributos CAE y resultado AFIP están mapeados, pero este ejercicio no implementa la autorización fiscal.
