package com.medac.geofichajejesusdavid.repositories;

import com.medac.geofichajejesusdavid.models.Empleado;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {

    // Queremos buscar al empleado por su dni
    Optional<Empleado> findByDni(String dni);
}
