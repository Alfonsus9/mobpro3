package com.example.mobpro3.ui.screen

import android.app.DatePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.mobpro3.R
import com.example.mobpro3.model.Laporan
import com.example.mobpro3.network.ApiStatus
import com.example.mobpro3.util.uriToFile
import java.io.File
import java.util.Calendar

@Composable
fun CreateReportDialog(
    token: String,
    viewModel: ReportViewModel = viewModel(),
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    report: Laporan? = null
) {

    val isEditMode = report != null

    val context = LocalContext.current

    var nama by remember(report) { mutableStateOf(report?.nama_barang ?: "") }
    var statusBarang by remember(report) { mutableStateOf(report?.status ?: "Hilang") }
    var lokasi by remember(report) { mutableStateOf(report?.lokasi ?: "") }
    var deskripsi by remember(report) { mutableStateOf(report?.deskripsi ?: "") }
    var tanggal by remember(report) { mutableStateOf(report?.tanggal_kejadian ?: "") }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageFile by remember { mutableStateOf<File?>(null) }

    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->

            tanggal = String.format(
                "%04d-%02d-%02d",
                year,
                month + 1,
                day
            )
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.maxDate = calendar.timeInMillis
    }

    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->

        uri?.let {

            imageUri = it
            imageFile = uriToFile(context, it)
        }
    }

    val isFormValid =
        nama.isNotBlank() &&
                statusBarang.isNotBlank() &&
                lokasi.isNotBlank() &&
                deskripsi.isNotBlank() &&
                tanggal.isNotBlank() &&
                imageFile != null

    LaunchedEffect(viewModel.status) {

        if (
            viewModel.status == ApiStatus.SUCCESS &&
            viewModel.message.isNotEmpty()
        ) {

            onSuccess()

            viewModel.resetState()

            onDismiss()
        }
    }

    AlertDialog(

        onDismissRequest = {

            if (viewModel.status != ApiStatus.LOADING) {
                onDismiss()
            }
        },

        title = {
            Text(if (isEditMode) "Edit Laporan" else "Tambah Laporan")
        },

        text = {

            Column {

                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Nama Barang") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = statusBarang,
                    onValueChange = { statusBarang = it },
                    label = { Text("Status") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = lokasi,
                    onValueChange = { lokasi = it },
                    label = { Text("Lokasi") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = deskripsi,
                    onValueChange = { deskripsi = it },
                    label = { Text("Deskripsi") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = tanggal,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tanggal Kejadian") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        datePickerDialog.show()
                    }
                ) {
                    Text("Pilih Tanggal")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        pickImage.launch("image/*")
                    }
                ) {
                    Text("Pilih Foto")
                }

                imageUri?.let {

                    Spacer(modifier = Modifier.height(8.dp))

                    Image(
                        painter = rememberAsyncImagePainter(it),
                        contentDescription = null,
                        modifier = Modifier.size(120.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))


                if (viewModel.status == ApiStatus.FAILED) {

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = viewModel.message,
                        color = Color.Red
                    )
                }

                if (viewModel.status == ApiStatus.SUCCESS &&
                    viewModel.message.isNotEmpty()
                ) {

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = viewModel.message,
                        color = Color.Green
                    )
                }
            }
        },

        confirmButton = {

            Button(

                enabled =
                    isFormValid &&
                            viewModel.status != ApiStatus.LOADING,

                onClick = {

                    if (isEditMode) {

                        viewModel.updateReport(
                            token = token,
                            id = report!!.id,
                            nama = nama,
                            statusValue = statusBarang,
                            lokasi = lokasi,
                            deskripsi = deskripsi,
                            tanggal = tanggal,
                            file = imageFile
                        )

                    } else {

                        viewModel.createReport(
                            token = token,
                            nama = nama,
                            statusValue = statusBarang,
                            lokasi = lokasi,
                            deskripsi = deskripsi,
                            tanggal = tanggal,
                            file = imageFile
                        )
                    }
                }
            ) {
                Text(
                    if (viewModel.status == ApiStatus.LOADING)
                        "Mengirim..."
                    else
                        "Kirim"
                )
            }
        },

        dismissButton = {

            TextButton(
                enabled = viewModel.status != ApiStatus.LOADING,
                onClick = onDismiss
            ) {
                Text("Batal")
            }
        }
    )
}