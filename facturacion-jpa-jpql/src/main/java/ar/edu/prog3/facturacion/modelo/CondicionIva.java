package ar.edu.prog3.facturacion.modelo;

import javax.persistence.*;

@Entity
@Table(name = "condiciones_iva")
public class CondicionIva extends AuditoriaApp {

    @Column(name = "codigo_afip", nullable = false)
    private int codigoAfip;

    @Column(name = "denominacion", nullable = false)
    private String denominacion;

    public CondicionIva() {
        // Constructor sin argumentos requerido por JPA.
    }

    public int getCodigoAfip() {
        return codigoAfip;
    }

    public void setCodigoAfip(int codigoAfip) {
        this.codigoAfip = codigoAfip;
    }

    public String getDenominacion() {
        return denominacion;
    }

    public void setDenominacion(String denominacion) {
        this.denominacion = denominacion;
    }
}
