package com.medac.geofichajejesusdavid.services;

import com.medac.geofichajejesusdavid.models.Empleado;
import com.medac.geofichajejesusdavid.enums.EstadoFichaje;
import com.medac.geofichajejesusdavid.models.Fichaje;
import com.medac.geofichajejesusdavid.repositories.FichajeRepository;
import com.medac.geofichajejesusdavid.validations.DistanciaValidator;
import com.medac.geofichajejesusdavid.validations.HorarioValidator;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FichajeService implements GenericService<Fichaje, Long> {

    private static final double LATITUD_CENTRO_TRABAJO = 37.8802566;
    private static final double LONGITUD_CENTRO_TRABAJO = -4.8040947;
    private static final double METROS_MAXIMOS_PERMITIDOS = 200.0;
    private static final int HORA_APERTURA = 6;
    private static final int HORA_CIERRE = 22;

    private final FichajeRepository fichajeRepository;

    public FichajeService(FichajeRepository fichajeRepository) {
        this.fichajeRepository = fichajeRepository;
    }

    @Override
    public List<Fichaje> findAll() {
        return fichajeRepository.findAll();
    }

    @Override
    public Optional<Fichaje> findById(Long id) {
        return fichajeRepository.findById(id);
    }

    @Override
    public Fichaje save(Fichaje entity) {
        return fichajeRepository.save(entity);
    }

    @Override
    public void deleteById(Long id) {
        fichajeRepository.deleteById(id);
    }

    public Fichaje registrarFichaje(Empleado trabajador, double latitudActual, double longitudActual,
            EstadoFichaje tipoMovimiento) {
        validarDistancia(latitudActual, longitudActual);
        validarHorario(LocalDateTime.now());
        validarOrdenDeFichaje(trabajador.getId(), tipoMovimiento);

        Fichaje.Coordenadas posicionGps = new Fichaje.Coordenadas(latitudActual, longitudActual);
        Fichaje registroNuevo = new Fichaje(trabajador, LocalDateTime.now(), tipoMovimiento, posicionGps);

        return fichajeRepository.save(registroNuevo);
    }

    public List<Fichaje> buscarHistorialEmpleado(Long empleadoId) {
        return fichajeRepository.findByEmpleadoIdOrderByFechaHoraDesc(empleadoId);
    }

    public long contarDiasTrabajados(Long empleadoId) {
        List<Fichaje> fichajesDelEmpleado = fichajeRepository.findByEmpleadoId(empleadoId);

        return fichajesDelEmpleado.stream()
            .filter(registro -> registro.getEstado() == EstadoFichaje.ENTRADA)
            .count();
    }

    public String calcularHorasTrabajadas(Long empleadoId) {
        List<Fichaje> historial = fichajeRepository.findByEmpleadoId(empleadoId);

        List<Fichaje> entradasOrdenadas = historial.stream()
                .filter(registro -> registro.getEstado() == EstadoFichaje.ENTRADA)
                .sorted((actual, siguiente) -> actual.getFechaHora().compareTo(siguiente.getFechaHora()))
                .toList();

        List<Fichaje> salidasOrdenadas = historial.stream()
                .filter(registro -> registro.getEstado() == EstadoFichaje.SALIDA)
                .sorted((actual, siguiente) -> actual.getFechaHora().compareTo(siguiente.getFechaHora()))
                .toList();

        java.time.Duration tiempoTotal = java.time.Duration.ZERO;
        int parejasCompletas = Math.min(entradasOrdenadas.size(), salidasOrdenadas.size());

        for (int posicion = 0; posicion < parejasCompletas; posicion++) {
            LocalDateTime inicioTurno = entradasOrdenadas.get(posicion).getFechaHora();
            LocalDateTime finTurno = salidasOrdenadas.get(posicion).getFechaHora();
            tiempoTotal = tiempoTotal.plus(java.time.Duration.between(inicioTurno, finTurno));
        }

        long horas = tiempoTotal.toHours();
        int minutos = tiempoTotal.toMinutesPart();
        int segundos = tiempoTotal.toSecondsPart();

        return String.format("%02d:%02d:%02d", horas, minutos, segundos);
    }

    private void validarDistancia(double latitudActual, double longitudActual) {
        DistanciaValidator comprobarCercania = (latitudCentro, longitudCentro, latitudPersona, longitudPersona) -> {
            double latitudPersonaRad = Math.toRadians(latitudPersona);
            double latitudCentroRad = Math.toRadians(latitudCentro);
            double longitudPersonaRad = Math.toRadians(longitudPersona);
            double longitudCentroRad = Math.toRadians(longitudCentro);
            double radioTierraMetros = 6371000.0;

            double parteCentral = Math.pow(Math.sin((latitudPersonaRad - latitudCentroRad) / 2), 2)
                    + Math.cos(latitudCentroRad) * Math.cos(latitudPersonaRad)
                    * Math.pow(Math.sin((longitudPersonaRad - longitudCentroRad) / 2), 2);

            double distanciaEnMetros = 2 * radioTierraMetros * Math.asin(Math.sqrt(parteCentral));
            return distanciaEnMetros <= METROS_MAXIMOS_PERMITIDOS;
        };

        boolean dentroDelRadio = comprobarCercania.validar(
                LATITUD_CENTRO_TRABAJO,
                LONGITUD_CENTRO_TRABAJO,
                latitudActual,
                longitudActual);

        if (!dentroDelRadio) {
            throw new RuntimeException("Error: estas a mas de 200 metros del centro de trabajo.");
        }
    }

    private void validarHorario(LocalDateTime momentoActual) {
        HorarioValidator comprobarHorario = horaRegistro -> {
            int hora = horaRegistro.getHour();
            return hora >= HORA_APERTURA && hora < HORA_CIERRE;
        };

        if (!comprobarHorario.validar(momentoActual)) {
            throw new RuntimeException("Error: solo puedes fichar entre las 6:00 y las 22:00.");
        }
    }

    private void validarOrdenDeFichaje(Long empleadoId, EstadoFichaje movimientoSolicitado) {
        Optional<Fichaje> ultimoRegistro = fichajeRepository.findByEmpleadoId(empleadoId).stream()
                .max((actual, siguiente) -> actual.getFechaHora().compareTo(siguiente.getFechaHora()));

        if (movimientoSolicitado == EstadoFichaje.ENTRADA
                && ultimoRegistro.isPresent()
                && ultimoRegistro.get().getEstado() == EstadoFichaje.ENTRADA) {
            throw new RuntimeException("Error: ya tienes una entrada abierta. Primero debes registrar una salida.");
        }

        if (movimientoSolicitado == EstadoFichaje.SALIDA && ultimoRegistro.isEmpty()) {
            throw new RuntimeException("Error: no puedes salir porque todavia no has registrado una entrada.");
        }

        if (movimientoSolicitado == EstadoFichaje.SALIDA
                && ultimoRegistro.isPresent()
                && ultimoRegistro.get().getEstado() == EstadoFichaje.SALIDA) {
            throw new RuntimeException("Error: la ultima accion ya fue una salida. Primero registra una entrada.");
        }
    }
}
