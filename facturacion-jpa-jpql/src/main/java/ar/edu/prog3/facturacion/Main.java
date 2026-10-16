package ar.edu.prog3.facturacion;

import ar.edu.prog3.facturacion.modelo.FacturaVenta;
import ar.edu.prog3.facturacion.modelo.FacturaVentaDetalle;
import javax.persistence.*;

public final class Main {
    private Main() { }

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory(
                "FacturacionPU", ConfiguracionJpa.desdeEntorno());
        try {
            EntityManager em = emf.createEntityManager();
            try {
                EntityTransaction tx = em.getTransaction();
                Long facturaId;
                try {
                    tx.begin();
                    FacturaVenta facturaVenta = DatosEjemplo.crearFactura();

                    // UNICA llamada a persist: Hibernate recorre las cascadas del grafo.
                    em.persist(facturaVenta);

                    tx.commit();
                    facturaId = facturaVenta.getId();
                } catch (RuntimeException error) {
                    if (tx.isActive()) {
                        tx.rollback();
                    }
                    throw error;
                }

                // Vaciar el contexto obliga a recuperar la factura desde la base de datos.
                em.clear();
                tx.begin();
                try {
                    FacturaVenta recuperada = em.createQuery(
                            "select distinct f from FacturaVenta f left join fetch f.detalles where f.id = :id",
                            FacturaVenta.class).setParameter("id", facturaId).getSingleResult();
                    if (recuperada.getDetalles().size() != 2) {
                        throw new IllegalStateException("La cascada no guardo los dos detalles esperados");
                    }
                    System.out.printf("%nFactura ID=%d | numero=%d | estado=%s%n",
                            recuperada.getId(), recuperada.getNumero(), recuperada.getEstado());
                    for (FacturaVentaDetalle detalle : recuperada.getDetalles()) {
                        System.out.printf("  %s: %.0f x %.2f = %.2f (ID=%d)%n",
                                detalle.getDescripcion(), detalle.getCantidad(), detalle.getPrecioUnitario(),
                                detalle.getImporteSubtotal(), detalle.getId());
                    }
                    System.out.printf("TOTAL: %.2f | SALDO: %.2f%n",
                            recuperada.getImporteTotal(), recuperada.getImporteSaldo());
                    System.out.println("VERIFICADO: factura y 2 detalles recuperados de la base.");
                    tx.commit();
                } catch (RuntimeException error) {
                    if (tx.isActive()) {
                        tx.rollback();
                    }
                    throw error;
                }

                // =================================================================
                // EJECUCIÓN DE LAS 22 CONSULTAS JPQL
                // =================================================================
                ConsultasJpql consultas = new ConsultasJpql(em);
                consultas.ejecutarPruebas();

                
            } finally {
                em.close();
            }
        } finally {
            emf.close();
        }
    }
}
