# WeatherSnap 🌤️📸

WeatherSnap is a native Android application built as part of the Android Intern Assignment task. The app allows users to search for live weather updates by city, generate local weather reports using a custom CameraX interface, compress captured images to save storage space, and maintain a local log of saved reports using Room DB.

---

## 🚀 Tech Stack & Architecture

The project strictly follows clean coding standards and the recommended modern Android architecture:
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (with Material 3 components)
- **Architecture**: MVVM (Model-View-ViewModel) + Clean Architecture approach
- **Reactive Stream / Threading**: Kotlin Coroutines & StateFlow
- **Dependency Injection**: Hilt
- **Local Storage**: Room Database (with background thread execution)
- **Networking**: Retrofit + OkHttp (with HTTP Logging Interceptor for debugging)
- **Camera integration**: CameraX API

---

## 📦 Features Implemented

1. **Weather Search Screen**
    - Clean city search bar with real-time text validation.
    - Autocomplete suggestions pull automatically from the Open-Meteo Geocoding API once you type more than 2 letters.
    - Proper state management handling **Loading, Success, Empty, and Error** states with smooth Compose transitions.
    - Shows a structured weather card display upon selection, opening up actions to create new reports.

2. **Create Report Screen**
    - Locks down and displays the immutable weather data selected from the previous screen.
    - Provides text inputs for personal notes and a custom thumbnail box for the captured image preview.

3. **Custom Camera Screen**
    - Built completely using CameraX without relying on third-party device intents.
    - Features a live camera stream layout with straightforward Capture and Close triggers.

4. **Image Compression & Metrics**
    - Automatically downsizes heavy, high-res camera photos to a max boundary of 1024x1024 pixels right after capture.
    - Compares and displays **Original Size vs Compressed Size** side-by-side on screen before saving.

5. **Saved Reports List**
    - Feeds historical logs dynamically from Room using a coroutine-backed `Flow` stream.
    - Renders individual cards showing the compressed photo, captured weather metrics, user notes, size difference logs, and formatted dates.

---

## 💡 Developer Judgment Challenge

### My Approach to Handling Lifecycles & Memory:
1. **Preventing Silent Data Changes:** Instead of triggering a fresh network request when opening the creation flow, the app passes the explicitly chosen `WeatherSnapshot` across screens. This locks the precise weather metrics selected by the user, making sure data doesn't silently mutate due to background API shifts.
2. **Safe Background Threading:** Heavy bitmap processing and file storage work happen on a dedicated background thread pool (`ExecutorService`) so the UI never stutters or drops frames. Once processing is done, the app marshals back to the main thread (`ContextCompat.getMainExecutor`) to safely trigger Compose navigation without edge-case lifecycle crashes.
3. **Memory & Cache Cleanup:** To prevent the app from hogging RAM when clicking multiple photos back-to-back, bitmaps are explicitly recycled using `scaledBitmap?.recycle()`. Additionally, file streams are enclosed in `.use {}` blocks to automatically clear I/O resources and avoid caching leaks.

---

## 🛠️ Setup & How to Run

### Requirements
- Android Studio Iguana (or later)
- Gradle version 8.2+
- SDK 34 target (Minimum SDK supported: 26)

### Running the App
1. Clone the project locally:
   ```bash
   git clone [

2. Open Android Studio, click on Open Project, and target the root directory of this repository.

3. Let Gradle sync and download the dependencies completely.

4. Plug in an Android device via USB debugging (or boot up an emulator instance) and click the green Run button.


// Screen recording link is here.
https://drive.google.com/file/d/1ghJRIAK1AV5sSyrwAI6ext4Z9w5jMkiy/view?usp=sharing