package ar.edu.prog3.facturacion;

import ar.edu.prog3.facturacion.modelo.*;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class ConsultasJpql {

    private final EntityManager em;

    public ConsultasJpql(EntityManager em) {
        this.em = em;
    }

    // =========================================================================
    // NIVEL 1: Consultas Básicas y Proyecciones
    // =========================================================================

    // PUNTO 1: Obtener la lista completa de todas las facturas de venta
    public List<FacturaVenta> punto1ObtenerTodasLasFacturas() {
        String jpql = "SELECT f FROM FacturaVenta f";
        TypedQuery<FacturaVenta> query = em.createQuery(jpql, FacturaVenta.class);
        return query.getResultList();
    }

    // PUNTO 2: Seleccionar número, fecha de emisión e importe total
    public List<Object[]> punto2ProyeccionAtributosFactura() {
        String jpql = "SELECT f.numero, f.fechaEmision, f.importeTotal FROM FacturaVenta f";
        TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class);
        return query.getResultList();
    }

    // PUNTO 3: Obtener artículos por denominación de su rubro (WHERE)
    public List<Articulo> punto3ArticulosPorRubro(String denominacionRubro) {
        String jpql = "SELECT a FROM Articulo a WHERE a.rubro.denominacion = :denominacionRubro";
        TypedQuery<Articulo> query = em.createQuery(jpql, Articulo.class);
        query.setParameter("denominacionRubro", denominacionRubro);
        return query.getResultList();
    }

    // PUNTO 4: Filtrado por rango de fechas (BETWEEN con LocalDate)
    public List<FacturaVenta> punto4FacturasPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        String jpql = "SELECT f FROM FacturaVenta f WHERE f.fechaEmision BETWEEN :fechaInicio AND :fechaFin";
        TypedQuery<FacturaVenta> query = em.createQuery(jpql, FacturaVenta.class);
        query.setParameter("fechaInicio", fechaInicio);
        query.setParameter("fechaFin", fechaFin);
        return query.getResultList();
    }

    // =========================================================================
    // NIVEL 2: Condicionales Combinados, Operadores de Texto y Agregaciones
    // =========================================================================

    // PUNTO 5: Condicionales complejos y verificación de nulos (AND, IS NULL)
    public List<FacturaVenta> punto5FacturasEmitidasMayoresA10000NoAnuladas() {
        String jpql = "SELECT f FROM FacturaVenta f " +
                      "WHERE f.estado = :estado " +
                      "AND f.importeTotal > :importeMinimo " +
                      "AND f.fechaAnulacion IS NULL";
        TypedQuery<FacturaVenta> query = em.createQuery(jpql, FacturaVenta.class);
        query.setParameter("estado", "EMITIDA");
        query.setParameter("importeMinimo", 10000.0);
        return query.getResultList();
    }

    // PUNTO 6: Búsqueda por patrón de texto (LIKE y LOWER)
    public List<Cliente> punto6BuscarClientesPorDenominacionOCuit(String textoParcial) {
        String jpql = "SELECT c FROM Cliente c " +
                      "WHERE LOWER(c.denominacion) LIKE :patronTexto " +
                      "OR c.cuitCuil LIKE :prefijoCuit";
        TypedQuery<Cliente> query = em.createQuery(jpql, Cliente.class);
        query.setParameter("patronTexto", "%" + textoParcial.toLowerCase() + "%");
        query.setParameter("prefijoCuit", "20-%");
        return query.getResultList();
    }

    // PUNTO 7: Valores distintos y ordenamiento (DISTINCT y ORDER BY)
    public List<String> punto7EstadosDistintosOrdenados() {
        String jpql = "SELECT DISTINCT f.estado FROM FacturaVenta f ORDER BY f.estado ASC";
        TypedQuery<String> query = em.createQuery(jpql, String.class);
        return query.getResultList();
    }

    // PUNTO 8: Funciones de agregación simples (COUNT, SUM, AVG)
    public Object[] punto8EstadisticasFacturas() {
        String jpql = "SELECT COUNT(f), SUM(f.importeTotal), AVG(f.importeTotal) FROM FacturaVenta f";
        TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class);
        return query.getSingleResult();
    }

    // PUNTO 9: Operador de inclusión (IN)
    public List<PuntoVenta> punto9PuntosVentaPorNumeros(List<Integer> numeros) {
        String jpql = "SELECT p FROM PuntoVenta p WHERE p.numero IN :listaNumeros";
        TypedQuery<PuntoVenta> query = em.createQuery(jpql, PuntoVenta.class);
        query.setParameter("listaNumeros", numeros);
        return query.getResultList();
    }

    // =========================================================================
    // NIVEL 3: Navegación de Entidades, JOINs y Subconsultas Simples
    // =========================================================================

    // PUNTO 10: Navegación implícita por relaciones (usuarioCarga.usuario)
    public List<FacturaVenta> punto10FacturasPorUsuarioCarga(String nombreUsuario) {
        String jpql = "SELECT f FROM FacturaVenta f WHERE f.usuarioCarga.usuario = :nombreUsuario";
        TypedQuery<FacturaVenta> query = em.createQuery(jpql, FacturaVenta.class);
        query.setParameter("nombreUsuario", nombreUsuario);
        return query.getResultList();
    }

    // PUNTO 11: Cláusula INNER JOIN explícita (Detalles por número de PuntoVenta)
    public List<FacturaVentaDetalle> punto11DetallesPorPuntoVenta(int numeroPuntoVenta) {
        String jpql = "SELECT d FROM FacturaVentaDetalle d " +
                      "INNER JOIN d.factura f " +
                      "INNER JOIN f.puntoVenta pv " +
                      "WHERE pv.numero = :numeroPuntoVenta";
        TypedQuery<FacturaVentaDetalle> query = em.createQuery(jpql, FacturaVentaDetalle.class);
        query.setParameter("numeroPuntoVenta", numeroPuntoVenta);
        return query.getResultList();
    }

    // PUNTO 12: Cláusula LEFT JOIN (Artículos y su marca, incluyendo sin marca)
    public List<Object[]> punto12ArticulosConYsinMarca() {
        String jpql = "SELECT a.denominacion, m.denominacion " +
                      "FROM Articulo a " +
                      "LEFT JOIN a.marca m";
        TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class);
        return query.getResultList();
    }

    // PUNTO 13: Navegación multinivel con JOINs combinados (Facturas por Marca)
    public List<FacturaVenta> punto13FacturasPorMarcaDeArticulo(String denominacionMarca) {
        String jpql = "SELECT DISTINCT f FROM FacturaVenta f " +
                      "INNER JOIN f.detalles d " +
                      "INNER JOIN d.listaPrecioArticulo lpa " +
                      "INNER JOIN lpa.articulo a " +
                      "INNER JOIN a.marca m " +
                      "WHERE m.denominacion = :denominacionMarca";
        TypedQuery<FacturaVenta> query = em.createQuery(jpql, FacturaVenta.class);
        query.setParameter("denominacionMarca", denominacionMarca);
        return query.getResultList();
    }

    // PUNTO 14: Subconsulta en cláusula WHERE (Facturas mayores al promedio)
    public List<FacturaVenta> punto14FacturasMayoresAlPromedio() {
        String jpql = "SELECT f FROM FacturaVenta f " +
                      "WHERE f.importeTotal > (SELECT AVG(f2.importeTotal) FROM FacturaVenta f2)";
        TypedQuery<FacturaVenta> query = em.createQuery(jpql, FacturaVenta.class);
        return query.getResultList();
    }

    // PUNTO 15: Filtrado por Cliente a través de su CUIT/CUIL
    public List<FacturaVenta> punto15FacturasPorCuitCliente(String cuitCuil) {
        String jpql = "SELECT f FROM FacturaVenta f " +
                      "WHERE f.cliente.cuitCuil = :cuitCuil";
        TypedQuery<FacturaVenta> query = em.createQuery(jpql, FacturaVenta.class);
        query.setParameter("cuitCuil", cuitCuil);
        return query.getResultList();
    }

    // =========================================================================
    // NIVEL 4: Agrupamiento (GROUP BY) y Filtros de Grupo (HAVING)
    // =========================================================================

    // PUNTO 16: Agrupamiento básico (GROUP BY Punto de Venta)
    public List<Object[]> punto16ResumenPorPuntoVenta() {
        String jpql = "SELECT f.puntoVenta.descripcion, COUNT(f), SUM(f.importeTotal) " +
                      "FROM FacturaVenta f " +
                      "GROUP BY f.puntoVenta.descripcion";
        TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class);
        return query.getResultList();
    }

    // PUNTO 17: Agrupamiento con condicional de grupo (HAVING > 5 facturas)
    public List<String> punto17UsuariosConMasDeCincoFacturas() {
        String jpql = "SELECT f.usuarioCarga.nombre " +
                      "FROM FacturaVenta f " +
                      "GROUP BY f.usuarioCarga.id, f.usuarioCarga.nombre " +
                      "HAVING COUNT(f) > :cantidadMinima";
        TypedQuery<String> query = em.createQuery(jpql, String.class);
        query.setParameter("cantidadMinima", 5L);
        return query.getResultList();
    }

    // PUNTO 18: Agrupamiento y agregación sobre entidades relacionadas (Por Marca)
    public List<Object[]> punto18VentasAgrupadasPorMarca() {
        String jpql = "SELECT m.denominacion, SUM(d.cantidad), SUM(d.importeSubtotal) " +
                      "FROM FacturaVentaDetalle d " +
                      "INNER JOIN d.listaPrecioArticulo lpa " +
                      "INNER JOIN lpa.articulo a " +
                      "INNER JOIN a.marca m " +
                      "GROUP BY m.id, m.denominacion";
        TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class);
        return query.getResultList();
    }

    // PUNTO 19: Agrupamiento por Condición de IVA
    public List<Object[]> punto19TotalFacturadoPorCondicionIva() {
        String jpql = "SELECT f.condicionIva.denominacion, SUM(f.importeTotal) " +
                      "FROM FacturaVenta f " +
                      "GROUP BY f.condicionIva.id, f.condicionIva.denominacion";
        TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class);
        return query.getResultList();
    }

    // =========================================================================
    // NIVEL 5: Subconsultas Correlacionadas y Expresiones Condicionales
    // =========================================================================

    // PUNTO 20: Subconsulta correlacionada con EXISTS (Marcas con artículos facturados)
    public List<Marca> punto20MarcasConArticulosFacturados() {
        String jpql = "SELECT m FROM Marca m " +
                      "WHERE EXISTS (" +
                      "    SELECT d FROM FacturaVentaDetalle d " +
                      "    WHERE d.listaPrecioArticulo.articulo.marca = m" +
                      ")";
        TypedQuery<Marca> query = em.createQuery(jpql, Marca.class);
        return query.getResultList();
    }

    // PUNTO 21: Subconsulta correlacionada con NOT EXISTS (Artículos nunca vendidos)
    public List<Articulo> punto21ArticulosNuncaFacturados() {
        String jpql = "SELECT a FROM Articulo a " +
                      "WHERE NOT EXISTS (" +
                      "    SELECT d FROM FacturaVentaDetalle d " +
                      "    WHERE d.listaPrecioArticulo.articulo = a" +
                      ")";
        TypedQuery<Articulo> query = em.createQuery(jpql, Articulo.class);
        return query.getResultList();
    }

    // PUNTO 22: Proyección condicional (CASE WHEN) y ordenamiento DESC
    public List<Object[]> punto22ClasificacionFacturasPorValor() {
        String jpql = "SELECT f.numero, f.importeTotal, " +
                      "CASE " +
                      "    WHEN f.importeTotal > :limiteAlto THEN 'ALTO VALOR' " +
                      "    WHEN f.importeTotal >= :limiteBajo AND f.importeTotal <= :limiteAlto THEN 'MEDIO VALOR' " +
                      "    ELSE 'BAJO VALOR' " +
                      "END " +
                      "FROM FacturaVenta f " +
                      "ORDER BY f.importeTotal DESC";
        TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class);
        query.setParameter("limiteAlto", 50000.0);
        query.setParameter("limiteBajo", 10000.0);
        return query.getResultList();
    }

    // =========================================================================
    // MÉTODO PARA PROBAR LAS 22 CONSULTAS CON LOS DATOS DE "DatosEjemplo.java"
    // =========================================================================
    public void ejecutarPruebas() {
        System.out.println("\n================ INICIO PRUEBAS JPQL ================");

        System.out.println("\n--- Punto 1: Todas las facturas ---");
        punto1ObtenerTodasLasFacturas().forEach(f -> 
            System.out.println("Factura N°: " + f.getNumero() + " | Total: $" + f.getImporteTotal()));

        System.out.println("\n--- Punto 2: Proyección (Número, Fecha, Total) ---");
        punto2ProyeccionAtributosFactura().forEach(fila -> 
            System.out.println("N°: " + fila[0] + " | Fecha: " + fila[1] + " | Total: $" + fila[2]));

        // Ajustado a "Libreria" según DatosEjemplo
        System.out.println("\n--- Punto 3: Artículos por Rubro ('Libreria') ---");
        punto3ArticulosPorRubro("Libreria").forEach(a -> 
            System.out.println("Artículo: " + a.getDenominacion()));

        // Ajustado a LocalDate según DatosEjemplo
        System.out.println("\n--- Punto 4: Facturas por rango de fechas ---");
        LocalDate haceUnMes = LocalDate.now().minusMonths(1);
        LocalDate hoy = LocalDate.now();
        punto4FacturasPorRangoFechas(haceUnMes, hoy).forEach(f -> 
            System.out.println("Factura N°: " + f.getNumero() + " | Fecha: " + f.getFechaEmision()));

        System.out.println("\n--- Punto 5: Emitidas, > $10.000 y no anuladas ---");
        punto5FacturasEmitidasMayoresA10000NoAnuladas().forEach(f -> 
            System.out.println("Factura N°: " + f.getNumero() + " | Estado: " + f.getEstado() + " | Total: $" + f.getImporteTotal()));

        // Ajustado a "ejemplo" para que coincida con "Cliente de ejemplo"
        System.out.println("\n--- Punto 6: Clientes por texto ('ejemplo') o CUIT '20-' ---");
        punto6BuscarClientesPorDenominacionOCuit("ejemplo").forEach(c -> 
            System.out.println("Cliente: " + c.getDenominacion() + " | CUIT: " + c.getCuitCuil()));

        System.out.println("\n--- Punto 7: Estados distintos ordenados ---");
        punto7EstadosDistintosOrdenados().forEach(estado -> 
            System.out.println("Estado: " + estado));

        System.out.println("\n--- Punto 8: Estadísticas (COUNT, SUM, AVG) ---");
        Object[] stats = punto8EstadisticasFacturas();
        System.out.println("Cantidad: " + stats[0] + " | Suma Total: $" + stats[1] + " | Promedio: $" + stats[2]);

        System.out.println("\n--- Punto 9: Puntos de venta IN (1, 2, 5) ---");
        punto9PuntosVentaPorNumeros(Arrays.asList(1, 2, 5)).forEach(pv -> 
            System.out.println("Punto Venta N°: " + pv.getNumero() + " - " + pv.getDescripcion()));

        // Ajustado a "alumno" según DatosEjemplo
        System.out.println("\n--- Punto 10: Facturas por usuario de carga ('alumno') ---");
        punto10FacturasPorUsuarioCarga("alumno").forEach(f -> 
            System.out.println("Factura N°: " + f.getNumero()));

        System.out.println("\n--- Punto 11: Detalles por Punto de Venta N° 1 (INNER JOIN) ---");
        punto11DetallesPorPuntoVenta(1).forEach(d -> 
            System.out.println("Detalle: " + d.getDescripcion() + " | Cant: " + d.getCantidad() + " | Subtotal: $" + d.getImporteSubtotal()));

        System.out.println("\n--- Punto 12: Artículos con y sin marca (LEFT JOIN) ---");
        punto12ArticulosConYsinMarca().forEach(fila -> 
            System.out.println("Artículo: " + fila[0] + " | Marca: " + (fila[1] != null ? fila[1] : "SIN MARCA")));

        // Ajustado a "Marca del TP" según DatosEjemplo
        System.out.println("\n--- Punto 13: Facturas por Marca de artículo ('Marca del TP') ---");
        punto13FacturasPorMarcaDeArticulo("Marca del TP").forEach(f -> 
            System.out.println("Factura N°: " + f.getNumero()));

        System.out.println("\n--- Punto 14: Facturas mayores al promedio ---");
        punto14FacturasMayoresAlPromedio().forEach(f -> 
            System.out.println("Factura N°: " + f.getNumero() + " | Total: $" + f.getImporteTotal()));

        // Ajustado a "20000000001" según DatosEjemplo
        System.out.println("\n--- Punto 15: Facturas por CUIT de Cliente ('20000000001') ---");
        punto15FacturasPorCuitCliente("20000000001").forEach(f -> 
            System.out.println("Factura N°: " + f.getNumero()));

        System.out.println("\n--- Punto 16: Resumen por Punto de Venta (GROUP BY) ---");
        punto16ResumenPorPuntoVenta().forEach(fila -> 
            System.out.println("PV: " + fila[0] + " | Cant. Facturas: " + fila[1] + " | Total: $" + fila[2]));

        System.out.println("\n--- Punto 17: Usuarios con más de 5 facturas (HAVING) ---");
        punto17UsuariosConMasDeCincoFacturas().forEach(nombre -> 
            System.out.println("Usuario: " + nombre));

        System.out.println("\n--- Punto 18: Ventas agrupadas por Marca ---");
        punto18VentasAgrupadasPorMarca().forEach(fila -> 
            System.out.println("Marca: " + fila[0] + " | Unidades: " + fila[1] + " | Subtotal: $" + fila[2]));

        System.out.println("\n--- Punto 19: Total facturado por Condición de IVA ---");
        punto19TotalFacturadoPorCondicionIva().forEach(fila -> 
            System.out.println("Condición IVA: " + fila[0] + " | Total: $" + fila[1]));

        System.out.println("\n--- Punto 20: Marcas con artículos facturados (EXISTS) ---");
        punto20MarcasConArticulosFacturados().forEach(m -> 
            System.out.println("Marca: " + m.getDenominacion()));

        System.out.println("\n--- Punto 21: Artículos nunca facturados (NOT EXISTS) ---");
        punto21ArticulosNuncaFacturados().forEach(a -> 
            System.out.println("Artículo sin ventas: " + a.getDenominacion()));

        System.out.println("\n--- Punto 22: Clasificación CASE WHEN ---");
        punto22ClasificacionFacturasPorValor().forEach(fila -> 
            System.out.println("Factura N°: " + fila[0] + " | Total: $" + fila[1] + " | Categoría: " + fila[2]));

        System.out.println("\n================ FIN PRUEBAS JPQL ================\n");
    }
}