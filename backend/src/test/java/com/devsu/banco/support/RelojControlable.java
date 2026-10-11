package com.devsu.banco.support;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class RelojControlable extends Clock {

    private final ZoneId zona;
    private volatile Instant instante;

    public RelojControlable(LocalDateTime fechaHora, ZoneId zona) {
        this.zona = zona;
        fijar(fechaHora);
    }

    public final void fijar(LocalDateTime fechaHora) {
        this.instante = fechaHora.atZone(zona).toInstant();
    }

    @Override
    public ZoneId getZone() {
        return zona;
    }

    @Override
    public Clock withZone(ZoneId otraZona) {
        return Clock.fixed(instante, otraZona);
    }

    @Override
    public Instant instant() {
        return instante;
    }
}
