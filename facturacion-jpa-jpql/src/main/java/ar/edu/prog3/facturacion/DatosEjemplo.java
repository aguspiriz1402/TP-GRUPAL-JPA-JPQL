package ar.edu.prog3.facturacion;

import ar.edu.prog3.facturacion.modelo.*;
import java.time.LocalDate;

/** Construye objetos nuevos en memoria. No accede a la base ni llama a persist(). */
public final class DatosEjemplo {
    private DatosEjemplo() { }

    public static FacturaVenta crearFactura() {
        Usuario usuario = new Usuario();
        usuario.setUsuario("alumno");
        // Dato ficticio del TP: no hay autenticacion ni usuarios reales.
        usuario.setClave("SIN-AUTENTICACION-DEMO");
        usuario.setNombre("Ana");
        usuario.setApellido("Perez");

        PuntoVenta puntoVenta = new PuntoVenta();
        puntoVenta.setNumero(1);
        puntoVenta.setDescripcion("Punto de venta del TP");
        puntoVenta.setTipoEmision("DEMO");
        puntoVenta.setDomicilioComercial("San Martin 100");
        puntoVenta.registrarAlta(usuario);

        TipoMoneda moneda = new TipoMoneda();
        moneda.setCodigoAfip("PES");
        moneda.setDenominacion("Peso argentino");
        moneda.setSimbolo("$");
        moneda.registrarAlta(usuario);

        CondicionIva condicion = new CondicionIva();
        condicion.setCodigoAfip(5);
        condicion.setDenominacion("Consumidor final");
        condicion.registrarAlta(usuario);

        Contacto contacto = new Contacto();
        contacto.setEmail("cliente@example.com");
        contacto.setTelefono("011-0000-0000");
        contacto.setCelular("011-0000-0001");

        Domicilio domicilio = new Domicilio();
        domicilio.setNombreCalle("Belgrano");
        domicilio.setNumeroCalle("200");

        Cliente cliente = new Cliente();
        cliente.setCuitCuil("20000000001"); // Identificador ficticio; no se valida ante ARCA.
        cliente.setDenominacion("Cliente de ejemplo");
        cliente.setContacto(contacto);
        cliente.setDomicilio(domicilio);
        cliente.registrarAlta(usuario);

        Marca marca = new Marca();
        marca.setCodigo(1);
        marca.setDenominacion("Marca del TP");
        marca.registrarAlta(usuario);

        Rubro rubro = new Rubro();
        rubro.setCodigo(1);
        rubro.setDenominacion("Libreria");
        rubro.registrarAlta(usuario);

        ListaPrecio lista = new ListaPrecio();
        lista.setCodigo("MINORISTA");
        lista.setDenominacion("Lista minorista");
        lista.registrarAlta(usuario);

        ListaPrecioArticulo cuaderno = crearPrecio("CUA-001", "Cuaderno", 1250.50,
                lista, marca, rubro, usuario);
        ListaPrecioArticulo lapicera = crearPrecio("LAP-001", "Lapicera", 350.00,
                lista, marca, rubro, usuario);

        FacturaVenta factura = new FacturaVenta();
        factura.setNumero(1L);
        factura.setFechaEmision(LocalDate.now());
        factura.setCliente(cliente);
        factura.setCondicionIva(condicion);
        factura.setTipoMoneda(moneda);
        factura.setPuntoVenta(puntoVenta);
        factura.setEstado("BORRADOR");
        factura.setObservaciones("Ejemplo academico de ORM: sin autorizacion fiscal");
        factura.registrarAlta(usuario);
        factura.agregarDetalle(crearDetalle(cuaderno, 2));
        factura.agregarDetalle(crearDetalle(lapicera, 3));
        factura.recalcularTotal();
        return factura;
    }

    private static ListaPrecioArticulo crearPrecio(String codigo, String descripcion, double precio,
            ListaPrecio lista, Marca marca, Rubro rubro, Usuario usuario) {
        Articulo articulo = new Articulo();
        articulo.setCodigo(codigo);
        articulo.setDenominacion(descripcion);
        articulo.setMarca(marca);
        articulo.setRubro(rubro);
        articulo.registrarAlta(usuario);

        ListaPrecioArticulo precioArticulo = new ListaPrecioArticulo();
        precioArticulo.setListaPrecio(lista);
        precioArticulo.setArticulo(articulo);
        precioArticulo.setPrecioVenta(precio);
        precioArticulo.registrarAlta(usuario);
        return precioArticulo;
    }

    private static FacturaVentaDetalle crearDetalle(ListaPrecioArticulo precio, double cantidad) {
        FacturaVentaDetalle detalle = new FacturaVentaDetalle();
        detalle.setListaPrecioArticulo(precio);
        detalle.setDescripcion(precio.getArticulo().getDenominacion());
        detalle.setCantidad(cantidad);
        detalle.setPrecioUnitario(precio.getPrecioVenta());
        detalle.setPorcentajeBonificacion(0);
        // Importes ilustrativos: este TP no define reglas de liquidacion de IVA.
        detalle.setImporteNeto(cantidad * precio.getPrecioVenta());
        detalle.setImporteIva(0);
        detalle.setImporteSubtotal(detalle.getImporteNeto());
        return detalle;
    }
}
