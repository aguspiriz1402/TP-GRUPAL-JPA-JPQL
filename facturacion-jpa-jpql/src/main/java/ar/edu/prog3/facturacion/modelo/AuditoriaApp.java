package ar.edu.prog3.facturacion.modelo;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/** Las columnas de auditoria se incluyen en cada tabla que hereda de esta clase. */
@MappedSuperclass
public abstract class AuditoriaApp extends EntityId {
    @Column(name = "fecha_alta", nullable = false)
    protected LocalDateTime fechaAlta;

    @Column(name = "fecha_baja")
    protected LocalDateTime fechaBaja;

    @Column(name = "fecha_modificacion", nullable = false)
    protected LocalDateTime fechaModificacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "usuario_carga_id", nullable = false)
    protected Usuario usuarioCarga;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "usuario_baja_id")
    protected Usuario usuarioBaja;

    @ManyToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "usuario_modificacion_id", nullable = false)
    protected Usuario usuarioModificacion;

    public void registrarAlta(Usuario usuario) {
        Objects.requireNonNull(usuario, "Se requiere un usuario de auditoria");
        fechaAlta = LocalDateTime.now();
        fechaModificacion = fechaAlta;
        usuarioCarga = usuario;
        usuarioModificacion = usuario;
    }

    public void registrarModificacion(Usuario usuario) {
        usuarioModificacion = Objects.requireNonNull(usuario, "Se requiere un usuario de auditoria");
        fechaModificacion = LocalDateTime.now();
    }

    public void registrarBaja(Usuario usuario) {
        usuarioBaja = Objects.requireNonNull(usuario, "Se requiere un usuario de auditoria");
        fechaBaja = LocalDateTime.now();
        registrarModificacion(usuario);
    }

    @PrePersist
    protected void completarFechas() {
        if (fechaAlta == null) {
            fechaAlta = LocalDateTime.now();
        }
        if (fechaModificacion == null) {
            fechaModificacion = fechaAlta;
        }
    }

    @PreUpdate
    protected void actualizarFechaModificacion() {
        fechaModificacion = LocalDateTime.now();
    }

    public LocalDateTime getFechaAlta() { return fechaAlta; }
    public LocalDateTime getFechaBaja() { return fechaBaja; }
    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public Usuario getUsuarioCarga() { return usuarioCarga; }
    public Usuario getUsuarioBaja() { return usuarioBaja; }
    public Usuario getUsuarioModificacion() { return usuarioModificacion; }
}
