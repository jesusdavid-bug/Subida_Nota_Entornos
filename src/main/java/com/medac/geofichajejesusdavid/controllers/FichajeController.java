package com.medac.geofichajejesusdavid.controllers;

import com.medac.geofichajejesusdavid.models.Empleado;
import com.medac.geofichajejesusdavid.enums.EstadoFichaje;
import com.medac.geofichajejesusdavid.models.Fichaje;
import com.medac.geofichajejesusdavid.services.EmpleadoService;
import com.medac.geofichajejesusdavid.services.FichajeService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
public class FichajeController {

    private final FichajeService fichajeService;
    private final EmpleadoService empleadoService;
    private final String googleMapsApiKey;

    public FichajeController(FichajeService fichajeService,
            EmpleadoService empleadoService,
            @Value("${app.google-maps-api-key:}") String googleMapsApiKey) {
        this.fichajeService = fichajeService;
        this.empleadoService = empleadoService;
        this.googleMapsApiKey = googleMapsApiKey;
    }

    @GetMapping("/")
    public String mostrarLogin() {
        return "index";
    }

    @PostMapping("/login")
    public String comprobarAcceso(@RequestParam String dni, Model model) {
        Optional<Empleado> trabajadorEncontrado = empleadoService.findByDni(dni);

        if (trabajadorEncontrado.isEmpty()) {
            model.addAttribute("mensajeError", "El DNI introducido no existe en la base de datos.");
            return "index";
        }

        prepararPanel(trabajadorEncontrado.get(), model);
        return "panel";
    }

    @PostMapping("/fichar")
    public String guardarFichaje(@RequestParam String dni,
            @RequestParam double latitud,
            @RequestParam double longitud,
            @RequestParam EstadoFichaje estado,
            Model model) {
        Optional<Empleado> trabajadorEncontrado = empleadoService.findByDni(dni);

        if (trabajadorEncontrado.isEmpty()) {
            model.addAttribute("mensajeError", "No se encontro el empleado que intenta fichar.");
            return "index";
        }

        Empleado trabajador = trabajadorEncontrado.get();

        try {
            fichajeService.registrarFichaje(trabajador, latitud, longitud, estado);
            model.addAttribute("mensajeExito", "Fichaje de " + estado + " guardado correctamente.");
        } catch (RuntimeException errorValidacion) {
            model.addAttribute("mensajeError", errorValidacion.getMessage());
        }

        prepararPanel(trabajador, model);
        return "panel";
    }

    private void prepararPanel(Empleado trabajador, Model model) {
        List<Fichaje> historialPersonal = fichajeService.buscarHistorialEmpleado(trabajador.getId());
        long diasTrabajados = fichajeService.contarDiasTrabajados(trabajador.getId());
        String horasTrabajadas = fichajeService.calcularHorasTrabajadas(trabajador.getId());

        model.addAttribute("empleado", trabajador);
        model.addAttribute("fichajes", historialPersonal);
        model.addAttribute("diasTrabajados", diasTrabajados);
        model.addAttribute("horasTrabajadas", horasTrabajadas);
        model.addAttribute("googleMapsApiKey", googleMapsApiKey);
    }
}
