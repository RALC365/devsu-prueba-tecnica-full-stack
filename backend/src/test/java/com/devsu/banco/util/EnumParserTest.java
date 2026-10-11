package com.devsu.banco.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devsu.banco.entity.Genero;
import com.devsu.banco.entity.TipoCuenta;
import com.devsu.banco.entity.TipoMovimiento;
import com.devsu.banco.exception.ValorInvalidoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class EnumParserTest {

    @ParameterizedTest
    @CsvSource({
            "CREDITO, CREDITO",
            "credito, CREDITO",
            "Crédito, CREDITO",
            "Depósito, CREDITO",
            "'  retiro ', DEBITO",
            "Débito, DEBITO"
    })
    void convierteTipoDeMovimientoIgnorandoMayusculasAcentosYAlias(String texto, TipoMovimiento esperado) {
        assertThat(TipoMovimiento.desde(texto)).isEqualTo(esperado);
    }

    @ParameterizedTest
    @CsvSource({"Ahorro, AHORROS", "Ahorros, AHORROS", "corriente, CORRIENTE"})
    void convierteTipoDeCuentaConLosNombresDelEnunciado(String texto, TipoCuenta esperado) {
        assertThat(TipoCuenta.desde(texto)).isEqualTo(esperado);
    }

    @Test
    void aceptaAbreviaturasDeGenero() {
        assertThat(Genero.desde("f")).isEqualTo(Genero.FEMENINO);
        assertThat(Genero.desde("Masculino")).isEqualTo(Genero.MASCULINO);
    }

    @Test
    void rechazaValoresDesconocidosIndicandoLosPermitidos() {
        assertThatThrownBy(() -> TipoCuenta.desde("PLAZO FIJO"))
                .isInstanceOf(ValorInvalidoException.class)
                .hasMessage("'PLAZO FIJO' no es un valor válido. Valores permitidos: AHORROS, CORRIENTE");
    }

    @Test
    void rechazaValoresNulos() {
        assertThatThrownBy(() -> TipoMovimiento.desde(null)).isInstanceOf(ValorInvalidoException.class);
    }
}
