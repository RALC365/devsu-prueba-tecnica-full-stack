package com.devsu.banco.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cliente")
@PrimaryKeyJoinColumn(name = "cliente_id", referencedColumnName = "persona_id")
@Getter
@Setter
@NoArgsConstructor
public class Cliente extends Persona {

    @NotBlank
    @Column(name = "contrasena", nullable = false, length = 100)
    private String contrasena;

    @NotNull
    @Column(name = "estado", nullable = false)
    private Boolean estado;

    public Long getClienteId() {
        return getId();
    }

    public boolean isActivo() {
        return Boolean.TRUE.equals(estado);
    }
}
