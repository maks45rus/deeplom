package com.example.raspisanieshgpu.main

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raspisanieshgpu.Data.DataManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainActivityVM : ViewModel() {
    private val _internetStatus = MutableStateFlow(true)
    val internetStatus: StateFlow<Boolean> = _internetStatus

    private val _dataLoadingStatus = MutableStateFlow(false)
    val dataLoadingStatus: StateFlow<Boolean> = _dataLoadingStatus

    // Добавляем периодическую проверку
    private var connectivityManager: ConnectivityManager? = null
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            updateConnectionStatus(true)
        }

        override fun onLost(network: Network) {
            updateConnectionStatus(false)
        }
    }

    fun setupNetworkMonitor(context: Context) {
        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Проверка при запуске
        checkInitialConnection(context)

        // Регистрация callback для отслеживания изменений
        connectivityManager?.registerDefaultNetworkCallback(networkCallback)
    }

    private fun checkInitialConnection(context: Context) {
        viewModelScope.launch {
            _internetStatus.value = isInternetAvailable(context)
            if (_internetStatus.value) {
                refreshData(context)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        connectivityManager?.unregisterNetworkCallback(networkCallback)
    }

    private fun updateConnectionStatus(isConnected: Boolean) {
        viewModelScope.launch {
            _internetStatus.value = isConnected
        }
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    fun refreshData(context: Context) {
        viewModelScope.launch {
            try {
                _dataLoadingStatus.value = true
                DataManager.refreshGroups(context)
                DataManager.refreshTeachers(context)
            } catch (e: Exception) {
            } finally {
                _dataLoadingStatus.value = false
            }
        }
    }
}