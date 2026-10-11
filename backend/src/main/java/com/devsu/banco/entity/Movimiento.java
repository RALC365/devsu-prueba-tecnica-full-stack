package com.devsu.banco.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "movimiento")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movimiento_id")
    private Long id;

    @NotNull
    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false, length = 10)
    private TipoMovimiento tipoMovimiento;

    // crédito positivo, débito negativo
    @NotNull
    @Column(name = "valor", nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    // saldo de la cuenta después del movimiento
    @NotNull
    @Setter
    @Column(name = "saldo", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldo;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cuenta_id", nullable = false)
    private Cuenta cuenta;

    public static Movimiento registrar(Cuenta cuenta, TipoMovimiento tipo, BigDecimal valor,
                                       BigDecimal saldo, LocalDateTime fecha) {
        Movimiento movimiento = new Movimiento();
        movimiento.cuenta = cuenta;
        movimiento.tipoMovimiento = tipo;
        movimiento.valor = valor;
        movimiento.saldo = saldo;
        movimiento.fecha = fecha;
        return movimiento;
    }

    public void corregir(TipoMovimiento tipo, BigDecimal valor) {
        this.tipoMovimiento = tipo;
        this.valor = valor;
    }

    public BigDecimal getSaldoAnterior() {
        return saldo.subtract(valor);
    }
}
