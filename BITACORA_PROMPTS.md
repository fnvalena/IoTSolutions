# Bitácora de Prompts – IoT Solutions

**Asignatura:** Aplicaciones Móviles para IoT · INACAP La Serena
**Evaluación:** Sumativa I – Taller "Aplicación Android (APK)"
**Estudiante:** Valentina Ferreira Alfaro
**Herramienta de IA:** Claude (Anthropic)

Este documento registra los prompts usados durante el desarrollo, qué se obtuvo con cada uno y cómo se revisó el resultado.

---

## Prompt 01 – Análisis de la pauta de evaluación

**Fecha:** 10-11-2026
**Objetivo:** Entender los requerimientos y el puntaje antes de empezar a programar.

```text
Actúa como desarrollador Android senior y docente de programación móvil.
Adjunto la pauta de la Evaluación Sumativa I de "Aplicaciones Móviles para IoT".

1. Resume el contexto del caso y los requerimientos funcionales y técnicos,
   indicando el puntaje de cada uno.
2. Identifica ambigüedades o inconsistencias en la pauta (fechas, escala de
   notas, fórmulas, datos que no aparecen en los mockups).
3. Propón cómo abordar el desarrollo (proyecto completo, avance por pantalla
   o revisión de código existente).

Responde en español, de forma breve y ordenada.
```

**Resultado:** Resumen de las 5 Activities y de los 30 puntos técnicos. Se detectaron tres puntos a aclarar: el día de entrega (el 17-11-2026 cae martes, no viernes), la nota 4.0 (el 60% de 100 son 60 puntos, no 30) y la necesidad de un campo *costo unitario* para las fórmulas.

---

## Prompt 02 – Generación del proyecto base

**Fecha:** 10-11-2026
**Objetivo:** Obtener un proyecto funcional que cumpla todos los requerimientos de la pauta.

```text
Desarrolla un proyecto completo de Android Studio para la app "IoT Solutions"
(gestión de una bodega inteligente), según la pauta adjunta.

Stack y restricciones:
- Kotlin + Jetpack Compose (Material 3), Gradle Kotlin DSL.
- Navegación entre Activities mediante Intents explícitos (una Activity por pantalla).
- Sin base de datos: credenciales y datos en variables locales (objeto en memoria).
- Código organizado en paquetes: model, ui (por pantalla), util.
- Diseño responsivo y componentes modernos.

Pantallas:
1. Login: validación con variables locales y mensaje de error.
2. Bienvenida: nombre, fotografía, logo y 4 botones de navegación.
3. Perfil: visualizar y editar nombre, correo, teléfono, tipo de usuario y foto.
4. Productos: lista (nombre, precio, stock) y mantenedor con galería de imágenes,
   edición de stock y precio, y cálculo de stock actual, precio de venta,
   % de ganancia y valor total del stock, usando exactamente las fórmulas de la pauta.
5. Monitoreo: temperatura y humedad simuladas, y control de luces y puerta.

Entregables: proyecto listo para abrir en Android Studio, README con instrucciones
de ejecución y generación del APK, y una tabla que relacione cada requerimiento
con el archivo donde se implementa. Comenta el código en español.
```

**Resultado:** Proyecto con 6 Activities, `DataRepository` como almacenamiento en memoria, `InventoryCalculator` con las fórmulas, README y tabla de cumplimiento.

---

## Prompt 03 – Control de versiones con GitHub

**Fecha:** 10-11-2026
**Objetivo:** Versionar el proyecto y tenerlo respaldado en GitHub.

```text
Necesito conectar el proyecto de Android Studio con un repositorio en GitHub
(usuario fnvalena) para versionar el desarrollo.

- Indica los pasos para hacerlo desde Android Studio (login, Share Project on
  GitHub, primer commit y push).
- Incluye la alternativa por terminal con git.
- Verifica que el .gitignore excluya archivos generados y locales
  (build/, .gradle/, .idea/, local.properties).
```

**Resultado:** Guía paso a paso desde Android Studio, alternativa con comandos de git y `.gitignore` ya configurado en el proyecto.

---

## Prompt 04 – Rediseño de la interfaz (UI/UX)

**Fecha:** 10-11-2026
**Objetivo:** Ir más allá del mockup de referencia, con una interfaz moderna y dinámica, sin perder ningún requerimiento.

```text
Los mockups de la pauta son solo referencia. Rediseña la interfaz de la app
para que sea moderna, atractiva y dinámica, manteniendo todos los requerimientos
funcionales y técnicos.

Lineamientos:
- Identidad visual coherente: paleta de marca con degradado, esquinas redondeadas,
  soporte para modo claro y oscuro.
- Microinteracciones y animaciones con las APIs de Compose (animate*AsState,
  AnimatedVisibility, AnimatedContent, InfiniteTransition), sin librerías externas.
- Login: logo animado, indicador de carga y animación de error.
- Bienvenida: encabezado tipo dashboard, saludo según la hora, indicadores del
  inventario y menú en mosaicos con animación de entrada.
- Productos: búsqueda, orden, barra de stock y animación de la lista.
- Mantenedor: galería deslizable con indicador de páginas, ajuste del margen con
  slider, botones rápidos de stock y resultados con números animados.
- Bodega: indicadores circulares (gauges), gráfico del historial de temperatura
  en vivo y controles de luces y puerta con animación de estado.
- Transiciones deslizantes entre Activities.

Mantén el código organizado y comentado, y explica qué se cambió.
```

**Resultado:** Nuevos componentes reutilizables (`CircularGauge`, `Sparkline`, `PulsingLogo`, `animatedDouble`), tema con degradado y transiciones entre Activities. Todas las pantallas fueron rediseñadas.

---

## Prompt 05 – Documentación del uso de IA

**Fecha:** 10-11-2026
**Objetivo:** Dejar registro de los prompts usados en el proyecto.

```text
Crea una bitácora en Markdown con los prompts usados en este proyecto, redactados
de forma profesional. Para cada uno indica: número, fecha, objetivo, prompt y
resultado obtenido. Mantenla actualizada con cada nueva solicitud.
```

**Resultado:** Este documento (`BITACORA_PROMPTS.md`).

---

## Plantilla para próximos prompts

```text
Rol: Actúa como <rol experto>.
Contexto: <proyecto, tecnología y estado actual>.
Tarea: <qué se necesita, en una frase>.
Requisitos:
- <requisito 1>
- <requisito 2>
Restricciones: <lo que no se debe cambiar o usar>.
Formato de salida: <código, pasos, tabla, archivo…>.
```
