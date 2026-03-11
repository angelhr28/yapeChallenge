# Yape Challenge - Gestión de Documentos Confidenciales

Aplicación Android de gestión de documentos confidenciales con protección biométrica, cifrado AES-256 y arquitectura modular.

## Arquitectura

### Multi-Module + Clean Architecture + MVI

```
:app                     → Entry point, Navigation, DI
:core                    → MVI base, Security, UI components
:domain                  → Models, Use Cases, Repository interfaces
:data                    → Room DB, Encrypted storage, Repository impl
:feature:documents       → Pantalla de lista de documentos
:feature:detail          → Pantalla de detalle de documento
```

### Patrón MVI (Model-View-Intent)

Se implementa un `MviViewModel<State, Intent, Effect>` como base:
- **State**: Estado inmutable de la UI observado via `StateFlow`
- **Intent**: Acciones del usuario que el ViewModel procesa
- **Effect**: Eventos one-shot (navegación, snackbars) via `Channel`

Este patrón garantiza un flujo de datos unidireccional y predecible, facilitando el testing y debugging.

### Clean Architecture

- **Domain Layer** (`:domain`): Modelos de negocio, interfaces de repositorio y use cases. Sin dependencias de Android framework. Capa más estable del sistema.
- **Data Layer** (`:data`): Implementaciones concretas de repositorios, Room DB, almacenamiento cifrado. Depende solo de `:domain`.
- **Presentation Layer** (`:feature:*`): Screens + ViewModels usando MVI. Dependen de `:core` y `:domain`, nunca de `:data`.

### Dependency Rule

Las dependencias fluyen hacia adentro: `feature → domain ← data`. La capa de dominio no conoce detalles de implementación (DB, cifrado, UI).

## Decisiones Técnicas

### Cifrado AES-256-GCM
- Se usa **Android Keystore** para almacenar la clave AES-256 de forma segura en hardware
- Modo **GCM (Galois/Counter Mode)** que provee autenticación + cifrado
- Cada archivo se cifra con un IV único generado automáticamente
- El IV se almacena prepended al archivo cifrado para la desencriptación

### Autenticación Biométrica
- `BiometricPrompt` de AndroidX con soporte para `BIOMETRIC_STRONG` y `BIOMETRIC_WEAK`
- Requerida para **visualizar** documentos y para **eliminar** documentos
- Si la autenticación falla, se regresa a la pantalla anterior

### Prevención de Screenshots
- Se aplica `FLAG_SECURE` en la ventana al entrar al detalle
- Se remueve al salir via `DisposableEffect`

### Marca de Agua con Geolocalización
- Canvas composable que dibuja texto repetitivo rotado 30° con transparencia
- Texto incluye ubicación obtenida via **Geocoder** (nivel calle)
- Se usa `FusedLocationProviderClient` para coordenadas GPS

### Almacenamiento Seguro
- Los archivos se cifran y almacenan en el directorio interno de la app (`filesDir/secure_docs/`)
- No accesible por otras apps ni por el usuario sin root
- Metadatos en **Room Database** con foreign keys y cascade delete

### Pinch-to-Zoom
- Implementado con `detectTransformGestures` de Compose
- Soporta zoom de 1x a 5x con pan (desplazamiento)
- Se resetea al volver a escala 1x

## Librerías Externas

| Librería | Uso | Justificación |
|----------|-----|---------------|
| **Koin** | Inyección de dependencias | Ligero, sin generación de código, ideal para multi-módulo |
| **Room** | Base de datos local | ORM oficial de Android, soporte de Flow reactivo |
| **Navigation Compose** | Navegación type-safe | Rutas tipadas con Kotlin Serialization |
| **CameraX** | Captura de fotos | API moderna de cámara, lifecycle-aware |
| **Coil** | Carga de imágenes | Nativo de Kotlin, soporte de Compose |
| **Biometric** | Autenticación biométrica | API oficial de AndroidX |
| **Play Services Location** | Geolocalización | Geocoding para obtener nombre de calle |
| **MockK** | Mocking en tests | Idiomatic para Kotlin, soporte de coroutines |
| **Turbine** | Testing de Flows | Testing de StateFlow/SharedFlow con API intuitiva |

## Testing

### Unit Tests
- **Use Cases**: Verifican delegación correcta al repositorio y transformación de datos
- **ViewModels**: Verifican flujo MVI (intent → state + effects) con `Turbine`
- **Repository**: Verifica mapeo entidad ↔ dominio, cifrado y operaciones CRUD

### Estructura de Tests
```
domain/src/test/        → GetDocumentsUseCaseTest, AddDocumentUseCaseTest, DeleteDocumentUseCaseTest
data/src/test/          → DocumentRepositoryImplTest
feature/documents/test/ → DocumentsViewModelTest
feature/detail/test/    → DetailViewModelTest
```

## Pantallas

### 1. Pantalla Inicial (DocumentsScreen)
- Lista de documentos con cards informativas (nombre, tipo, tamaño, fecha)
- **Filter chips** para filtrar por tipo (Todos, PDF, Imágenes)
- **FAB** con menú desplegable: Galería o Cámara
- Estado vacío con ilustración cuando no hay documentos

### 2. Detalle de Documento (DetailScreen)
- Autenticación biométrica obligatoria al entrar
- Prevención de screenshots (`FLAG_SECURE`)
- Visualización con pinch-to-zoom para imágenes
- Marca de agua semi-transparente con geolocalización
- Historial de accesos con fecha, hora y tipo de acción
- Eliminación con confirmación biométrica

## Stack Técnico

- **Kotlin** 2.0.21
- **Jetpack Compose** con Material 3
- **AGP** 9.0.1 / **Gradle** 9.2.1
- **compileSdk** 36 / **minSdk** 24
- **Coroutines + Flow** para asincronía reactiva

## Novedades de la Plataforma Utilizadas
- AGP 9.x con compileSdk minor API levels
- Kotlin 2.0+ con compose compiler plugin integrado
- Navigation Compose type-safe routes con Kotlin Serialization
- Material 3 dynamic color (Material You)
- CameraX con ProcessCameraProvider lifecycle binding
- Edge-to-edge display con `enableEdgeToEdge()`
