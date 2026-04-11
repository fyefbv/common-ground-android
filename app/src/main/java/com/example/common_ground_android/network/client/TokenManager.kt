package com.example.common_ground_android.network.client

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.content.edit

class TokenManager(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "secure_tokens",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val scope = CoroutineScope(Dispatchers.IO)

    private val _accessToken = MutableStateFlow<String?>(prefs.getString(KEY_ACCESS_TOKEN, null))
    val accessToken: StateFlow<String?> = _accessToken.asStateFlow()

    private val _refreshToken = MutableStateFlow<String?>(prefs.getString(KEY_REFRESH_TOKEN, null))
    val refreshToken: StateFlow<String?> = _refreshToken.asStateFlow()

    private val _profileId = MutableStateFlow<String?>(prefs.getString(KEY_PROFILE_ID, null))
    val profileId: StateFlow<String?> = _profileId.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(prefs.getBoolean(KEY_IS_AUTHENTICATED, false))
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_PROFILE_ID = "profile_id"
        private const val KEY_IS_AUTHENTICATED = "is_authenticated"
    }

    suspend fun saveTokens(accessToken: String, refreshToken: String, profileId: String? = null) = withContext(Dispatchers.IO) {
        prefs.edit {
            putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .putBoolean(KEY_IS_AUTHENTICATED, true)
        }

        if (profileId != null) {
            prefs.edit { putString(KEY_PROFILE_ID, profileId) }
        } else {
            prefs.edit(commit = true) { remove(KEY_PROFILE_ID) }
        }

        scope.launch {
            _accessToken.emit(accessToken)
            _refreshToken.emit(refreshToken)
            _isAuthenticated.emit(true)
            if (profileId != null) _profileId.emit(profileId) else _profileId.emit(null)
        }
    }

    suspend fun updateAccessToken(accessToken: String) = withContext(Dispatchers.IO) {
        prefs.edit {
            putString(KEY_ACCESS_TOKEN, accessToken)
        }

        scope.launch {
            _accessToken.emit(accessToken)
        }
    }

    suspend fun saveProfileId(profileId: String) = withContext(Dispatchers.IO) {
        prefs.edit { putString(KEY_PROFILE_ID, profileId) }
        scope.launch { _profileId.emit(profileId) }
    }

    suspend fun clearTokens() = withContext(Dispatchers.IO) {
        prefs.edit { clear() }
        scope.launch {
            _accessToken.emit(null)
            _refreshToken.emit(null)
            _profileId.emit(null)
            _isAuthenticated.emit(false)
        }
    }

    suspend fun clearProfileId() = withContext(Dispatchers.IO) {
        prefs.edit(commit = true) { remove(KEY_PROFILE_ID) }
        scope.launch {
            _profileId.emit(null)
        }
    }

    fun getAccessTokenSync(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)
    fun getRefreshTokenSync(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)
    fun getProfileIdSync(): String? = prefs.getString(KEY_PROFILE_ID, null)
}