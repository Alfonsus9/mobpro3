package com.example.mobpro3.ui.screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.util.copy
import com.example.mobpro3.model.Laporan
import com.example.mobpro3.network.ApiStatus
import com.example.mobpro3.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class Mainstate(
    val data: List<Laporan> = emptyList()
)
class MainViewModel : ViewModel() {

    private val _data = MutableStateFlow(Mainstate())
    val data = _data.asStateFlow()

    private val _status = MutableStateFlow(ApiStatus.LOADING)
    val status: StateFlow<ApiStatus> = _status

    init {
        retrieveData()
    }

    fun retrieveData() {
        viewModelScope.launch {
            try {
                _status.value = ApiStatus.LOADING

                val response = RetrofitClient.apiService.getAllReports()

                if(response.success) {
                   _data.update {
                       it.copy(
                           response.data
                       )
                   }
                }

                _status.value = ApiStatus.SUCCESS

            } catch (e: Exception) {
                _status.value = ApiStatus.FAILED
                Log.e("REPORT", e.message ?: "error")
            }
        }
    }

    fun deleteReport(token: String, id: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.deleteReport(
                    token = "Bearer $token",
                    id = id
                )

                if (response.message == "Berhasil dihapus") {

                    retrieveData()
                }
            } catch (e: Exception) {
                Log.e("DELETE", e.message ?: "error")
            }
        }
    }
}