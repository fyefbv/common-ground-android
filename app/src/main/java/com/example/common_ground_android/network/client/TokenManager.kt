package com.example.common_ground_android.network.client

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.common_ground_android.network.config.ApiConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "tokens")

class TokenManager(private val context: Context) {
    private val dataStore = context.dataStore

    companion object {
        private val ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val PROFILE_ID = stringPreferencesKey("profile_id")
        private val TOKENS_EXPIRE_AT = stringPreferencesKey("tokens_expire_at")
        private val IS_AUTHENTICATED = booleanPreferencesKey("is_authenticated")
    }

    suspend fun saveTokens(accessToken: String, refreshToken: String, profileId: String? = null) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = accessToken
            preferences[REFRESH_TOKEN] = refreshToken
            preferences[IS_AUTHENTICATED] = true

            profileId?.let {
                preferences[PROFILE_ID] = it
            }

            val expireAt = System.currentTimeMillis() + 15 * 60 * 1000 - ApiConfig.TOKEN_EXPIRY_BUFFER
            preferences[TOKENS_EXPIRE_AT] = expireAt.toString()
        }
    }

    suspend fun updateAccessToken(accessToken: String) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = accessToken

            val expireAt = System.currentTimeMillis() + 15 * 60 * 1000 - ApiConfig.TOKEN_EXPIRY_BUFFER
            preferences[TOKENS_EXPIRE_AT] = expireAt.toString()
        }
    }

    suspend fun saveProfileId(profileId: String) {
        dataStore.edit { preferences ->
            preferences[PROFILE_ID] = profileId
        }
    }

    suspend fun clearTokens() {
        dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN)
            preferences.remove(REFRESH_TOKEN)
            preferences.remove(PROFILE_ID)
            preferences.remove(TOKENS_EXPIRE_AT)
            preferences[IS_AUTHENTICATED] = false
        }
    }

    val accessToken: Flow<String?> = dataStore.data
        .map { preferences -> preferences[ACCESS_TOKEN] }

    val refreshToken: Flow<String?> = dataStore.data
        .map { preferences -> preferences[REFRESH_TOKEN] }

    val profileId: Flow<String?> = dataStore.data
        .map { preferences -> preferences[PROFILE_ID] }

    val isAuthenticated: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[IS_AUTHENTICATED] ?: false }

    suspend fun getAccessTokenSync(): String? {
        return dataStore.data.map { it[ACCESS_TOKEN] }.firstOrNull()
    }

    suspend fun getRefreshTokenSync(): String? {
        return dataStore.data.map { it[REFRESH_TOKEN] }.firstOrNull()
    }

    suspend fun getProfileIdSync(): String? {
        return dataStore.data.map { it[PROFILE_ID] }.firstOrNull()
    }

    suspend fun isAccessTokenExpired(): Boolean {
        val expireAtStr = dataStore.data.map { it[TOKENS_EXPIRE_AT] }.firstOrNull()
        return if (expireAtStr != null) {
            val expireAt = expireAtStr.toLongOrNull() ?: return true
            System.currentTimeMillis() >= expireAt
        } else {
            true
        }
    }
}