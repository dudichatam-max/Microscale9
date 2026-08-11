package com.microtonal.scale9lab.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

class ScaleRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("microtonal_scale_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getSavedScale(): Scale {
        val json = prefs.getString("current_scale", null) ?: return Scale.getDefault9NoteScale()
        return try {
            gson.fromJson(json, Scale::class.java) ?: Scale.getDefault9NoteScale()
        } catch (e: Exception) {
            Scale.getDefault9NoteScale()
        }
    }

    fun saveScale(scale: Scale) {
        val json = gson.toJson(scale)
        prefs.edit().putString("current_scale", json).apply()
    }

    fun resetToDefault(): Scale {
        val defaultScale = Scale.getDefault9NoteScale()
        saveScale(defaultScale)
        return defaultScale
    }
}
