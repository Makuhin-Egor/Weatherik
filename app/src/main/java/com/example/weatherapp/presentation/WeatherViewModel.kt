package com.example.weatherapp.presentation

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.domain.model.CityInfo
import com.example.weatherapp.domain.model.HourlyInfo
import com.example.weatherapp.domain.model.WeatherData
import com.example.weatherapp.domain.model.WeatherInfo
import com.example.weatherapp.domain.repository.WeatherRepository
import com.example.weatherapp.location.LocationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

data class WeatherUiState(
    val cityName: String = "Locating...",
    val currentTemp: Double? = null,
    val feelsLike: Double? = null,
    val currentWeatherCode: Int = 0,
    val currentIsDay: Boolean = true,
    val weather: List<WeatherInfo> = emptyList(),
    val hourly: List<HourlyInfo> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isLocationError: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<CityInfo> = emptyList(),
    val isSearching: Boolean = false,
    val isSearchError: Boolean = false
)

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository,
    private val locationManager: LocationManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private var currentLat: Double = 0.0
    private var currentLon: Double = 0.0

    init {
        loadWeatherByLocation()
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    fun loadWeatherByLocation() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, cityName = "Locating...", error = null, isLocationError = false, weather = emptyList(), hourly = emptyList(), currentTemp = null)

            if (!isNetworkAvailable()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    cityName = "Unknown",
                    error = "No internet connection.\nPlease check your network.",
                    isLocationError = true
                )
                return@launch
            }

            try {
                val location = withTimeoutOrNull(5000L) {
                    locationManager.getCurrentLocation()
                }

                if (location != null) {
                    currentLat = location.latitude
                    currentLon = location.longitude
                    _uiState.value = _uiState.value.copy(cityName = "Current Location")
                    fetchWeather()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        cityName = "Unknown",
                        error = "Unable to determine location.\nPlease check internet and GPS.",
                        isLocationError = true
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    cityName = "Unknown",
                    error = "Location access denied.",
                    isLocationError = true
                )
            }
        }
    }

    fun selectCity(city: CityInfo) {
        currentLat = city.latitude
        currentLon = city.longitude

        val displayName = if (city.region.isNotBlank() && city.region != city.name) {
            "${city.name}, ${city.region}"
        } else {
            city.name
        }

        _uiState.value = _uiState.value.copy(
            cityName = displayName,
            searchResults = emptyList(),
            searchQuery = "",
            weather = emptyList(),
            hourly = emptyList(),
            currentTemp = null,
            error = null,
            isLocationError = false
        )
        fetchWeather()
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query, isSearchError = false)
        if (query.length > 2) {
            searchCities(query)
        } else {
            _uiState.value = _uiState.value.copy(searchResults = emptyList(), isSearchError = false)
        }
    }

    fun retrySearch() {
        val currentQuery = _uiState.value.searchQuery
        if (currentQuery.length > 2) {
            searchCities(currentQuery)
        }
    }

    fun retryWeatherLoad() {
        if (_uiState.value.isLocationError) {
            loadWeatherByLocation()
        } else {
            fetchWeather()
        }
    }

    private fun searchCities(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true, isSearchError = false)

            if (!isNetworkAvailable()) {
                _uiState.value = _uiState.value.copy(searchResults = emptyList(), isSearching = false, isSearchError = true)
                return@launch
            }

            val result = withTimeoutOrNull(5000L) {
                repository.searchCities(query)
            }

            if (result == null) {
                _uiState.value = _uiState.value.copy(searchResults = emptyList(), isSearching = false, isSearchError = true)
            } else {
                result.onSuccess { list ->
                    _uiState.value = _uiState.value.copy(searchResults = list, isSearching = false, isSearchError = false)
                }
                result.onFailure {
                    _uiState.value = _uiState.value.copy(searchResults = emptyList(), isSearching = false, isSearchError = true)
                }
            }
        }
    }

    private fun fetchWeather() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            if (!isNetworkAvailable()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentTemp = null,
                    weather = emptyList(),
                    hourly = emptyList(),
                    error = "No internet connection.\nPlease check your network.",
                    isLocationError = false
                )
                return@launch
            }

            repository.getWeather(lat = currentLat, lon = currentLon, cityName = _uiState.value.cityName)
                .onSuccess { data: WeatherData ->
                    _uiState.value = _uiState.value.copy(
                        currentTemp = data.currentTemp,
                        feelsLike = data.feelsLike,
                        currentWeatherCode = data.currentWeatherCode,
                        currentIsDay = data.currentIsDay,
                        weather = data.daily,
                        hourly = data.hourly,
                        isLoading = false
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        currentTemp = null,
                        weather = emptyList(),
                        hourly = emptyList(),
                        error = "Failed to load weather.\nCheck internet connection.",
                        isLoading = false,
                        isLocationError = false
                    )
                }
        }
    }
}
