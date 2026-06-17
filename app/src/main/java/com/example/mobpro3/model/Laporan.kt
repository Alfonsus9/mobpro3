package com.example.mobpro3.model

data class Laporan (
    val id: String,
    val user_id: String,
    val nama_barang: String,
    val status: String,
    val lokasi: String,
    val deskripsi: String?,
    val foto: String?,
    val tanggal_kejadian: String,

    val created_at: String?,
    val updated_at: String?,

    val name: String,
    val email: String?
)