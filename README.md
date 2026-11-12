# IoT Solutions – Bodega Inteligente

App Android (Kotlin + Jetpack Compose) para la Evaluación Sumativa I de **Aplicaciones Móviles para IoT** (INACAP La Serena).

## Cómo abrirla y ejecutarla

1. Descomprime la carpeta `IoTSolutions`.
2. En Android Studio: **File → Open** → selecciona la carpeta `IoTSolutions` (la que contiene `settings.gradle.kts`).
3. Espera a que termine el *Gradle Sync* (la primera vez descarga dependencias, necesita internet).
   - Si Android Studio ofrece actualizar el *Android Gradle Plugin*, puedes aceptar sin problema.
4. Conecta el celular con **Depuración USB** activada (o usa un emulador) y presiona ▶ **Run**.

### Generar el APK

**Build → Build App Bundle(s) / APK(s) → Build APK(s)**.
El archivo queda en `app/build/outputs/apk/debug/app-debug.apk`. Lo puedes pasar al teléfono e instalarlo (hay que permitir "instalar apps de origen desconocido").

## Credenciales de prueba (variables locales)

| Usuario    | Contraseña | Tipo          |
|------------|------------|---------------|
| `admin`    | `admin123` | Administrador |
| `operador` | `1234`     | Operador      |

Están definidas en `model/DataRepository.kt`.

## Estructura del código

```
cl.inacap.iotsolutions
├── model/                 Datos y lógica (sin interfaz)
│   ├── User.kt            Usuario y tipos de usuario
│   ├── Product.kt         Producto e imágenes de la galería
│   ├── InventoryCalculator.kt  Fórmulas del mantenedor
│   └── DataRepository.kt  "Base de datos" en memoria (variables locales)
├── util/
│   └── Format.kt          Formato chileno de dinero y porcentajes
└── ui/
    ├── theme/             Colores y tema Material 3
    ├── components/        Barra superior, logo, foto, campos, tarjetas
    ├── login/             Activity 1 – LoginActivity
    ├── welcome/           Activity 2 – WelcomeActivity
    ├── profile/           Activity 3 – ProfileActivity
    ├── products/          Activity 4 – ProductListActivity + ProductDetailActivity
    └── warehouse/         Activity 5 – WarehouseActivity
```

## Cumplimiento de la pauta

| Requerimiento | Dónde |
|---|---|
| Validación de credenciales con variables locales | `DataRepository.login()` + `LoginActivity` |
| Mensaje de error si son inválidas | Tarjeta roja en `LoginScreen` |
| Nombre, foto y logo en la bienvenida | `WelcomeActivity` (`UserHeader`, `AppLogo`, `UserPhoto`) |
| 4 botones de navegación | `WelcomeActivity` → Intents a cada Activity |
| Ver/editar nombre, correo, teléfono, tipo y foto | `ProfileActivity` (foto con el selector de fotos del sistema) |
| Lista con nombre, precio y stock | `ProductListActivity` |
| Al tocar un producto → mantenedor | `ProductDetailActivity` |
| Galería de imágenes del producto | `ProductDetailActivity` (miniaturas + botón para agregar fotos) |
| Editar stock y precio + cálculos | `ProductDetailActivity` + `InventoryCalculator` |
| Temperatura y humedad simuladas | `WarehouseActivity` (sliders, valores aleatorios y simulación automática) |
| Encender/apagar luces y puerta | `WarehouseActivity` (`ControlCard`) |
| Jetpack Compose | Todas las pantallas son `@Composable` con Material 3 |
| Navegación entre Activities | `Intent` explícitos (`startActivity`) en cada pantalla |
| Variables locales sin BD | `DataRepository` (objeto en memoria con estados de Compose) |
| Código en paquetes | `model`, `util`, `ui/...` |
| Diseño responsivo | `BoxWithConstraints` (2 columnas en pantallas anchas), `widthIn`, `weight`, scroll, `LazyColumn` |

## Diseño e interacción

Los mockups de la pauta se tomaron solo como referencia. La interfaz incluye:

- **Tema propio**: degradado de marca, esquinas redondeadas y modo claro/oscuro.
- **Transiciones** deslizantes entre Activities (`res/anim` + `themes.xml`).
- **Login**: logo con ondas animadas, indicador de carga y sacudida de la tarjeta si las credenciales son incorrectas.
- **Bienvenida**: encabezado tipo dashboard, saludo según la hora, indicadores del inventario con números animados y menú en mosaicos con animación de entrada.
- **Productos**: buscador, orden por nombre/precio/stock, barra de stock (roja si queda poco) y animación al filtrar.
- **Mantenedor**: galería deslizable con puntos indicadores, slider del margen, botones rápidos ±1/±5/±10 y resultados animados.
- **Bodega**: gauges circulares dibujados con `Canvas`, gráfico del historial de temperatura en vivo, indicador "EN VIVO" y tarjetas de luces y puerta que cambian de color.

Los componentes reutilizables están en `ui/components` (`CircularGauge`, `Sparkline`, `PulsingLogo`, `animatedDouble`, etc.).

La bitácora de prompts usados está en `BITACORA_PROMPTS.md`.

## Fórmulas (`InventoryCalculator.kt`)

- **Stock actual** = Stock inicial ± unidades agregadas o retiradas
- **Precio de venta** = Costo unitario + (Costo unitario × margen)
- **% de ganancia** = ((Precio venta − Costo unitario) / Costo unitario) × 100
- **Valor total stock** = Stock actual × Precio de venta

En el mantenedor se puede editar el costo y el margen (el precio de venta se recalcula) o el precio de venta directamente (y entonces se recalcula el margen).

> Como no hay base de datos, los cambios se pierden al cerrar la app por completo. Esto es lo esperado en esta versión.
