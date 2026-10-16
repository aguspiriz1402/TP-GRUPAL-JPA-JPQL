package ar.edu.prog3.facturacion.modelo;

import javax.persistence.*;

@Entity
@Table(name = "tipos_moneda")
public class TipoMoneda extends AuditoriaApp {

    @Column(name = "codigo_afip", nullable = false)
    private String codigoAfip;

    @Column(name = "denominacion", nullable = false)
    private String denominacion;

    @Column(name = "simbolo", nullable = false)
    private String simbolo;

    public TipoMoneda() {
        // Constructor sin argumentos requerido por JPA.
    }

    public String getCodigoAfip() {
        return codigoAfip;
    }

    public void setCodigoAfip(String codigoAfip) {
        this.codigoAfip = codigoAfip;
    }

    public String getDenominacion() {
        return denominacion;
    }

    public void setDenominacion(String denominacion) {
        this.denominacion = denominacion;
    }

    public String getSimbolo() {
        return simbolo;
    }

    public void setSimbolo(String simbolo) {
        this.simbolo = simbolo;
    }
}
