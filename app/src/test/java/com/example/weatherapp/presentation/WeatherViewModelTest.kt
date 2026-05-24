package com.example.weatherapp.presentation

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.weatherapp.MainDispatcherRule
import com.example.weatherapp.domain.model.WeatherData
import com.example.weatherapp.domain.repository.WeatherRepository
import com.example.weatherapp.location.LocationManager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class WeatherViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mockRepository = mockk<WeatherRepository>()
    private val mockLocationManager = mockk<LocationManager>()
    private val mockContext = mockk<Context>()
    private val mockConnectivityManager = mockk<ConnectivityManager>()
    private val mockNetworkCapabilities = mockk<NetworkCapabilities>()

    private lateinit var viewModel: WeatherViewModel

    private val fakeWeatherData = WeatherData(
        currentTemp = 20.0,
        feelsLike = 18.0,
        currentWeatherCode = 0,
        currentIsDay = true,
        daily = emptyList(),
        hourly = emptyList()
    )

    @Before
    fun setup() {
        every { mockContext.getSystemService(Context.CONNECTIVITY_SERVICE) } returns mockConnectivityManager
        every { mockConnectivityManager.activeNetwork } returns mockk()
        every { mockConnectivityManager.getNetworkCapabilities(any()) } returns mockNetworkCapabilities
        every { mockNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns true
        every { mockNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) } returns true

        coEvery { mockRepository.getWeather(any(), any(), any()) } returns Result.success(fakeWeatherData)
    }

    @Test
    fun `when location found and weather loaded, ui state should be success`() = runTest {
        val fakeLocation = mockk<android.location.Location>(relaxed = true)
        every { fakeLocation.latitude } returns 55.75
        every { fakeLocation.longitude } returns 37.61

        coEvery { mockLocationManager.getCurrentLocation() } returns fakeLocation

        viewModel = WeatherViewModel(mockRepository, mockLocationManager, mockContext)

        val state = viewModel.uiState.value
        assertEquals("Current Location", state.cityName)
        assertEquals(20.0, state.currentTemp!!, 0.01)
        assertNull(state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun `when no network on startup, ui state should show location error`() = runTest {
        every { mockNetworkCapabilities.hasTransport(any()) } returns false

        viewModel = WeatherViewModel(mockRepository, mockLocationManager, mockContext)

        val state = viewModel.uiState.value
        assertEquals("Unknown", state.cityName)
        assertNotNull(state.error)
        assertTrue(state.isLocationError)
        assertFalse(state.isLoading)
    }

    @Test
    fun `when location fails but network exists, ui state should show location error`() = runTest {
        coEvery { mockLocationManager.getCurrentLocation() } returns null

        viewModel = WeatherViewModel(mockRepository, mockLocationManager, mockContext)

        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertTrue(state.isLocationError)
    }

    @Test
    fun `when weather fetch fails, ui state should show weather error but not location error`() = runTest {
        val fakeLocation = mockk<android.location.Location>(relaxed = true)
        coEvery { mockLocationManager.getCurrentLocation() } returns fakeLocation
        coEvery { mockRepository.getWeather(any(), any(), any()) } returns Result.failure(Exception("API Error"))

        viewModel = WeatherViewModel(mockRepository, mockLocationManager, mockContext)

        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertFalse("Should not be a location error", state.isLocationError)
        assertNull(state.currentTemp)
    }

    @Test
    fun `retryWeatherLoad should call fetchWeather if isLocationError is false`() = runTest {
        val fakeLocation = mockk<android.location.Location>(relaxed = true)
        coEvery { mockLocationManager.getCurrentLocation() } returns fakeLocation

        var fetchCount = 0
        coEvery { mockRepository.getWeather(any(), any(), any()) } coAnswers {
            fetchCount++
            if (fetchCount == 1) Result.failure(Exception("Fail")) else Result.success(fakeWeatherData)
        }

        viewModel = WeatherViewModel(mockRepository, mockLocationManager, mockContext)

        val errorState = viewModel.uiState.value
        assertNotNull(errorState.error)
        assertFalse(errorState.isLocationError)

        viewModel.retryWeatherLoad()

        val successState = viewModel.uiState.value
        assertNull(successState.error)
        assertNotNull(successState.currentTemp)
        assertEquals(20.0, successState.currentTemp!!, 0.01)
    }
}
