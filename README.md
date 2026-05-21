# GeoFichaje Jesus David

Aplicacion web hecha con **Spring Boot** para registrar la entrada y salida de empleados usando su ubicacion GPS. El proyecto valida que el fichaje se realiza cerca del centro de trabajo, dentro del horario permitido y siguiendo el orden correcto de jornada.

## Que hace

GeoFichaje permite que un empleado:

- Acceda al panel escribiendo su DNI.
- Autorice la ubicacion del navegador.
- Registre una **ENTRADA** o una **SALIDA**.
- Consulte su historial de fichajes.
- Vea un resumen con entradas registradas y tiempo trabajado.

El sistema guarda los datos en MySQL mediante JPA y muestra la ubicacion con Google Maps.

## Tecnologias

- Java 25
- Spring Boot 4.0.6
- Spring MVC
- Spring Data JPA
- Thymeleaf
- MySQL
- Google Maps JavaScript API
- Maven Wrapper

## Estructura del proyecto

```text
src/main/java/com/medac/geofichajejesusdavid
+-- controllers      # Controlador web MVC
+-- enums            # EstadoFichaje: ENTRADA / SALIDA
+-- models           # Entidades JPA: Empleado y Fichaje
+-- repositories     # Repositorios Spring Data JPA
+-- services         # Logica de negocio
+-- validations      # Interfaces funcionales de validacion

src/main/resources
+-- templates        # Vistas Thymeleaf
+-- static/css       # Estilos de la interfaz
+-- application.properties
```

## Como funciona

1. El empleado entra en `/` y escribe su DNI.
2. `FichajeController` busca el empleado mediante `EmpleadoService`.
3. Si el DNI existe, se carga `panel.html` con el historial y las metricas.
4. El navegador solicita permiso de geolocalizacion.
5. Al pulsar **Entrada** o **Salida**, se envia el DNI, la latitud, la longitud y el estado.
6. `FichajeService` valida:
   - Que el empleado este como maximo a 200 metros del centro de trabajo.
   - Que el fichaje se haga entre las 6:00 y las 22:00.
   - Que no haya dos entradas seguidas ni una salida sin entrada previa.
7. Si todo es correcto, `FichajeRepository` guarda el registro en MySQL.

## Modelo de datos

### Empleado

Representa a la persona que puede fichar.

- `id`
- `nombre`
- `dni`
- `email`

### Fichaje

Representa cada registro horario.

- `id`
- `empleado`
- `fechaHora`
- `estado`
- `ubicacion`

La relacion principal es:

```text
Empleado 1 --- * Fichaje
```

## Reglas de negocio

| Regla | Descripcion |
|---|---|
| Distancia | El empleado debe estar a 200 metros o menos del centro de trabajo. |
| Horario | Solo se puede fichar entre las 6:00 y las 22:00. |
| Orden | Una entrada abierta debe cerrarse con una salida antes de registrar otra entrada. |
| Existencia | Solo pueden acceder empleados cuyo DNI exista en la base de datos. |

## Configuracion local

El proyecto usa MySQL. Antes de arrancar la aplicacion, crea una base de datos:

```sql
CREATE DATABASE geofichaje_jesusdavid;
```

La configuracion por defecto espera:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/geofichaje_jesusdavid
spring.datasource.username=root
spring.datasource.password=
```

Puedes cambiar estos valores en `src/main/resources/application.properties`.

## Google Maps API Key

Por seguridad, la clave de Google Maps no se sube al repositorio. Define esta variable de entorno antes de ejecutar el proyecto:

```powershell
$env:GOOGLE_MAPS_API_KEY="tu_clave_aqui"
```

En Linux/macOS:

```bash
export GOOGLE_MAPS_API_KEY="tu_clave_aqui"
```

Si no defines la clave, la aplicacion sigue funcionando, pero el mapa puede no cargarse correctamente.

## Como ejecutar

Desde la carpeta del proyecto:

```powershell
.\mvnw.cmd spring-boot:run
```

En Linux/macOS:

```bash
./mvnw spring-boot:run
```

Despues abre:

```text
http://localhost:8080
```

## Datos de prueba

La aplicacion valida el DNI contra MySQL. Puedes insertar un empleado manualmente:

```sql
INSERT INTO empleados (nombre, dni, email)
VALUES ('Jesus David', '12345678A', 'jesusdavid@example.com');
```

Luego entra con el DNI:

```text
12345678A
```

## Presentacion

La carpeta `presentacion/` incluye material de apoyo para explicar el proyecto en clase:

- `geofichaje_jesusdavid_presentacion.pptx`
- `geofichaje_jesusdavid_presentacion.pdf`

## Control de versiones

El proyecto esta preparado para subirse a GitHub. Recomendaciones:

- No subir claves privadas ni credenciales.
- Mantener commits pequenos y descriptivos.
- Documentar cambios importantes en este README.
- Revisar `git status` antes de hacer `push`.

## Autor

Proyecto desarrollado por **Jesus David** para la actividad de subida de nota de Entornos de Desarrollo.
