package com.abhishek.hexmerge

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class SaveManager(context: Context) {
    private val prefs = context.getSharedPreferences("hex_merge_save", Context.MODE_PRIVATE)

    fun save(engine: GameEngine) {
        val s = engine.snapshot()
        val obj = JSONObject().apply {
            put("level", s.level); put("score", s.score); put("bestScore", s.bestScore)
            put("coins", s.coins); put("unlockedLevel", s.unlockedLevel)
            put("tiles", JSONObject().apply { s.tiles.forEach { (k,v) -> put(k,v) } })
            put("queue", JSONArray(s.queue))
            put("stars", JSONObject().apply { s.stars.forEach { (k,v) -> put(k.toString(),v) } })
        }
        prefs.edit().putString("snapshot", obj.toString()).apply()
    }

    fun restore(engine: GameEngine): Boolean {
        return runCatching {
            val raw = prefs.getString("snapshot", null) ?: return false
            val obj = JSONObject(raw)
            val tileObj = obj.optJSONObject("tiles") ?: JSONObject()
            val tileMap = mutableMapOf<String, Int>()
            tileObj.keys().forEach { key -> tileMap[key] = tileObj.optInt(key) }
            val q = obj.optJSONArray("queue") ?: JSONArray()
            val queue = List(q.length()) { q.optInt(it) }
            val starsObj = obj.optJSONObject("stars") ?: JSONObject()
            val stars = mutableMapOf<Int, Int>()
            starsObj.keys().forEach { key -> stars[key.toIntOrNull() ?: 1] = starsObj.optInt(key) }
            engine.restore(GameSnapshot(
                obj.optInt("level",1), obj.optLong("score"), obj.optLong("bestScore"),
                obj.optInt("coins"), tileMap, queue, obj.optInt("unlockedLevel",1), stars
            ))
            true
        }.getOrDefault(false)
    }

    fun clear() { prefs.edit().clear().apply() }
}
