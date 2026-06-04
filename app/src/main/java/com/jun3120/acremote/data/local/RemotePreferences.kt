package com.jun3120.acremote.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * 已保存遥控器的本地持久化存储（SharedPreferences）。
 */
object RemotePreferences {

    private const val PREF_NAME = "ac_remotes"
    private const val KEY_REMOTES = "saved_remotes"
    private val gson = Gson()

    fun getSavedRemotes(context: Context): List<SavedRemote> {
        val json = getPrefs(context).getString(KEY_REMOTES, "[]") ?: "[]"
        val type = object : TypeToken<List<SavedRemote>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addRemote(context: Context, remote: SavedRemote) {
        val remotes = getSavedRemotes(context).toMutableList()
        // 避免重复：同路径覆盖
        remotes.removeAll { it.codePath == remote.codePath }
        remotes.add(0, remote)
        save(context, remotes)
    }

    fun removeRemote(context: Context, codePath: String) {
        val remotes = getSavedRemotes(context).filter { it.codePath != codePath }
        save(context, remotes)
    }

    private fun save(context: Context, remotes: List<SavedRemote>) {
        getPrefs(context).edit()
            .putString(KEY_REMOTES, gson.toJson(remotes))
            .apply()
    }

    private fun getPrefs(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
}

data class SavedRemote(
    val codePath: String,
    val categoryId: Int,
    val brandName: String,
    val savedAt: Long = System.currentTimeMillis()
)
