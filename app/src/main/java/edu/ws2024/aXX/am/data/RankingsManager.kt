package edu.ws2024.aXX.am.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import edu.ws2024.aXX.am.data.DataStoremanager.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

object RankingsManager {
    private val RANKINGS_KEY = stringPreferencesKey("rankings_list")
    private val gson = Gson()

    suspend fun saveRanking(context: Context, newRecord: GameRecord) {
        val currentRankings = getRankingsList(context).toMutableList()
        currentRankings.add(newRecord)

        // Sort: duration desc → coins desc → timestamp desc
        val sortedRankings = currentRankings.sortedWith(
            compareByDescending<GameRecord> { it.duration }
                .thenByDescending { it.coins }
                .thenByDescending { it.timestamp }
        )

        context.dataStore.edit { preferences ->
            preferences[RANKINGS_KEY] = gson.toJson(sortedRankings)
        }
    }

    suspend fun getRankingsList(context: Context): List<GameRecord> {
        val jsonString = context.dataStore.data
            .map { preferences ->
                preferences[RANKINGS_KEY] ?: "[]"
            }.first()

        val type = object : TypeToken<List<GameRecord>>() {}.type
        return gson.fromJson(jsonString, type) ?: emptyList()
    }
}