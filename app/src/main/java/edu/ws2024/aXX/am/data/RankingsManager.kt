package edu.ws2024.aXX.am.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object RankingsManager {
    private const val PREFS_NAME = "rankings_prefs"
    private const val KEY_RANKINGS = "rankings_list"

    private val gson = Gson()

    fun saveRanking(context: Context, record: GameRecord) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existing = loadRankings(context).toMutableList()
        existing.add(record)

        val json = gson.toJson(existing)
        prefs.edit().putString(KEY_RANKINGS, json).apply()
    }

    fun loadRankings(context: Context): List<GameRecord> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_RANKINGS, null) ?: return emptyList()

        val type = object : TypeToken<List<GameRecord>>() {}.type
        return gson.fromJson(json, type)
    }
}