package com.hlls.agenda

import android.content.Context
import org.json.JSONArray

object Store {
    private const val PREFS = "agenda"
    private const val KEY = "tasks"

    fun load(ctx: Context): List<Task> = try {
        val arr = JSONArray(ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, "[]"))
        (0 until arr.length()).map { Task.fromJson(arr.getJSONObject(it)) }
    } catch (e: Exception) {
        emptyList()
    }

    /** Salva e já reagenda os alertas. */
    fun save(ctx: Context, list: List<Task>) {
        val arr = JSONArray()
        list.forEach { arr.put(it.toJson()) }
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, arr.toString()).apply()
        Scheduler.reschedule(ctx)
    }
}
