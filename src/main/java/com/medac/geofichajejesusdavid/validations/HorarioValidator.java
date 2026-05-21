package com.medac.geofichajejesusdavid.validations;

import java.time.LocalDateTime;

@FunctionalInterface
public interface HorarioValidator {
    boolean validar(LocalDateTime horaFichaje);
}
