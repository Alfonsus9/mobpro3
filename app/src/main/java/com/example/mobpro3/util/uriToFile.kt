package com.example.mobpro3.util

import android.content.Context
import android.net.Uri
import java.io.File

fun uriToFile(context: Context, uri: Uri): File {
    val input = context.contentResolver.openInputStream(uri)!!
    val file = File.createTempFile("report_", ".jpg", context.cacheDir)
    file.outputStream().use { input.copyTo(it) }
    return file
}