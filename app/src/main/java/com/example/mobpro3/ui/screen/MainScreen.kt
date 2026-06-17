package com.example.mobpro3.ui.screen

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.mobpro3.BuildConfig
import com.example.mobpro3.R
import com.example.mobpro3.model.GoogleLoginRequest
import com.example.mobpro3.model.Laporan
import com.example.mobpro3.model.User
import com.example.mobpro3.network.ApiStatus
import com.example.mobpro3.network.RetrofitClient
import com.example.mobpro3.network.UserDataStore
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {

    val context = LocalContext.current
    val dataStore = UserDataStore(context)
    val user by dataStore.userFlow.collectAsState(User())

    val mainViewModel: MainViewModel = viewModel()

    var showProfileDialog by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }

    var showEditDialog by remember { mutableStateOf(false) }
    var selectedReport by remember { mutableStateOf<Laporan?>(null) }

    Scaffold(

        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
                actions = {

                    IconButton(onClick = {
                        if (user.email.isEmpty()) {
                            CoroutineScope(Dispatchers.IO).launch {
                                signIn(context, dataStore)
                            }
                        } else {
                            showProfileDialog = true
                        }
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.outline_account_circle_24),
                            contentDescription = "Profile"
                        )
                    }
                }
            )
        },

        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (user.token.isEmpty()) {
                        Toast.makeText(
                            context,
                            "Harus login",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        showCreateDialog = true
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah")
            }
        }

    ) { innerPadding ->

        ScreenContent(
            modifier = Modifier.padding(innerPadding),
            token = user.token,
            currentUserId = user.id,
            viewModel = mainViewModel,

            onEdit = {
                selectedReport = it
                showEditDialog = true
            }
        )

        if (showProfileDialog) {
            ProfilDialog(
                user = user,
                onDismissRequest = { showProfileDialog = false },
                onConfirmation = {
                    CoroutineScope(Dispatchers.IO).launch {
                        signOut(context, dataStore)
                    }
                    showProfileDialog = false
                }
            )
        }

        if (showCreateDialog) {

            CreateReportDialog(
                token = user.token,
                onDismiss = { showCreateDialog = false },
                onSuccess = {
                    mainViewModel.retrieveData()
                }
            )
        }

        if (showEditDialog && selectedReport != null) {

            CreateReportDialog(
                token = user.token,
                report = selectedReport,
                onDismiss = {
                    showEditDialog = false
                    selectedReport = null
                },
                onSuccess = {
                    mainViewModel.retrieveData()
                }
            )
        }
    }
}

@Composable
fun ScreenContent(
    modifier: Modifier = Modifier,
    token: String,
    currentUserId: String,
    viewModel: MainViewModel,
    onEdit: (Laporan) -> Unit
) {

    val data by viewModel.data.collectAsState()
    val status by viewModel.status.collectAsState()

    when (status) {

        ApiStatus.LOADING -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        ApiStatus.SUCCESS -> {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                items(data.data) { item ->

                    ReportItem(
                        item = item,
                        canDelete = item.user_id == currentUserId,
                        onDelete = {
                            viewModel.deleteReport(token, item.id)
                        },
                        onEdit = {
                            onEdit(item)
                        }
                    )
                }
            }
        }

        ApiStatus.FAILED -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Error")
                Button(onClick = { viewModel.retrieveData() }) {
                    Text("Retry")
                }
            }
        }
    }
}

@Composable
fun ReportItem(
    item: Laporan,
    canDelete: Boolean,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {

    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 140.dp)
        ) {

            AsyncImage(
                model = item.foto,
                contentDescription = null,
                modifier = Modifier
                    .width(120.dp)
                    .height(140.dp),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(12.dp)
            ) {

                Column {

                    Text(
                        text = item.nama_barang,
                        fontWeight = FontWeight.Bold
                    )

                    Text("Status: ${item.status}")
                    Text("Lokasi: ${item.lokasi}")
                    Text("User: ${item.email}")

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = item.deskripsi ?: "Tidak ada deskripsi",
                        maxLines = 4
                    )
                }

                if (canDelete) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                    ) {

                        IconButton(onClick = onEdit) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit"
                            )
                        }

                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color.Red
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus") },
            text = { Text("Yakin hapus laporan ini?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

private suspend fun signIn(
    context: Context,
    dataStore: UserDataStore
) {

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(BuildConfig.API_KEY)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    try {

        val credentialManager = CredentialManager.create(context)
        val result = credentialManager.getCredential(context, request)

        handleSignIn(result, dataStore)

    } catch (e: Exception) {
        Log.e("SIGN-IN", e.message ?: "error")
    }
}

private suspend fun sendToBackend(
    idToken: String,
    dataStore: UserDataStore
) {
    try {

        val api = RetrofitClient.apiService

        val response = api.googleLogin(
            GoogleLoginRequest(idToken)
        )

        dataStore.saveToken(response.token)

        dataStore.saveData(
            User(
                id = response.user.id,
                name = response.user.name,
                email = response.user.email,
                photoUrl = response.user.photo,
                token = response.token
            )
        )

    } catch (e: Exception) {
        Log.e("API", e.message ?: "error")
    }
}

private suspend fun handleSignIn(
    result: GetCredentialResponse,
    dataStore: UserDataStore
) {

    val credential = result.credential

    if (credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
    ) {

        val googleCred =
            GoogleIdTokenCredential.createFrom(credential.data)

        val idToken = googleCred.idToken

        sendToBackend(idToken, dataStore)
    }
}

private suspend fun signOut(
    context: Context,
    dataStore: UserDataStore
) {

    val credentialManager = CredentialManager.create(context)

    credentialManager.clearCredentialState(
        ClearCredentialStateRequest()
    )

    dataStore.clear()
}
