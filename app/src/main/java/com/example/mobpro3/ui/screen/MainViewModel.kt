package com.example.mobpro3.ui.screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobpro3.model.Laporan
import com.example.mobpro3.network.ApiStatus
import com.example.mobpro3.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class MainViewModel : ViewModel() {

    private val _data = MutableStateFlow<List<Laporan>>(emptyList())
    val data: StateFlow<List<Laporan>> = _data

    private val _status = MutableStateFlow(ApiStatus.LOADING)
    val status: StateFlow<ApiStatus> = _status

    fun retrieveData() {
        viewModelScope.launch {
            try {
                _status.value = ApiStatus.LOADING

                val response = RetrofitClient.apiService.getAllReports()
                _data.value = response.data

                _status.value = ApiStatus.SUCCESS

            } catch (e: Exception) {
                _status.value = ApiStatus.FAILED
                Log.e("REPORT", e.message ?: "error")
            }
        }
    }

    fun deleteReport(
        token: String,
        id: String
    ) {
        viewModelScope.launch {

            try {

                RetrofitClient.apiService.deleteReport(
                    token = "Bearer $token",
                    id = id
                )

                _data.value = _data.value.filter {
                    it.id != id
                }

            } catch (e: Exception) {
                Log.e("DELETE", e.message ?: "error")
            }
        }
    }
}