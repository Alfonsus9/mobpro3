package com.example.mobpro3.network

import com.example.mobpro3.model.CreateReportResponse
import com.example.mobpro3.model.DeleteReportResponse
import com.example.mobpro3.model.GoogleLoginRequest
import com.example.mobpro3.model.GoogleLoginResponse
import com.example.mobpro3.model.LaporanResponse
import com.example.mobpro3.model.UpdateReportResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {

    @POST("auth/google")
    suspend fun googleLogin(
        @Body request: GoogleLoginRequest
    ): GoogleLoginResponse

    @GET("/reports")
    suspend fun getAllReports(): LaporanResponse

    @Multipart
    @POST("reports")
    suspend fun createReport(
        @Header("Authorization") token: String,
        @Part("nama_barang") namaBarang: RequestBody,
        @Part("status") status: RequestBody,
        @Part("lokasi") lokasi: RequestBody,
        @Part("deskripsi") deskripsi: RequestBody,
        @Part("tanggal_kejadian") tanggal: RequestBody,
        @Part foto: MultipartBody.Part?
    ): CreateReportResponse

    @DELETE("reports/{id}")
    suspend fun deleteReport(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): DeleteReportResponse

    @Multipart
    @PUT("reports/{id}")
    suspend fun updateReport(
        @Path("id") id: String,
        @Header("Authorization") token: String,
        @Part("nama_barang") namaBarang: RequestBody,
        @Part("status") status: RequestBody,
        @Part("lokasi") lokasi: RequestBody,
        @Part("deskripsi") deskripsi: RequestBody,
        @Part("tanggal_kejadian") tanggal: RequestBody,
        @Part foto: MultipartBody.Part? = null
    ): UpdateReportResponse

}