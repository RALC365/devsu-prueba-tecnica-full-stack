package com.devsu.banco.repository;

import com.devsu.banco.entity.Cuenta;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

    @EntityGraph(attributePaths = "cliente")
    List<Cuenta> findAllByOrderByNumeroCuentaAsc();

    @EntityGraph(attributePaths = "cliente")
    List<Cuenta> findByClienteIdOrderByNumeroCuentaAsc(Long clienteId);

    @EntityGraph(attributePaths = "cliente")
    Optional<Cuenta> findWithClienteById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cuenta c where c.id = :id")
    Optional<Cuenta> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cuenta c where c.numeroCuenta = :numeroCuenta")
    Optional<Cuenta> findByNumeroCuentaForUpdate(@Param("numeroCuenta") String numeroCuenta);

    boolean existsByNumeroCuenta(String numeroCuenta);

    boolean existsByNumeroCuentaAndIdNot(String numeroCuenta, Long id);

    boolean existsByClienteId(Long clienteId);
}
