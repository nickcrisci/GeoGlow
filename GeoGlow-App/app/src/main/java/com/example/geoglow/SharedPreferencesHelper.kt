package com.example.geoglow

import android.content.Context
import android.content.SharedPreferences


object SharedPreferencesHelper {
    private const val PREFS_NAME = "GeoGlowPrefs"
    private const val UNIQUE_ID_KEY = "unique_id"
    private const val FRIEND_LIST_KEY = "friend_list"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getUniqueID(context: Context): String? {
        return getPreferences(context).getString(UNIQUE_ID_KEY, null)
    }

    fun setUniqueID(context: Context, uniqueID: String) {
        val editor: SharedPreferences.Editor = getPreferences(context).edit()
        editor.putString(UNIQUE_ID_KEY, uniqueID)
        editor.apply()
    }

    fun getFriendList(context: Context): List<Friend> {
        val jsonString = getPreferences(context).getString(FRIEND_LIST_KEY, null) ?: return emptyList()
        return jsonStringToList(jsonString)
    }

    fun setFriendList(context: Context, friendList: List<Friend>) {
        val jsonString = listToJsonString(friendList)
        val editor: SharedPreferences.Editor = getPreferences(context).edit()
        editor.putString(FRIEND_LIST_KEY, jsonString)
        editor.apply()
    }
}