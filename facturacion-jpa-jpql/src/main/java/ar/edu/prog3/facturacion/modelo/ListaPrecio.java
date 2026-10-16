package ar.edu.prog3.facturacion.modelo;

import javax.persistence.*;

@Entity
@Table(name = "listas_precio")
public class ListaPrecio extends AuditoriaApp {

    @Column(name = "codigo", nullable = false)
    private String codigo;

    @Column(name = "denominacion", nullable = false)
    private String denominacion;

    public ListaPrecio() {
        // Constructor sin argumentos requerido por JPA.
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDenominacion() {
        return denominacion;
    }

    public void setDenominacion(String denominacion) {
        this.denominacion = denominacion;
    }
}
