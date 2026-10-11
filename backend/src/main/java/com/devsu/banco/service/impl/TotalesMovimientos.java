package com.devsu.banco.service.impl;

import com.devsu.banco.entity.Movimiento;
import com.devsu.banco.entity.TipoMovimiento;
import com.devsu.banco.util.Montos;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.stream.Collector;
import java.util.stream.Collectors;

record TotalesMovimientos(BigDecimal creditos, BigDecimal debitos) {

    static TotalesMovimientos de(Collection<Movimiento> movimientos) {
        return movimientos.stream().collect(Collectors.teeing(
                sumaDe(TipoMovimiento.CREDITO),
                sumaDe(TipoMovimiento.DEBITO),
                TotalesMovimientos::new));
    }

    private static Collector<Movimiento, ?, BigDecimal> sumaDe(TipoMovimiento tipo) {
        return Collectors.filtering(
                movimiento -> movimiento.getTipoMovimiento() == tipo,
                Collectors.collectingAndThen(
                        Collectors.reducing(BigDecimal.ZERO, Movimiento::getValor, BigDecimal::add),
                        Montos::normalizar));
    }
}
