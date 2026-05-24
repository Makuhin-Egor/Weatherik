# 🌤️ Weatherik - Kotlin Android Weather Application

A modern, feature-rich weather application built with **Kotlin** and **Jetpack Compose**, providing real-time weather data, hourly forecasts, and 10-day predictions with a beautiful, intuitive UI.

---

## ✨ Features

### 🌡️ Weather Data
- **Current Weather**: Real-time temperature, weather condition, and visual emoji/icon representation
- **"Feels Like" Temperature**: Perceived temperature for better comfort assessment
- **Daily Min/Max**: Temperature range for the current day

### 📅 Forecasts
- **Hourly Forecast**: Scrollable list showing weather predictions for the next 24+ hours
- **10-Day Forecast**: Extended outlook with temperature bars visualizing daily ranges
- **Smart Date Labels**: Shows "Today", "Mon", "Tue", etc., with automatic localization-ready formatting

### 🔍 Search & Location
- **Geolocation Support**: Auto-detect user location with runtime permission handling
- **City Search**: Search for any city worldwide with autocomplete suggestions
- **Location Quick-Refresh**: One-tap button to reload weather for current GPS position

### 🎨 UI/UX
- **Modern Jetpack Compose UI**: Fully declarative, responsive interface
- **Animated Loading States**: Lottie animations for smooth loading experiences
- **Gradient Backgrounds & Cards**: Visually appealing dark theme with soft blue/orange accents
- **Temperature Visualization**: Custom Canvas-based gradient bars for daily temperature ranges
- **Error Handling**: User-friendly error states with retry actions

### ⚙️ Technical
- **Offline Awareness**: Network connectivity checks with appropriate error messages
- **Timeout Handling**: 5-second timeout on network requests to prevent hanging
- **State Management**: Unidirectional data flow with `StateFlow` and MVVM architecture

---

## 🛠️ Tech Stack

| Category | Technologies |
|----------|-------------|
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose, Material 3 |
| **Architecture** | MVVM, Clean Architecture principles |
| **Dependency Injection** | Hilt |
| **Async/Coroutines** | Kotlin Coroutines, Flow |
| **Animations** | Lottie Compose |
| **Location** | Android Location Services, Runtime Permissions |
| **Networking** | Retrofit/OKHttp (via Repository layer) |
| **Date/Time** | Java Time API (`java.time`) |

---

<p align="center">Made with ❤️ and Kotlin</p>