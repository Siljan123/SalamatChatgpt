package edu.ws2024.aXX.am.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object DataStoremanager {
    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    private val SKIER_COLOR_KEY = stringPreferencesKey("skier_jacket_color")

    suspend fun saveSkierColor(context: Context, colorHex: String) {
        context.dataStore.edit { preferences ->
            preferences[SKIER_COLOR_KEY] = colorHex
        }
    }

    fun getSkierColor(context: Context): Flow<String?> {
        return context.dataStore.data
            .map { preferences ->
                preferences[SKIER_COLOR_KEY]
            }
    }
}