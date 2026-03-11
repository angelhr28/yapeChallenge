# Yape Challenge - Gestión de Documentos Confidenciales

Aplicación Android para gestión segura de documentos confidenciales con cifrado AES-256-GCM, autenticación biométrica, marca de agua con geolocalización y prevención de capturas de pantalla.

---

## Tabla de Contenidos

- [Arquitectura](#arquitectura)
- [Módulos](#módulos)
- [Flujos de la Aplicación](#flujos-de-la-aplicación)
- [Seguridad](#seguridad)
- [Pantallas](#pantallas)
- [Stack Técnico](#stack-técnico)
- [Librerías Externas](#librerías-externas)
- [Testing](#testing)
- [Configuración del Proyecto](#configuración-del-proyecto)

---

## Arquitectura

### Multi-Module + Clean Architecture + MVI

El proyecto sigue una arquitectura modular basada en **Clean Architecture** con el patrón **MVI (Model-View-Intent)** para la capa de presentación.

```
┌─────────────────────────────────────────────────┐
│                  :app                            │
│         Entry Point · Navigation · DI            │
└──────────┬──────────────────┬────────────────────┘
           │                  │
    ┌──────▼──────┐    ┌──────▼──────┐
    │  :feature:  │    │  :feature:  │
    │  documents  │    │   detail    │
    │  (Lista)    │    │  (Detalle)  │
    └──────┬──────┘    └──────┬──────┘
           │                  │
    ┌──────▼──────────────────▼──────┐
    │            :core               │
    │   MVI Base · Security · UI     │
    └──────┬─────────────────────────┘
           │
    ┌──────▼──────┐         ┌────────────┐
    │   :domain   │◄────────│   :data    │
    │  Models     │         │  Room DB   │
    │  Use Cases  │         │  Cifrado   │
    │  Interfaces │         │  Repos     │
    └─────────────┘         └────────────┘
```

### Dependency Rule

Las dependencias fluyen hacia adentro: `feature → domain ← data`. La capa de dominio no conoce detalles de implementación (DB, cifrado, UI).

### Patrón MVI (Model-View-Intent)

Se implementa un `MviViewModel<State, Intent, Effect>` como clase base abstracta:

```
Usuario ──► Intent ──► ViewModel.handleIntent() ──► setState()
                                                        │
                                                        ▼
UI ◄──── State (StateFlow) ◄─── Nuevo estado inmutable

Eventos one-shot: sendEffect() ──► Effect (Channel) ──► Navegación, Snackbars
```

- **State**: Estado inmutable de la UI observado via `StateFlow` (single source of truth)
- **Intent**: Acciones del usuario que el ViewModel procesa
- **Effect**: Eventos one-shot (navegación, snackbars) emitidos via `Channel`

---

## Módulos

| Módulo | Responsabilidad |
|--------|----------------|
| `:app` | Entry point, `MainActivity`, navegación, tema, configuración de Koin |
| `:core` | `MviViewModel` base, `CryptoManager`, `BiometricHelper`, componentes UI compartidos |
| `:domain` | Modelos (`Document`, `AccessLog`), interfaces de repositorio, use cases. **Sin dependencias Android** |
| `:data` | Room DB, `EncryptedFileManager`, implementación de repositorio, mappers entidad ↔ dominio |
| `:feature:documents` | Pantalla de lista de documentos (Screen + ViewModel + Contract) |
| `:feature:detail` | Pantalla de detalle con zoom, watermark y logs de acceso |

---

## Flujos de la Aplicación

### 1. Agregar Documento

```
Usuario presiona FAB
    │
    ├── Selecciona "Galería" ──► OpenDocument (image/*, application/pdf) ──► Bytes
    │
    └── Selecciona "Cámara" ──► CameraX Preview ──► Captura foto ──► Bytes
                                                                        │
                                                                        ▼
                                              AddDocumentUseCase.invoke(name, mimeType, bytes)
                                                                        │
                                                                        ▼
                                              DocumentRepositoryImpl.addDocument()
                                                    │
                                                    ├── CryptoManager.encrypt(bytes) ──► Archivo cifrado
                                                    │                                    en filesDir/secure_docs/
                                                    │
                                                    └── DocumentDao.insert(entity) ──► Metadatos en Room
                                                                        │
                                                                        ▼
                                                              ShowSuccess("Documento agregado")
```

### 2. Visualizar Documento

```
Usuario toca documento en la lista
    │
    ▼
NavigateToDetail(documentId)
    │
    ▼
DetailViewModel.LoadDocument(id)
    │
    ├── GetDocumentDetailUseCase ──► Carga metadatos del documento
    ├── GetAccessLogsUseCase ──► Carga historial de accesos
    └── Emite RequestBiometricAuth (Effect)
                │
                ▼
        BiometricPrompt aparece
                │
        ┌───────┴───────┐
        │               │
    Éxito           Fallo/Cancel
        │               │
        ▼               ▼
  OnAuthenticated    NavigateBack
        │
        ├── GetDecryptedDocumentUseCase ──► Descifra bytes en memoria
        ├── LogAccessUseCase(VIEW) ──► Registra acceso con ubicación
        ├── FLAG_SECURE activado ──► Previene screenshots
        └── FusedLocationProvider ──► Obtiene ubicación para watermark
                │
                ▼
        Imagen descifrada + Watermark + Zoom habilitado
```

### 3. Eliminar Documento

```
Usuario presiona botón eliminar
    │
    ▼
RequestDelete (Intent)
    │
    ▼
Emite RequestDeleteBiometricAuth (Effect)
    │
    ▼
BiometricPrompt de confirmación
    │
    ├── Éxito ──► ConfirmDelete (Intent)
    │                   │
    │                   ├── LogAccessUseCase(DELETE) ──► Registra acción
    │                   ├── DeleteDocumentUseCase ──► Elimina archivo + DB
    │                   └── Emite NavigateBack + ShowSuccess
    │
    └── Fallo ──► No action
```

### 4. Filtrar Documentos

```
Usuario toca Filter Chip (Todos / PDF / Imágenes)
    │
    ▼
FilterByType(DocumentType?) ──► GetDocumentsUseCase(filter)
    │                                       │
    │                                       ▼
    │                           Repository.getAll() o getByType()
    │                                       │
    ▼                                       ▼
selectedFilter actualizado         Lista filtrada via Flow reactivo
```

### 5. Navegación

```
NavGraph (Type-Safe con Kotlin Serialization)

DocumentsRoute ──────────────────► DetailRoute(documentId: Long)
 (Start Destination)    click          │
       ▲                               │
       └───────────── back ◄───────────┘
```

---

## Seguridad

### Cifrado AES-256-GCM

```
Cifrado:                              Descifrado:

Bytes originales                      Archivo cifrado
    │                                     │
    ▼                                     ▼
Generar IV aleatorio (12 bytes)       Leer tamaño IV (1 byte)
    │                                     │
    ▼                                     ▼
Cifrar con AES-256-GCM               Leer IV (N bytes)
(clave del Android Keystore)              │
    │                                     ▼
    ▼                                 Descifrar con GCM
[IV_size | IV | datos_cifrados]       (solo en memoria, nunca en disco)
    │                                     │
    ▼                                     ▼
Guardar en filesDir/secure_docs/      ByteArray en memoria
```

- **Android Keystore** almacena la clave AES-256 en hardware seguro
- **GCM** provee cifrado + autenticación (integridad)
- **IV único** por archivo previene ataques de repetición

### Autenticación Biométrica

- `BiometricPrompt` con `BIOMETRIC_STRONG | BIOMETRIC_WEAK`
- Requerida para: **visualizar** y **eliminar** documentos
- Fallo → retorno automático a pantalla anterior

### Prevención de Screenshots

- `FLAG_SECURE` aplicado al entrar al detalle
- Removido via `DisposableEffect` al salir
- Previene capturas y grabaciones de pantalla

### Marca de Agua con Geolocalización

- Canvas composable con texto repetitivo rotado 30°
- Semi-transparente para no obstruir el contenido
- Ubicación obtenida directamente en `DetailScreen` via `FusedLocationProviderClient`
- Geocoding reverso con `Geocoder` para obtener nombre de calle (`thoroughfare` + `subThoroughfare`)
- Fallback a coordenadas GPS si el geocoding no está disponible

### Almacenamiento

- Archivos cifrados en `context.filesDir/secure_docs/` (no accesible sin root)
- Metadatos en Room DB (sin datos sensibles)

---

## Pantallas

### DocumentsScreen (Lista)

| Elemento | Descripción |
|----------|-------------|
| TopAppBar | Título de la app |
| Filter Chips | Filtros: Todos, PDF, Imágenes |
| Document Cards | Nombre, tipo, tamaño, fecha de creación |
| FAB | Menú desplegable: Galería o Cámara |
| Empty State | Ilustración cuando no hay documentos |

**MVI Contract:**
- **State**: `documents`, `isLoading`, `selectedFilter`, `error`
- **Intents**: `LoadDocuments`, `FilterByType`, `AddDocument`, `OpenDocument`
- **Effects**: `NavigateToDetail`, `ShowError`, `ShowSuccess`

### DetailScreen (Detalle)

| Elemento | Descripción |
|----------|-------------|
| Back button | Regresar a la lista |
| Delete button | Eliminar con confirmación biométrica |
| ZoomableImage | Pinch-to-zoom (1x–5x) con pan (imágenes) |
| PdfViewer | Renderizado real de PDF con `PdfRenderer` + zoom |
| WatermarkOverlay | Marca de agua con geolocalización (nivel de calle) |
| AccessLogSection | Historial: fecha, acción, ubicación |

**MVI Contract:**
- **State**: `document`, `decryptedBytes`, `accessLogs`, `isLoading`, `isAuthenticated`, `currentLocation`, `error`
- **Intents**: `LoadDocument`, `OnAuthenticated`, `RequestDelete`, `ConfirmDelete`, `UpdateLocation`
- **Effects**: `RequestBiometricAuth`, `RequestDeleteBiometricAuth`, `NavigateBack`, `ShowError`, `ShowSuccess`

---

## Stack Técnico

| Tecnología | Versión | Uso |
|-----------|---------|-----|
| Kotlin | 2.0.21 | Lenguaje principal |
| Jetpack Compose | BOM 2024.09.00 | UI declarativa |
| Material 3 | Latest | Design system (Material You) |
| AGP | 9.0.1 | Build system |
| Gradle | 9.2.1 | Gestión de dependencias |
| compileSdk | 36 | API target |
| minSdk | 24 | Android 7.0+ |

### Novedades de Plataforma

- **AGP 9.x** con compileSdk minor API levels
- **Kotlin 2.0+** con Compose Compiler plugin integrado
- **Navigation Compose** type-safe routes con Kotlin Serialization
- **Material 3** dynamic color (Material You)
- **CameraX** con ProcessCameraProvider lifecycle binding
- **Edge-to-edge** display con `enableEdgeToEdge()`

---

## Librerías Externas

| Librería | Versión | Uso | Justificación |
|----------|---------|-----|---------------|
| **Koin** | 4.0.4 | Inyección de dependencias | Ligero, sin code generation, DSL legible, ideal para multi-módulo |
| **Room** | 2.7.1 | Base de datos local | ORM oficial, queries type-safe, soporte nativo de Flow |
| **Navigation Compose** | 2.9.0 | Navegación | Rutas tipadas con Kotlin Serialization, compile-time safety |
| **CameraX** | 1.5.0 | Captura de fotos | API moderna, lifecycle-aware, reemplaza Camera2 |
| **Coil** | 3.1.0 | Carga de imágenes | Nativo Kotlin, soporte Compose, ligero |
| **Biometric** | 1.4.0-alpha02 | Autenticación biométrica | API oficial AndroidX |
| **Security Crypto** | 1.0.0 | Encrypted SharedPreferences | Cifrado seguro de preferencias |
| **Play Services Location** | 21.3.0 | Geolocalización | FusedLocationProvider + Geocoder |
| **Coroutines** | 1.10.1 | Asincronía | Flow reactivo, structured concurrency |
| **KSP** | 2.0.21-1.0.28 | Procesamiento de anotaciones | Code generation para Room |

---

## Testing

### Estrategia

Los tests siguen la arquitectura por capas, verificando cada capa de forma aislada con mocks.

### Cobertura por Módulo

| Módulo | Tests | Qué verifica |
|--------|-------|-------------|
| `:domain` | `GetDocumentsUseCaseTest` | Delegación al repositorio, filtrado por tipo, lista vacía |
| `:domain` | `AddDocumentUseCaseTest` | Flujo de adición de documentos |
| `:domain` | `DeleteDocumentUseCaseTest` | Flujo de eliminación |
| `:data` | `DocumentRepositoryImplTest` | Mapeo entidad ↔ dominio, cifrado/descifrado, CRUD, logging |
| `:feature:documents` | `DocumentsViewModelTest` | Carga inicial, filtrado, navegación, efectos MVI |
| `:feature:detail` | `DetailViewModelTest` | Autenticación biométrica, carga, descifrado, logging de acceso |

### Frameworks de Testing

| Herramienta | Uso |
|-------------|-----|
| **JUnit 4** | Test runner |
| **MockK** | Mocking idiomático para Kotlin |
| **Turbine** | Testing de Flow/StateFlow con API declarativa |
| **Coroutines Test** | `StandardTestDispatcher` para control de coroutines |

### Estructura de Tests

```
domain/src/test/
└── usecase/
    ├── GetDocumentsUseCaseTest.kt
    ├── AddDocumentUseCaseTest.kt
    └── DeleteDocumentUseCaseTest.kt

data/src/test/
└── repository/
    └── DocumentRepositoryImplTest.kt

feature/documents/src/test/
└── DocumentsViewModelTest.kt

feature/detail/src/test/
└── DetailViewModelTest.kt
```

---

## Configuración del Proyecto

### Permisos (AndroidManifest)

| Permiso | Uso |
|---------|-----|
| `CAMERA` | Captura de fotos |
| `ACCESS_FINE_LOCATION` | GPS para marca de agua |
| `ACCESS_COARSE_LOCATION` | Ubicación por red |
| `USE_BIOMETRIC` | Autenticación biométrica |

### Base de Datos (Room)

**DocumentEntity** (`documents`)
| Campo | Tipo | Descripción |
|-------|------|-------------|
| `id` | Long (PK) | Auto-generado |
| `name` | String | Nombre del documento |
| `type` | String | "PDF" o "IMAGE" |
| `encryptedPath` | String | Ruta al archivo cifrado |
| `thumbnailPath` | String? | Miniatura (opcional) |
| `mimeType` | String | Tipo MIME |
| `fileSize` | Long | Tamaño en bytes |
| `createdAt` | Long | Timestamp de creación |
| `updatedAt` | Long | Timestamp de actualización |

**AccessLogEntity** (`access_logs`)
| Campo | Tipo | Descripción |
|-------|------|-------------|
| `id` | Long (PK) | Auto-generado |
| `documentId` | Long | FK al documento |
| `accessedAt` | Long | Timestamp del acceso |
| `action` | String | "VIEW" o "DELETE" |
| `location` | String? | Ubicación del acceso |

### Ejecución

```bash
# Compilar
./gradlew assembleDebug

# Tests unitarios
./gradlew test

# Tests por módulo
./gradlew :domain:test
./gradlew :data:test
./gradlew :feature:documents:test
./gradlew :feature:detail:test
```
