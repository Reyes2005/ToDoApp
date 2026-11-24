# Especificaciones Técnicas, Requerimientos y Librerías — ToDoApp

Este documento detalla los requisitos del entorno, las versiones de las herramientas de compilación, las librerías utilizadas y las instrucciones técnicas para compilar y ejecutar el proyecto.

---

## 1. Requerimientos del Entorno de Desarrollo

Para compilar o modificar el proyecto desde Android Studio o mediante la línea de comandos (CLI), se requiere el siguiente entorno:

| Componente | Versión Requerida | Función en el Proyecto |
| :--- | :--- | :--- |
| **Java Development Kit (JDK)** | `JDK 17` o `JDK 21 LTS` | Entorno de ejecución para Gradle, el compilador de Kotlin y el procesador KSP. |
| **Kotlin** | `2.0.21` | Lenguaje de programación principal y compilador del plugin de Jetpack Compose. |
| **Gradle** | `8.12` | Sistema de automatización de compilación y gestión de dependencias. |
| **Android Gradle Plugin (AGP)** | `8.7.2` | Integración del flujo de construcción de Android (`assembleDebug`, `installDebug`, `testDebugUnitTest`). |
| **KSP (Kotlin Symbol Processing)** | `2.0.21-1.0.27` | Procesador de símbolos utilizado por Room para generar las implementaciones SQL en tiempo de compilación (`TaskDao_Impl`, `ToDoDatabase_Impl`). |
| **Android SDK Platform** | `API 35` (Android 15) | Plataforma de compilación (`compileSdk = 35` y `targetSdk = 35`). |
| **Android SDK Build-Tools** | `35.0.0` | Herramientas de compilación de recursos, conversión de bytecode (`d8`), alineación (`zipalign`) y firma (`apksigner`). |
| **Min SDK** | `API 24` (Android 7.0+) | Versión mínima de Android compatible para instalar y ejecutar la aplicación. |

---

## 2. Librerías y Dependencias del Módulo (`app/build.gradle.kts`)

El proyecto mantiene compatibilidad estricta entre versiones mediante el uso del **Compose Bill of Materials (BOM)** y artefactos estables de **AndroidX**:

### 2.1. Núcleo de Android y Ciclo de Vida
* `androidx.core:core-ktx:1.13.1`: Extensiones idiomáticas de Kotlin para las APIs base del sistema Android.
* `androidx.lifecycle:lifecycle-runtime-ktx:2.8.7`: Soporte de coroutines vinculadas al ciclo de vida de la actividad.
* `androidx.lifecycle:lifecycle-runtime-compose:2.8.7`: Provee `collectAsStateWithLifecycle()` para observar flujos `StateFlow` de forma segura en Jetpack Compose.
* `androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7`: Provee la clase `ViewModel` y el ámbito `viewModelScope`.
* `androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7`: Integración de `viewModel()` dentro de funciones `@Composable`.
* `androidx.activity:activity-compose:1.9.3`: Provee `ComponentActivity.setContent {}` para alojar la interfaz de Jetpack Compose.

### 2.2. Interfaz de Usuario (Jetpack Compose + Material 3)
* `androidx.compose:compose-bom:2024.10.01`: Gestiona versiones compatibles entre todos los artefactos de Compose.
* `androidx.compose.ui:ui`, `ui-graphics`, `ui-tooling-preview`: Motor de renderizado declarativo, gráficos vectoriales y previsualización.
* `androidx.compose.material3:material3`: Componentes de Material Design 3 (`Scaffold`, `TopAppBar`, `Card`, `FilterChip`, `ExtendedFloatingActionButton`, `AlertDialog`, `OutlinedTextField`, `SnackbarHost`).
* `androidx.compose.material:material-icons-extended`: Conjunto de iconografía vectorial para estados de tareas, métricas y formularios.

### 2.3. Persistencia Local (Room Database sobre SQLite)
* `androidx.room:room-runtime:2.6.1`: Motor principal de gestión de la base de datos SQLite (`RoomDatabase`, `@Database`, `@Entity`, `@Dao`).
* `androidx.room:room-ktx:2.6.1`: Soporte para transacciones asíncronas con funciones `suspend` y consultas reactivas mediante `Flow`.
* `androidx.room:room-compiler:2.6.1` (vía `ksp`): Generador de código SQL verificado en tiempo de compilación.

### 2.4. Asincronía y Pruebas Unitarias
* `org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0`: Despachadores (`Dispatchers.Main`, `Dispatchers.IO`) y primitivas de concurrencia estructurada en Android.
* `junit:junit:4.13.2`: Marco de ejecución de pruebas unitarias locales.
* `org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0`: Utilidades (`runTest`, `UnconfinedTestDispatcher`, `Dispatchers.setMain`) para probar flujos reactivos y coroutines en el `TaskViewModel`.

---

## 3. Configuración y Compilación del Proyecto

### Opción A: Desde Android Studio
1. Abrir **Android Studio** y seleccionar **Open**.
2. Elegir la carpeta raíz `ToDoApp`.
3. Esperar a que finalice la sincronización de Gradle (el archivo `local.properties` se configurará automáticamente con el SDK del sistema).
4. Ejecutar en un emulador o dispositivo físico con **Run** o generar el APK desde **Build > Build Bundle(s) / APK(s) > Build APK(s)**.

### Opción B: Desde la Terminal (CLI)
1. Asegurarse de tener configurado un JDK 17 o 21 y definir el archivo `local.properties` en la raíz del proyecto con la ruta local del Android SDK:
   ```properties
   sdk.dir=C\:\\Program Files (x86)\\Android\\android-sdk
   ```
2. **Compilar el APK Debug:**
   ```powershell
   .\gradlew.bat assembleDebug
   ```
   El APK compilado se genera en `app/build/outputs/apk/debug/app-debug.apk` (también disponible en la raíz como `ToDoApp.apk`).
3. **Ejecutar la Suite de Pruebas Unitarias:**
   ```powershell
   .\gradlew.bat testDebugUnitTest
   ```
4. **Instalar directamente en un Dispositivo Conectado por ADB:**
   ```powershell
   .\gradlew.bat installDebug
   ```
