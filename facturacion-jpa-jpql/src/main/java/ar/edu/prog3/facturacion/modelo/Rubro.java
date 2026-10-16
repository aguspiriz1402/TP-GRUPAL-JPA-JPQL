package ar.edu.prog3.facturacion.modelo;

import javax.persistence.*;

@Entity
@Table(name = "rubros")
public class Rubro extends AuditoriaApp {

    @Column(name = "denominacion", nullable = false)
    private String denominacion;

    @Column(name = "codigo", nullable = false)
    private Integer codigo;

    public Rubro() {
        // Constructor sin argumentos requerido por JPA.
    }

    public String getDenominacion() {
        return denominacion;
    }

    public void setDenominacion(String denominacion) {
        this.denominacion = denominacion;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }
}
