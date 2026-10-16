package ar.edu.prog3.facturacion.modelo;

import javax.persistence.*;

@Entity
@Table(name = "domicilios")
public class Domicilio extends EntityId {

    @Column(name = "nombre_calle")
    private String nombreCalle;

    @Column(name = "numero_calle")
    private String numeroCalle;

    public Domicilio() {
        // Constructor sin argumentos requerido por JPA.
    }

    public String getNombreCalle() {
        return nombreCalle;
    }

    public void setNombreCalle(String nombreCalle) {
        this.nombreCalle = nombreCalle;
    }

    public String getNumeroCalle() {
        return numeroCalle;
    }

    public void setNumeroCalle(String numeroCalle) {
        this.numeroCalle = numeroCalle;
    }
}
