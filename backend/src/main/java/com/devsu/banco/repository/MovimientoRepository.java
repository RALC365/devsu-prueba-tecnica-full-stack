package com.devsu.banco.repository;

import com.devsu.banco.entity.Movimiento;
import com.devsu.banco.entity.TipoMovimiento;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    @EntityGraph(attributePaths = {"cuenta", "cuenta.cliente"})
    List<Movimiento> findAllByOrderByFechaDescIdDesc();

    @EntityGraph(attributePaths = {"cuenta", "cuenta.cliente"})
    List<Movimiento> findByCuentaNumeroCuentaOrderByFechaDescIdDesc(String numeroCuenta);

    @EntityGraph(attributePaths = {"cuenta", "cuenta.cliente"})
    Optional<Movimiento> findWithCuentaById(Long id);

    @Query("select m.cuenta.id from Movimiento m where m.id = :id")
    Optional<Long> findCuentaIdById(@Param("id") Long id);

    // por id y no por fecha: es el orden en que realmente se registraron
    List<Movimiento> findByCuentaIdOrderByIdAsc(Long cuentaId);

    boolean existsByCuentaId(Long cuentaId);

    @Query("""
            select coalesce(sum(m.valor), 0) from Movimiento m
            where m.cuenta.id = :cuentaId
              and m.tipoMovimiento = :tipo
              and m.fecha >= :desde and m.fecha < :hasta
              and (:excluidoId is null or m.id <> :excluidoId)
            """)
    BigDecimal sumarValores(@Param("cuentaId") Long cuentaId,
                            @Param("tipo") TipoMovimiento tipo,
                            @Param("desde") LocalDateTime desde,
                            @Param("hasta") LocalDateTime hasta,
                            @Param("excluidoId") Long excluidoId);

    @Query("""
            select m from Movimiento m
            where m.cuenta.cliente.id = :clienteId
              and m.id = (select max(u.id) from Movimiento u
                          where u.cuenta.id = m.cuenta.id and u.fecha < :hasta)
            """)
    List<Movimiento> findUltimosDelClienteAntesDe(@Param("clienteId") Long clienteId,
                                                  @Param("hasta") LocalDateTime hasta);

    @EntityGraph(attributePaths = {"cuenta", "cuenta.cliente"})
    @Query("""
            select m from Movimiento m
            where m.cuenta.cliente.id = :clienteId
              and m.fecha >= :desde and m.fecha < :hasta
            order by m.fecha asc, m.id asc
            """)
    List<Movimiento> findDelClienteEntre(@Param("clienteId") Long clienteId,
                                         @Param("desde") LocalDateTime desde,
                                         @Param("hasta") LocalDateTime hasta);
}
