package com.example.mobpro3.network

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.mobpro3.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_preference"
)

class UserDataStore(private val context: Context) {

    companion object {
        private val USER_ID = stringPreferencesKey("id")
        private val USER_NAME = stringPreferencesKey("name")
        private val USER_EMAIL = stringPreferencesKey("email")
        private val USER_PHOTO = stringPreferencesKey("photoUrl")
        private val USER_TOKEN = stringPreferencesKey("token")
    }

    val userFlow: Flow<User> = context.dataStore.data.map { preferences ->
        User(
            id = preferences[USER_ID] ?: "",
            name = preferences[USER_NAME] ?: "",
            email = preferences[USER_EMAIL] ?: "",
            photoUrl = preferences[USER_PHOTO] ?: "",
            token = preferences[USER_TOKEN] ?: ""
        )
    }

    val tokenFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_TOKEN]
    }

    suspend fun saveData(user: User) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID] = user.id
            preferences[USER_NAME] = user.name
            preferences[USER_EMAIL] = user.email
            preferences[USER_PHOTO] = user.photoUrl
        }
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_TOKEN] = token
        }
    }

    suspend fun clear() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}