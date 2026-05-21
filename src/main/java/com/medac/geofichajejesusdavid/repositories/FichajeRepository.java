package com.medac.geofichajejesusdavid.repositories;

import com.medac.geofichajejesusdavid.models.Fichaje;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FichajeRepository extends JpaRepository<Fichaje, Long> {

    // Spring crea esta consulta automaticamente a partir del nombre del metodo.
    List<Fichaje> findByEmpleadoId(Long empleadoId);

    List<Fichaje> findByEmpleadoIdOrderByFechaHoraDesc(Long empleadoId);
}
