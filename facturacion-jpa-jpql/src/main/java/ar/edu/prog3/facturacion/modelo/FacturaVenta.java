package ar.edu.prog3.facturacion.modelo;

import javax.persistence.*;
import java.time.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Entity
@Table(name = "facturas_venta")
public class FacturaVenta extends AuditoriaApp {

    @Column(name = "numero")
    private Long numero;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @ManyToOne(fetch = FetchType.LAZY, optional = true, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "cliente_id", nullable = true)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "condicion_iva_id", nullable = false)
    private CondicionIva condicionIva;

    @ManyToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "tipo_moneda_id", nullable = false)
    private TipoMoneda tipoMoneda;

    @ManyToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "punto_venta_id", nullable = false)
    private PuntoVenta puntoVenta;

    @Column(name = "importe_cobrado")
    private double importeCobrado;

    @Column(name = "importe_saldo")
    private double importeSaldo;

    @Column(name = "importe_total", nullable = false)
    private double importeTotal;

    @Column(name = "cae")
    private String cae;

    @Column(name = "cae_fecha_vencimiento")
    private LocalDate caeFechaVencimiento;

    @Column(name = "resultado_afip")
    private String resultadoAfip;

    @Column(name = "motivo_rechazo")
    private String motivoRechazo;

    @Column(name = "estado", nullable = false)
    private String estado;

    @Column(name = "fecha_anulacion")
    private LocalDateTime fechaAnulacion;

    @Column(name = "observaciones")
    private String observaciones;

    public FacturaVenta() {
        // Constructor sin argumentos requerido por JPA.
    }

    public Long getNumero() {
        return numero;
    }

    public void setNumero(Long numero) {
        this.numero = numero;
    }

    public LocalDate getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDate fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public CondicionIva getCondicionIva() {
        return condicionIva;
    }

    public void setCondicionIva(CondicionIva condicionIva) {
        this.condicionIva = condicionIva;
    }

    public TipoMoneda getTipoMoneda() {
        return tipoMoneda;
    }

    public void setTipoMoneda(TipoMoneda tipoMoneda) {
        this.tipoMoneda = tipoMoneda;
    }

    public PuntoVenta getPuntoVenta() {
        return puntoVenta;
    }

    public void setPuntoVenta(PuntoVenta puntoVenta) {
        this.puntoVenta = puntoVenta;
    }

    public double getImporteCobrado() {
        return importeCobrado;
    }

    public void setImporteCobrado(double importeCobrado) {
        this.importeCobrado = importeCobrado;
    }

    public double getImporteSaldo() {
        return importeSaldo;
    }

    public void setImporteSaldo(double importeSaldo) {
        this.importeSaldo = importeSaldo;
    }

    public double getImporteTotal() {
        return importeTotal;
    }

    public void setImporteTotal(double importeTotal) {
        this.importeTotal = importeTotal;
    }

    public String getCae() {
        return cae;
    }

    public void setCae(String cae) {
        this.cae = cae;
    }

    public LocalDate getCaeFechaVencimiento() {
        return caeFechaVencimiento;
    }

    public void setCaeFechaVencimiento(LocalDate caeFechaVencimiento) {
        this.caeFechaVencimiento = caeFechaVencimiento;
    }

    public String getResultadoAfip() {
        return resultadoAfip;
    }

    public void setResultadoAfip(String resultadoAfip) {
        this.resultadoAfip = resultadoAfip;
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    public void setMotivoRechazo(String motivoRechazo) {
        this.motivoRechazo = motivoRechazo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaAnulacion() {
        return fechaAnulacion;
    }

    public void setFechaAnulacion(LocalDateTime fechaAnulacion) {
        this.fechaAnulacion = fechaAnulacion;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    // mappedBy nombra el atributo Java del lado que guarda la clave foranea.
    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FacturaVentaDetalle> detalles = new ArrayList<>();

    public List<FacturaVentaDetalle> getDetalles() {
        return Collections.unmodifiableList(detalles);
    }

    /** Mantiene sincronizados los dos extremos de la relacion. */
    public void agregarDetalle(FacturaVentaDetalle detalle) {
        Objects.requireNonNull(detalle, "El detalle es obligatorio");
        if (detalle.getFactura() != null && detalle.getFactura() != this) {
            throw new IllegalArgumentException("El detalle ya pertenece a otra factura");
        }
        if (!detalles.contains(detalle)) {
            detalles.add(detalle);
        }
        detalle.setFactura(this);
    }

    public void quitarDetalle(FacturaVentaDetalle detalle) {
        if (detalles.remove(detalle)) {
            detalle.setFactura(null);
        }
    }

    /** Suma subtotales ya definidos; no calcula impuestos ni autoriza comprobantes. */
    public void recalcularTotal() {
        BigDecimal total = detalles.stream()
                .map(detalle -> BigDecimal.valueOf(detalle.getImporteSubtotal()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        importeTotal = total.doubleValue();
        importeSaldo = total.subtract(BigDecimal.valueOf(importeCobrado))
                .setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
