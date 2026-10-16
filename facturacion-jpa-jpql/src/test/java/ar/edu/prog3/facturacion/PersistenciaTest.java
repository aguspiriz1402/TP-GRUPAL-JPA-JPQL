package ar.edu.prog3.facturacion;

import ar.edu.prog3.facturacion.modelo.*;
import org.junit.jupiter.api.*;
import javax.persistence.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/** Prueba el mismo mapeo con H2, o con una base PostgreSQL descartable indicada por propiedades. */
class PersistenciaTest {
    private EntityManagerFactory emf;
    private EntityManager em;

    @BeforeEach
    void abrirBaseAislada() {
        Map<String, Object> propiedades = new HashMap<>();
        String url = System.getProperty("test.db.url");
        if (url == null) {
            propiedades.put("javax.persistence.jdbc.driver", "org.h2.Driver");
            propiedades.put("javax.persistence.jdbc.url", "jdbc:h2:mem:tp_" + UUID.randomUUID()
                    + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE");
            propiedades.put("javax.persistence.jdbc.user", "sa");
            propiedades.put("javax.persistence.jdbc.password", "");
            propiedades.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        } else {
            propiedades.put("javax.persistence.jdbc.url", url);
            propiedades.put("javax.persistence.jdbc.user", System.getProperty("test.db.user", "postgres"));
            propiedades.put("javax.persistence.jdbc.password", System.getProperty("test.db.password", ""));
        }
        // Exclusivo de pruebas: la base indicada debe ser descartable.
        propiedades.put("hibernate.hbm2ddl.auto", "create-drop");
        propiedades.put("hibernate.show_sql", "false");
        emf = Persistence.createEntityManagerFactory("FacturacionPU", propiedades);
        em = emf.createEntityManager();
    }

    @AfterEach
    void cerrar() {
        if (em != null && em.isOpen()) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    @Test
    void unaSolaPersistenciaGuardaTodoElGrafoYPermiteRecuperarlo() {
        FacturaVenta factura = DatosEjemplo.crearFactura();
        guardar(factura);
        Long id = factura.getId();
        em.clear();
        em.getTransaction().begin();
        FacturaVenta recuperada = em.find(FacturaVenta.class, id);
        assertNotSame(factura, recuperada);
        assertEquals(2, recuperada.getDetalles().size());
        assertEquals(3551.00, recuperada.getImporteTotal(), 0.00001);
        assertEquals(3551.00, recuperada.getImporteSaldo(), 0.00001);
        assertEquals(LocalDate.now(), recuperada.getFechaEmision());
        assertNotNull(recuperada.getFechaAlta());
        assertNotNull(recuperada.getFechaModificacion());
        assertNull(recuperada.getFechaBaja());
        assertNull(recuperada.getUsuarioBaja());
        assertEquals("alumno", recuperada.getUsuarioCarga().getUsuario());
        for (FacturaVentaDetalle detalle : recuperada.getDetalles()) {
            assertNotNull(detalle.getId());
            assertSame(recuperada, detalle.getFactura());
            assertNotNull(detalle.getListaPrecioArticulo().getArticulo().getId());
        }
        assertEquals("cliente@example.com", recuperada.getCliente().getContacto().getEmail());
        assertEquals("Belgrano", recuperada.getCliente().getDomicilio().getNombreCalle());
        assertEquals(1, contar("FacturaVenta"));
        assertEquals(2, contar("FacturaVentaDetalle"));
        assertEquals(2, contar("Articulo"));
        assertEquals(2, contar("ListaPrecioArticulo"));
        for (String entidad : new String[]{"Usuario", "PuntoVenta", "TipoMoneda", "CondicionIva",
                "Cliente", "Contacto", "Domicilio", "Marca", "Rubro", "ListaPrecio"}) {
            assertEquals(1, contar(entidad), entidad);
        }
        em.getTransaction().commit();
    }

    @Test
    void unDatoObligatorioFaltanteRevierteTambienLasInsercionesEnCascada() {
        FacturaVenta factura = DatosEjemplo.crearFactura();
        factura.setEstado(null);
        assertThrows(PersistenceException.class, () -> guardar(factura));
        em.clear();
        em.getTransaction().begin();
        assertEquals(0, contar("FacturaVenta"));
        assertEquals(0, contar("FacturaVentaDetalle"));
        assertEquals(0, contar("Usuario"));
        assertEquals(0, contar("Articulo"));
        em.getTransaction().commit();
    }

    @Test
    void laBaseRechazaNullEnColumnasObligatoriasInclusoUsandoSqlDirecto() {
        guardar(DatosEjemplo.crearFactura());
        em.getTransaction().begin();
        assertThrows(PersistenceException.class, () -> em.createNativeQuery(
                "update articulos set denominacion = null").executeUpdate());
        em.getTransaction().rollback();
        em.clear();
        em.getTransaction().begin();
        assertEquals(0L, em.createQuery("select count(a) from Articulo a where a.denominacion is null",
                Long.class).getSingleResult());
        em.getTransaction().commit();
    }

    @Test
    void clienteMarcaYRubroPuedenSerNulosSegunLaConsigna() {
        FacturaVenta factura = DatosEjemplo.crearFactura();
        factura.setCliente(null);
        for (FacturaVentaDetalle detalle : factura.getDetalles()) {
            detalle.getListaPrecioArticulo().getArticulo().setMarca(null);
            detalle.getListaPrecioArticulo().getArticulo().setRubro(null);
        }
        guardar(factura);
        em.clear();
        em.getTransaction().begin();
        FacturaVenta recuperada = em.find(FacturaVenta.class, factura.getId());
        assertNull(recuperada.getCliente());
        assertNull(recuperada.getDetalles().get(0).getListaPrecioArticulo().getArticulo().getMarca());
        assertNull(recuperada.getDetalles().get(0).getListaPrecioArticulo().getArticulo().getRubro());
        assertEquals(0, contar("Cliente"));
        assertEquals(0, contar("Marca"));
        assertEquals(0, contar("Rubro"));
        em.getTransaction().commit();
    }

    @Test
    void oneToOneImpideQueDosClientesCompartanContactoODomicilio() {
        FacturaVenta factura = DatosEjemplo.crearFactura();
        guardar(factura);
        Cliente otro = new Cliente();
        otro.setCuitCuil("20000000002");
        otro.setDenominacion("Segundo cliente");
        otro.setContacto(factura.getCliente().getContacto());
        otro.setDomicilio(factura.getCliente().getDomicilio());
        otro.registrarAlta(factura.getUsuarioCarga());
        assertThrows(PersistenceException.class, () -> guardar(otro));
        em.clear();
        em.getTransaction().begin();
        assertEquals(1, contar("Cliente"));
        em.getTransaction().commit();
    }

    @Test
    void borrarFacturaBorraDetallesPeroConservaLosDatosCompartidos() {
        FacturaVenta factura = DatosEjemplo.crearFactura();
        guardar(factura);
        em.clear();
        em.getTransaction().begin();
        em.remove(em.find(FacturaVenta.class, factura.getId()));
        em.getTransaction().commit();
        em.clear();
        em.getTransaction().begin();
        assertEquals(0, contar("FacturaVenta"));
        assertEquals(0, contar("FacturaVentaDetalle"));
        assertEquals(2, contar("Articulo"));
        assertEquals(2, contar("ListaPrecioArticulo"));
        for (String entidad : new String[]{"Usuario", "PuntoVenta", "TipoMoneda", "CondicionIva",
                "Cliente", "Contacto", "Domicilio", "Marca", "Rubro", "ListaPrecio"}) {
            assertEquals(1, contar(entidad), entidad);
        }
        em.getTransaction().commit();
    }

    @Test
    void quitarUnDetalleEliminaElHuerfanoYConservaElOtro() {
        FacturaVenta factura = DatosEjemplo.crearFactura();
        guardar(factura);
        em.clear();
        em.getTransaction().begin();
        FacturaVenta recuperada = em.find(FacturaVenta.class, factura.getId());
        FacturaVentaDetalle quitado = recuperada.getDetalles().get(0);
        Long quitadoId = quitado.getId();
        recuperada.quitarDetalle(quitado);
        assertNull(quitado.getFactura());
        recuperada.recalcularTotal();
        double totalEsperado = recuperada.getDetalles().get(0).getImporteSubtotal();
        em.getTransaction().commit();
        em.clear();
        em.getTransaction().begin();
        assertNull(em.find(FacturaVentaDetalle.class, quitadoId));
        assertEquals(1, contar("FacturaVentaDetalle"));
        assertEquals(totalEsperado, em.find(FacturaVenta.class, factura.getId()).getImporteTotal(), 0.00001);
        assertEquals(2, contar("Articulo"));
        em.getTransaction().commit();
    }

    @Test
    void rollbackExplicitoDeshaceLaFacturaYTodoElGrafoNuevo() {
        em.getTransaction().begin();
        em.persist(DatosEjemplo.crearFactura());
        em.flush();
        em.getTransaction().rollback();
        em.clear();
        em.getTransaction().begin();
        for (String entidad : new String[]{"FacturaVenta", "FacturaVentaDetalle", "Usuario", "Articulo",
                "Cliente", "Contacto", "Domicilio", "ListaPrecioArticulo"}) {
            assertEquals(0, contar(entidad), entidad);
        }
        em.getTransaction().commit();
    }

    @Test
    void otraFacturaReutilizaReferenciasAdministradasSinDuplicarlas() {
        FacturaVenta primera = DatosEjemplo.crearFactura();
        guardar(primera);
        em.clear();
        em.getTransaction().begin();
        FacturaVenta existente = em.find(FacturaVenta.class, primera.getId());
        FacturaVenta segunda = new FacturaVenta();
        segunda.setNumero(2L);
        segunda.setFechaEmision(LocalDate.now());
        segunda.setEstado("BORRADOR");
        segunda.setCliente(existente.getCliente());
        segunda.setCondicionIva(existente.getCondicionIva());
        segunda.setTipoMoneda(existente.getTipoMoneda());
        segunda.setPuntoVenta(existente.getPuntoVenta());
        segunda.registrarAlta(existente.getUsuarioCarga());
        FacturaVentaDetalle detalle = new FacturaVentaDetalle();
        ListaPrecioArticulo precio = existente.getDetalles().get(0).getListaPrecioArticulo();
        detalle.setListaPrecioArticulo(precio);
        detalle.setCantidad(1);
        detalle.setPrecioUnitario(precio.getPrecioVenta());
        detalle.setImporteNeto(precio.getPrecioVenta());
        detalle.setImporteSubtotal(precio.getPrecioVenta());
        segunda.agregarDetalle(detalle);
        segunda.recalcularTotal();
        em.persist(segunda);
        em.getTransaction().commit();
        em.clear();
        em.getTransaction().begin();
        assertEquals(2, contar("FacturaVenta"));
        assertEquals(3, contar("FacturaVentaDetalle"));
        assertEquals(2, contar("Articulo"));
        assertEquals(2, contar("ListaPrecioArticulo"));
        assertEquals(1, contar("Cliente"));
        assertEquals(1, contar("Usuario"));
        em.getTransaction().commit();
    }

    private void guardar(Object entidad) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(entidad);
            tx.commit();
        } catch (RuntimeException error) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw error;
        }
    }

    private long contar(String entidad) {
        return em.createQuery("select count(e) from " + entidad + " e", Long.class).getSingleResult();
    }
}
