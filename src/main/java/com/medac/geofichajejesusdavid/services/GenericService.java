package com.medac.geofichajejesusdavid.services;

import java.util.List;
import java.util.Optional;

// Interfaz que define los metodos comunes para nuestros servicios.
public interface GenericService<T, ID> {

    List<T> findAll();

    Optional<T> findById(ID id);

    T save(T entity);

    void deleteById(ID id);
}
