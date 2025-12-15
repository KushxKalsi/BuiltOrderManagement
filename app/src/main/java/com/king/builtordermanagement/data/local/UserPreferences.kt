package com.king.builtordermanagement.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.king.builtordermanagement.data.models.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {
    
    private val gson = Gson()
    
    companion object {
        private val USER_KEY = stringPreferencesKey("user_data")
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    }
    
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN] ?: false
    }
    
    val currentUser: Flow<User?> = context.dataStore.data.map { preferences ->
        val userJson = preferences[USER_KEY]
        if (userJson != null) {
            try {
                gson.fromJson(userJson, User::class.java)
            } catch (e: Exception) {
                null
            }
        } else null
    }
    
    suspend fun saveUser(user: User) {
        context.dataStore.edit { preferences ->
            preferences[USER_KEY] = gson.toJson(user)
            preferences[IS_LOGGED_IN] = true
        }
    }
    
    suspend fun clearUser() {
        context.dataStore.edit { preferences ->
            preferences.remove(USER_KEY)
            preferences[IS_LOGGED_IN] = false
        }
    }
}
