package com.example.weatherapp.presentation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.weatherapp.domain.model.CityInfo
import com.example.weatherapp.domain.model.HourlyInfo
import com.example.weatherapp.domain.model.WeatherInfo
import com.example.weatherapp.domain.util.weatherCodeToDescription
import com.example.weatherapp.domain.util.weatherCodeToIcon
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private val BackgroundGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF0F3460),
        Color(0xFF16213E),
        Color(0xFF1A1A2E)
    )
)

private val CardBackgroundColor = Color(0xFFFFFFFF).copy(alpha = 0.07f)
private val SoftBlue = Color(0xFF90CAF9)
private val SoftOrange = Color(0xFFFFAB91)
private val TempBarGradient = Brush.horizontalGradient(colors = listOf(SoftBlue, SoftOrange))

@Composable
fun LottieLoadingAnimation(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("location_loading.json"))
    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(viewModel: WeatherViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showSearchDialog by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (granted) viewModel.loadWeatherByLocation()
    }

    LaunchedEffect(Unit) {
        if (viewModel.uiState.value.currentTemp != null) return@LaunchedEffect
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.loadWeatherByLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
            )
        }
    }

    if (showSearchDialog) {
        SearchCityDialog(
            query = uiState.searchQuery,
            results = uiState.searchResults,
            isSearching = uiState.isSearching,
            isSearchError = uiState.isSearchError,
            onQueryChange = { viewModel.updateSearchQuery(it) },
            onRetry = { viewModel.retrySearch() },
            onCitySelected = { viewModel.selectCity(it); showSearchDialog = false },
            onDismiss = { showSearchDialog = false }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGradient)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        uiState.cityName,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                },
                actions = {
                    IconButton(onClick = {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                        if (hasPermission) {
                            viewModel.loadWeatherByLocation()
                        } else {
                            locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION))
                        }
                    }) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = Color.White.copy(alpha = 0.8f))
                    }
                    IconButton(onClick = { showSearchDialog = true }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White.copy(alpha = 0.8f))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            when {
                uiState.isLoading && uiState.currentTemp == null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LottieLoadingAnimation(modifier = Modifier.size(120.dp))
                    }
                }
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Oops!",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                uiState.error ?: "Unknown error",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Card(
                                modifier = Modifier.clickable { viewModel.retryWeatherLoad() },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = SoftBlue.copy(alpha = 0.2f))
                            ) {
                                Text(
                                    "Try Again",
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                                    color = SoftBlue,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
                else -> {
                    val today = uiState.weather.firstOrNull()
                    val currentTemp = uiState.currentTemp
                    val feelsLike = uiState.feelsLike

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        if (currentTemp != null) {
                            item {
                                WeatherHeader(
                                    currentTemp = currentTemp,
                                    feelsLike = feelsLike,
                                    weatherCode = uiState.currentWeatherCode,
                                    isDay = uiState.currentIsDay,
                                    today = today
                                )
                            }
                        }

                        if (uiState.hourly.isNotEmpty()) {
                            item {
                                WeatherCard(modifier = Modifier.padding(top = 24.dp)) {
                                    Column {
                                        Text(
                                            "HOURLY FORECAST",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White.copy(alpha = 0.6f),
                                            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                                        )
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                                            items(uiState.hourly, key = { it.time }) { hour -> HourlyItem(hour) }
                                        }
                                    }
                                }
                            }
                        }

                        if (uiState.weather.size > 1) {
                            item {
                                WeatherCard(modifier = Modifier.padding(top = 16.dp, bottom = 32.dp)) {
                                    Column {
                                        Text(
                                            "10-DAY FORECAST",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White.copy(alpha = 0.6f),
                                            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                                        )
                                        val weekMin = uiState.weather.minOf { it.tempMin }
                                        val weekMax = uiState.weather.maxOf { it.tempMax }

                                        uiState.weather.forEachIndexed { index, day ->
                                            DailyRow(day, weekMin, weekMax)
                                            if (index < uiState.weather.size - 1) {
                                                Spacer(modifier = Modifier.height(12.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(0.5.dp)
                                                        .background(Color.White.copy(alpha = 0.1f))
                                                )
                                                Spacer(modifier = Modifier.height(12.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherHeader(currentTemp: Double, feelsLike: Double?, weatherCode: Int, isDay: Boolean, today: WeatherInfo?) {
    val icon = weatherCodeToIcon(weatherCode, isDay)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(icon, fontSize = 72.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "${currentTemp.toInt()}°",
            fontSize = 80.sp,
            fontWeight = FontWeight.Normal,
            color = Color.White,
            lineHeight = 80.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            weatherCodeToDescription(weatherCode).replaceFirstChar { it.uppercase() },
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.9f)
        )
        if (feelsLike != null) {
            Text(
                "Feels like ${feelsLike.toInt()}°",
                fontSize = 15.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
        if (today != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Max. ${today.tempMax.toInt()}°  Min. ${today.tempMin.toInt()}°",
                fontSize = 15.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun WeatherCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Box(modifier = Modifier.padding(20.dp)) {
            content()
        }
    }
}

@Composable
fun HourlyItem(hourly: HourlyInfo) {
    val time = remember(hourly.time) {
        try {
            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
            val parsedTime = LocalDateTime.parse(hourly.time, inputFormatter)
            val currentTime = LocalDateTime.now()

            if (parsedTime.truncatedTo(ChronoUnit.HOURS) == currentTime.truncatedTo(ChronoUnit.HOURS)) {
                "Now"
            } else {
                val outputFormatter = DateTimeFormatter.ofPattern("HH:mm")
                parsedTime.format(outputFormatter)
            }
        } catch (e: Exception) { hourly.time }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(time, fontSize = 13.sp, color = Color.White.copy(alpha = 0.6f))
        Text(hourly.iconEmoji, fontSize = 24.sp)
        Text("${hourly.temp.toInt()}°", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.White)
    }
}

@Composable
fun DailyRow(day: WeatherInfo, weekMin: Double, weekMax: Double) {
    val dayName = remember(day.date) {
        try {
            val date = LocalDate.parse(day.date)
            if (date == LocalDate.now()) {
                "Today"
            } else {
                date.dayOfWeek.name.let {
                    when (it) {
                        "MONDAY" -> "Mon"
                        "TUESDAY" -> "Tue"
                        "WEDNESDAY" -> "Wed"
                        "THURSDAY" -> "Thu"
                        "FRIDAY" -> "Fri"
                        "SATURDAY" -> "Sat"
                        "SUNDAY" -> "Sun"
                        else -> it
                    }
                }
            }
        } catch (e: Exception) { day.date }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            dayName,
            color = Color.White,
            fontSize = 15.sp,
            modifier = Modifier.width(56.dp)
        )
        Text(day.iconEmoji, fontSize = 22.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            "${day.tempMin.toInt()}°",
            fontSize = 15.sp,
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.width(35.dp)
        )

        TemperatureBar(
            currentMin = day.tempMin,
            currentMax = day.tempMax,
            weekMin = weekMin,
            weekMax = weekMax,
            modifier = Modifier
                .weight(1f)
                .height(5.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))
        Text(
            "${day.tempMax.toInt()}°",
            fontSize = 15.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(35.dp)
        )
    }
}

@Composable
fun TemperatureBar(
    currentMin: Double,
    currentMax: Double,
    weekMin: Double,
    weekMax: Double,
    modifier: Modifier = Modifier
) {
    val range = weekMax - weekMin
    val startFraction = if (range > 0) ((currentMin - weekMin) / range).toFloat() else 0f
    val endFraction = if (range > 0) ((currentMax - weekMin) / range).toFloat() else 1f

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val cornerRadius = CornerRadius(canvasHeight)

        drawRoundRect(
            color = Color.White.copy(alpha = 0.15f),
            cornerRadius = cornerRadius
        )

        drawRoundRect(
            brush = TempBarGradient,
            topLeft = Offset(startFraction * canvasWidth, 0f),
            size = Size((endFraction - startFraction) * canvasWidth, canvasHeight),
            cornerRadius = cornerRadius
        )
    }
}

@Composable
fun SearchCityDialog(
    query: String,
    results: List<CityInfo>,
    isSearching: Boolean,
    isSearchError: Boolean,
    onQueryChange: (String) -> Unit,
    onRetry: () -> Unit,
    onCitySelected: (CityInfo) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF16213E)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Search City", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = Color.White, modifier = Modifier.padding(bottom = 16.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    label = { Text("Enter city name", color = Color.White.copy(alpha = 0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 16.sp),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SoftBlue,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        cursorColor = SoftBlue
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (isSearching) {
                    LottieLoadingAnimation(modifier = Modifier.size(56.dp).align(Alignment.CenterHorizontally))
                } else if (isSearchError) {
                    Text(
                        "Network error. Tap to retry.",
                        color = SoftBlue,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onRetry() }
                            .padding(8.dp)
                    )
                } else if (results.isEmpty() && query.length > 2) {
                    Text("Nothing found", color = Color.White.copy(alpha = 0.4f))
                } else {
                    LazyColumn(modifier = Modifier.height(200.dp)) {
                        items(results) { city ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onCitySelected(city) }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = SoftBlue.copy(alpha = 0.7f))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (city.region.isNotBlank() && city.region != city.name) "${city.name}, ${city.region}" else city.name,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
