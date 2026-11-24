# AP-U1 · AforoSeguro: Control de Aforo Distribuido

Sistema distribuido para el control y monitoreo de aforo en tiempo real mediante **Java RMI**, con detección de fallos por latidos (*heartbeats*) y sincronización de eventos mediante **Relojes Lógicos de Lamport**.

---

## Información del Estudiante y Entrega

- **Asignatura:** Aplicaciones Distribuidas (ISR-701) · Séptimo Nivel A
- **Estudiante:** Bryan Humberto Lombeida Escaleras
- **Correo institucional / contacto:** `brylombeida@gmail.com`
- **Código de sesión:** `KD55-TB`
- **Actividad:** AP-U1 (Aporte 1)
- **Repositorio oficial:** [https://github.com/BryanLomb/AP-U1-Lombeida-Bryan.git](https://github.com/BryanLomb/AP-U1-Lombeida-Bryan.git)

---

## Estructura del Repositorio

```text
AP-U1-Lombeida-Bryan/
├── config.properties          # Archivo de configuración central (host, puertos, aforo, token)
├── src/                       # Código fuente en Java
│   ├── Configuracion.java     # Carga y validación estricta de propiedades
│   ├── ICoordinadorRemoto.java# Interfaz RMI del servicio remoto
│   ├── Solicitud.java         # DTO serializable para peticiones cliente -> servidor
│   ├── Respuesta.java         # DTO serializable para respuestas servidor -> cliente
│   ├── CoordinadorServicio.java # Implementación del servicio RMI, aforo, latidos y Lamport
│   ├── CoordinadorMain.java   # Proceso servidor del coordinador central
│   └── PuertaMain.java        # Proceso cliente de cada puerta (P1, P2, P3)
├── bin/                       # Clases compiladas (.class)
├── docs/                      # Documentación e informes en LaTeX
│   ├── informe.tex            # Documento principal del informe técnico
│   ├── informe.pdf            # PDF compilado del informe técnico
│   ├── entrega_SGA.tex        # Hoja de datos identificativos de entrega
│   ├── entrega_SGA.pdf        # PDF compilado de entrega
│   └── img/                   # Evidencias y capturas (ej. E0.png)
├── logs/                      # Bitácoras de eventos con relojes de Lamport (*.log)
└── README.md                  # Este documento
```

---

## 1. Compilación y Ejecución de cada Proceso

### Requisitos Previos

- **Java Development Kit (JDK):** Versión 17 o superior (compatible con Java 21).
- **Puertos de Red:** Los puertos TCP `5000` (registro RMI) y `5001` (puerto de exportación del servicio) deben encontrarse libres en `127.0.0.1`.

---

### Compilación

Desde el directorio del proyecto (`AP-U1-Lombeida-Bryan`), compile todos los archivos fuente Java direccionando los binarios al directorio `bin`:

```bash
javac -d bin src/*.java
```

---

### Configuración (`config.properties`)

Antes de iniciar los procesos, verifique que `config.properties` contenga los parámetros correctos:

```properties
coordinador.host = 127.0.0.1
puerto.registro = 5000
puerto.servicio = 5001
aforo.maximo = 10
token.acceso = Clave-Bryan
```

---

### Ejecución de los Procesos

El sistema está diseñado para ejecutarse en terminales independientes: una para el Coordinador Central y una para cada una de las tres puertas (P1 Norte, P2 Sur y P3 Este).

#### 1. Iniciar el Coordinador Central (Proceso 0)

Abra la primera terminal y ejecute el coordinador:

- **Modo Normal (por defecto, con exclusión mutua sincronizada):**
  ```bash
  java -cp bin CoordinadorMain
  ```
  *O especificando una ruta de configuración alternativa:*
  ```bash
  java -cp bin CoordinadorMain config.properties
  ```

- **Modo Inseguro (únicamente para el escenario E3 de condiciones de carrera):**
  ```bash
  java -cp bin CoordinadorMain --inseguro
  ```

**Acciones que realiza el Coordinador:**
- Crea o localiza el registro RMI en el puerto `5000`.
- Exporta y publica el objeto remoto en `rmi://127.0.0.1:5000/AforoSeguro`.
- Inicia el vigilante de latidos (*watchdog*) cada 1 s: si una puerta no emite latido en 6 s (`TIMEOUT_LATIDO_MS = 6000L`), la marca como `CAIDA`. Cuando vuelve a recibir un latido, la restablece a `ACTIVA`.
- Gestiona el contador global `dentro` protegiendo el acceso con `synchronized`.
- Registra cada evento en `logs/coordinador.log`.

---

#### 2. Iniciar las Puertas (Procesos 1, 2 y 3)

En terminales independientes, inicie cada una de las puertas pasando como primer argumento su identificador (`1`, `2` o `3`):

- **Terminal 2 — Puerta 1 (P1 Norte):**
  ```bash
  java -cp bin PuertaMain 1
  ```

- **Terminal 3 — Puerta 2 (P2 Sur):**
  ```bash
  java -cp bin PuertaMain 2
  ```

- **Terminal 4 — Puerta 3 (P3 Este):**
  ```bash
  java -cp bin PuertaMain 3
  ```

**Comandos interactivos disponibles en cada puerta:**

Una vez iniciada la consola interactiva (`P1>`, `P2>`, `P3>`), se pueden ingresar las siguientes órdenes:

| Comando | Acción |
| :--- | :--- |
| `entrar` | Envía una solicitud `ENTRAR` al coordinador para dar paso a una persona. Si hay cupo (`dentro < aforo`), se autoriza; si está lleno, se rechaza. |
| `salir` | Envía una solicitud `SALIR` al coordinador para registrar el egreso de una persona y decrementar el aforo ocupado. |
| `estado` | Consulta el estado global actual: ocupación (`dentro`), capacidad máxima (`aforo`) y estado de operatividad de todas las puertas. |
| `fin` | Detiene el planificador de latidos, registra el cierre y finaliza el proceso de la puerta de forma limpia. |

**Comportamiento autónomo de las puertas:**
- **Latido automático:** Cada puerta cuenta con un hilo demonio que envía periódicamente una solicitud `LATIDO` cada 2 segundos (`INTERVALO_LATIDO_MS = 2000L`) al coordinador.
- **Reloj de Lamport local:** Incrementa su valor antes de cada emisión y se ajusta a $\max(L_{\text{local}}, L_{\text{recibido}}) + 1$ tras recibir la respuesta.
- **Resiliencia ante caída del coordinador:** Si el coordinador no responde (`RemoteException`), la puerta no autoriza accesos localmente, priorizando la consistencia sobre la disponibilidad (sistema CP), y registra el estado `COORDINADOR_CAIDO`.
- **Bitácora local:** Cada puerta persiste sus eventos en `logs/P1.log`, `logs/P2.log` o `logs/P3.log`.

---

## 2. Formato de los Mensajes

La comunicación interproceso se realiza mediante **Java Remote Method Invocation (RMI)** a través de la interfaz:

```java
public interface ICoordinadorRemoto extends Remote {
    Respuesta procesar(Solicitud solicitud) throws RemoteException;
}
```

### Objeto `Solicitud` (`Serializable`)

Representa el mensaje transmitido desde la puerta hacia el coordinador:

| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `tipo` | `String` | Acción solicitada: `"ENTRAR"`, `"SALIR"`, `"ESTADO"` o `"LATIDO"`. |
| `token` | `String` | Token de autenticación del cliente (`config.token()`, ej. `"Clave-Bryan"`). |
| `idPuerta` | `String` | Identificador de la puerta emisora (`"1"`, `"2"` o `"3"`). |
| `lamport` | `long` | Marca de tiempo lógica del reloj de Lamport del emisor al momento del envío. |

---

### Objeto `Respuesta` (`Serializable`)

Representa el mensaje retornado desde el coordinador hacia la puerta:

| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `resultado` | `String` | Resultado de la operación: `"PERMITIDA"`, `"RECHAZADA"`, `"ESTADO"`, `"OK"` o `"ERROR"`. |
| `motivo` | `String` | Detalle o motivo en caso de rechazo (`""`, `"aforo completo"`, `"token inválido"`, `"puerta inválida"`, etc.). |
| `dentro` | `int` | Número actual de personas dentro del recinto tras evaluar la petición. |
| `aforo` | `int` | Capacidad máxima configurada (`aforo.maximo`). |
| `lamport` | `long` | Marca de tiempo lógica del coordinador tras procesar la solicitud. |
| `estadoPuertas` | `String` | Diagnóstico de conectividad de las puertas (ej. `P1: ACTIVA, P2: ACTIVA, P3: CAIDA`). |

---

### Formato de los Registros de Bitácora (`logs/*.log`)

Tanto el coordinador como las puertas generan archivos de bitácora bajo el directorio `logs/`:
- `logs/coordinador.log` (Identificador `0`)
- `logs/P1.log`, `logs/P2.log`, `logs/P3.log` (Identificadores `1`, `2`, `3`)

Cada línea sigue estrictamente el formato delimitado por punto y coma:

```text
lamport;id_proceso;evento;detalle
```

**Significado de los campos:**
1. `lamport`: Valor entero positivo del reloj lógico de Lamport al momento de registrar el evento.
2. `id_proceso`: Identificador numérico del proceso (`0` = Coordinador, `1` = P1, `2` = P2, `3` = P3).
3. `evento`: Etiqueta del evento, perteneciente al vocabulario controlado:
   - `INICIO`: Arranque e inicialización del proceso.
   - `ENVIAR`: Emisión de solicitud hacia el coordinador.
   - `RECIBIR`: Recepción de solicitud o respuesta.
   - `ENTRADA_PERMITIDA`: Acceso autorizado por existir cupo.
   - `ENTRADA_RECHAZADA`: Acceso denegado por aforo completo.
   - `SALIDA_REGISTRADA`: Egreso procesado y contador decrementado.
   - `LATIDO`: Latido recibido y actualizado.
   - `PUERTA_ACTIVA`: Puerta marcada como operativa.
   - `PUERTA_CAIDA`: Puerta marcada como caída por expirar el umbral de 6 s.
   - `TOKEN_INVALIDO`: Solicitud rechazada por credencial errónea.
   - `COORDINADOR_CAIDO`: Pérdida de comunicación con el coordinador remoto.
   - `FIN`: Detención controlada del proceso.
4. `detalle`: Parámetros contextuales en formato clave=valor (ej. `tipo=ENTRAR,destino=0`, `puerta=1,dentro=4,aforo=10`).

---

## 3. Compilación del Informe

La documentación formal se encuentra redactada en LaTeX dentro del directorio `docs/`.

### Requisitos de Compilación
- **Compilador:** `pdflatex` (distribución TeX Live o MiKTeX).
- **Paquetes requeridos:** `babel` (español), `geometry`, `graphicx`, `booktabs`, `tabularx`, `float`, `caption`, `enumitem`, `microtype`, `xcolor`, `amsmath`.

### Documentos Disponibles

1. **Informe Técnico Principal:**
   - **Archivo fuente:** `docs/informe.tex`
   - **Documento generado:** `docs/informe.pdf`
2. **Hoja de Datos Identificativos de Entrega:**
   - **Archivo fuente:** `docs/entrega_SGA.tex`
   - **Documento generado:** `docs/entrega_SGA.pdf`

### Orden de Comandos de Compilación

Es fundamental situarse en la carpeta `docs/` para que el compilador resuelva correctamente las rutas relativas a las figuras (`img/E0.png`):

```bash
cd docs
pdflatex -interaction=nonstopmode informe.tex
pdflatex -interaction=nonstopmode informe.tex
```

> **Nota:** Se ejecuta `pdflatex` **dos veces** consecutivas para resolver correctamente las referencias cruzadas (`\ref`, `\label`), numeración de tablas y figuras.

Para compilar la hoja de entrega identificativa:

```bash
pdflatex -interaction=nonstopmode entrega_SGA.tex
```

---

## 4. Código Reutilizado

> **No se reutilizó código**

Todo el código fuente de esta práctica fue desarrollado de forma original para la actividad AP-U1, implementando sobre la biblioteca estándar de Java SE (`java.rmi`, `java.util.concurrent`, `java.nio.file`) los mecanismos de:
- Protocolo distribuido cliente-servidor mediante Java RMI.
- Sincronización y exclusión mutua de sección crítica.
- Detector de fallos por tiempo de vida y latidos periódicos.
- Algoritmo de relojes lógicos y ordenamiento causal de Lamport.
