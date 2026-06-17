package com.example.mobpro3.ui.screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobpro3.network.ApiStatus
import com.example.mobpro3.network.RetrofitClient
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File


class ReportViewModel : ViewModel() {

    var status by mutableStateOf(ApiStatus.SUCCESS)
        private set

    var message by mutableStateOf("")
        private set


    fun createReport(
        token: String,
        nama: String,
        statusValue: String,
        lokasi: String,
        deskripsi: String,
        tanggal: String,
        file: File?
    ) {
        viewModelScope.launch {

            status = ApiStatus.LOADING

            try {

                val api = RetrofitClient.apiService

                val namaBody = nama.toRequestBody("text/plain".toMediaType())
                val statusBody = statusValue.toRequestBody("text/plain".toMediaType())
                val lokasiBody = lokasi.toRequestBody("text/plain".toMediaType())
                val deskripsiBody = deskripsi.toRequestBody("text/plain".toMediaType())
                val tanggalBody = tanggal.toRequestBody("text/plain".toMediaType())

                val fotoPart = file?.let {
                    val req = it.asRequestBody("image/*".toMediaType())
                    MultipartBody.Part.createFormData("foto", it.name, req)
                }

                val response = api.createReport(
                    token = "Bearer $token",
                    namaBarang = namaBody,
                    status = statusBody,
                    lokasi = lokasiBody,
                    deskripsi = deskripsiBody,
                    tanggal = tanggalBody,
                    foto = fotoPart
                )

                if (response.success) {
                    status = ApiStatus.SUCCESS
                    message = response.message
                } else {
                    status = ApiStatus.FAILED
                    message = response.message
                }

            } catch (e: Exception) {
                status = ApiStatus.FAILED
                message = e.message ?: "Unknown error"
            }
        }
    }

    fun resetState() {
        status = ApiStatus.SUCCESS
        message = ""
    }

    fun updateReport(
        token: String,
        id: String,
        nama: String,
        statusValue: String,
        lokasi: String,
        deskripsi: String,
        tanggal: String,
        file: File?
    ) {
        viewModelScope.launch {

            status = ApiStatus.LOADING

            try {

                val api = RetrofitClient.apiService

                val namaBody = nama.toRequestBody("text/plain".toMediaType())
                val statusBody = statusValue.toRequestBody("text/plain".toMediaType())
                val lokasiBody = lokasi.toRequestBody("text/plain".toMediaType())
                val deskripsiBody = deskripsi.toRequestBody("text/plain".toMediaType())
                val tanggalBody = tanggal.toRequestBody("text/plain".toMediaType())

                val fotoPart = file?.let {
                    val req = it.asRequestBody("image/*".toMediaType())
                    MultipartBody.Part.createFormData("foto", it.name, req)
                }

                val response = api.updateReport(
                    token = "Bearer $token",
                    id = id,
                    namaBarang = namaBody,
                    status = statusBody,
                    lokasi = lokasiBody,
                    deskripsi = deskripsiBody,
                    tanggal = tanggalBody,
                    foto = fotoPart
                )

                if (response.success) {
                    status = ApiStatus.SUCCESS
                    message = response.message
                } else {
                    status = ApiStatus.FAILED
                    message = response.message
                }

            } catch (e: Exception) {
                status = ApiStatus.FAILED
                message = e.message ?: "Error"
            }
        }
    }

}