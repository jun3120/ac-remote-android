package com.jun3120.acremote.data.usage

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class UsageRecord(
    val deviceCodePath: String,
    val action: String,
    val value: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class UsageStats(
    val favoriteTemp: Int = 26,
    val favoriteMode: String = "制冷",
    val totalActions: Int = 0,
    val totalRuntimeMinutes: Long = 0,
    val lastUsed: Long = 0,
    val tempHeat: String = "24",
    val tempCool: String = "26",
    val modes: Map<String, Int> = emptyMap(),
    val temps: Map<Int, Int> = emptyMap()
)

object UsageTracker {
    private const val PREF_NAME = "ac_usage"
    private const val KEY_RECORDS = "usage_records"
    private const val MAX_RECORDS = 500
    private val gson = Gson()

    fun log(context: Context, devicePath: String, action: String, value: String) {
        val records = getRecords(context).toMutableList()
        records.add(UsageRecord(devicePath, action, value))
        val trimmed = if (records.size > MAX_RECORDS) records.takeLast(MAX_RECORDS) else records
        save(context, trimmed)
    }

    fun getRecords(context: Context): List<UsageRecord> {
        val json = getPrefs(context).getString(KEY_RECORDS, "[]") ?: "[]"
        val type = object : TypeToken<List<UsageRecord>>() {}.type
        return try { gson.fromJson(json, type) ?: emptyList() } catch (e: Exception) { emptyList() }
    }

    fun getStats(context: Context): UsageStats {
        val records = getRecords(context)
        if (records.isEmpty()) return UsageStats()

        // Favorite mode
        val modeCounts = records.filter { it.action == "mode" }
            .groupBy { it.value }.mapValues { it.value.size }
        val favoriteMode = modeCounts.maxByOrNull { it.value }?.key ?: "制冷"

        // Favorite temperature
        val allTemps = records.filter { it.action == "temp_up" || it.action == "temp_down" }
            .mapNotNull { it.value.toIntOrNull() }
        val tempCounts = allTemps.groupBy { it }.mapValues { it.value.size }
        val favoriteTemp = tempCounts.maxByOrNull { it.value }?.key ?: 26

        // Heat/cool temp preferences
        val heatTemps = records.filter { it.value.toIntOrNull() != null && it.value.toIntOrNull()!! > 24 }
            .mapNotNull { it.value.toIntOrNull() }
        val coolTemps = records.filter { it.value.toIntOrNull() != null && it.value.toIntOrNull()!! <= 24 }
            .mapNotNull { it.value.toIntOrNull() }
        val tempHeat = if (heatTemps.isNotEmpty()) heatTemps.groupBy { it }.maxBy { it.value.size }.key.toString() else "24"
        val tempCool = if (coolTemps.isNotEmpty()) coolTemps.groupBy { it }.maxBy { it.value.size }.key.toString() else "26"

        // Runtime
        var totalMs = 0L
        var lastOnTime = 0L
        for (r in records) {
            if (r.action == "power" && r.value == "on" && lastOnTime == 0L) {
                lastOnTime = r.timestamp
            } else if (r.action == "power" && r.value == "off" && lastOnTime > 0) {
                totalMs += (r.timestamp - lastOnTime).coerceAtMost(8 * 3600 * 1000)
                lastOnTime = 0
            }
        }
        if (lastOnTime > 0) {
            totalMs += (System.currentTimeMillis() - lastOnTime).coerceAtMost(8 * 3600 * 1000)
        }

        return UsageStats(
            favoriteTemp = favoriteTemp,
            favoriteMode = favoriteMode,
            totalActions = records.size,
            totalRuntimeMinutes = totalMs / 60000,
            lastUsed = records.lastOrNull()?.timestamp ?: 0,
            tempHeat = tempHeat,
            tempCool = tempCool,
            modes = modeCounts,
            temps = tempCounts
        )
    }

    private fun save(context: Context, records: List<UsageRecord>) {
        getPrefs(context).edit().putString(KEY_RECORDS, gson.toJson(records)).apply()
    }

    private fun getPrefs(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
}
