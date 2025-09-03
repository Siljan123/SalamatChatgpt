package edu.ws2024.aXX.am.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object RankingsManager {
    private const val PREFS_NAME = "rankings_prefs"
    private const val KEY_RANKINGS = "rankings"

    fun saveRanking(context: Context, record: GameRecord) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()

        // Load existing
        val existingJson = prefs.getString(KEY_RANKINGS, null)
        val type = object : TypeToken<MutableList<GameRecord>>() {}.type
        val existingList: MutableList<GameRecord> =
            if (existingJson != null) gson.fromJson(existingJson, type) else mutableListOf()

        // Optional: remove old record if same player exists
        existingList.removeAll { it.playerName == record.playerName }

        // Add new record
        existingList.add(record)

        // Save back
        prefs.edit().putString(KEY_RANKINGS, gson.toJson(existingList)).apply()
    }

    fun loadRankings(context: Context): List<GameRecord> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_RANKINGS, null) ?: return emptyList()
        val type = object : TypeToken<List<GameRecord>>() {}.type
        return Gson().fromJson(json, type)
    }
}
