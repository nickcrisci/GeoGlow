package com.example.geoglow

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


object SharedPreferencesHelper {
    private const val PREFS_NAME = "app_preferences"
    private const val KEY_USER = "user"
    private const val KEY_FRIEND_LIST = "friends_list"
    private val gson = Gson()

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun setUser(context: Context, user: Friend) {
        val userJson = gson.toJson(user)
        getPreferences(context).edit().putString(KEY_USER, userJson).apply()
    }

    fun getUser(context: Context): Friend? {
        val userJson = getPreferences(context).getString(KEY_USER, null)
        return userJson?.let {
            gson.fromJson(it, Friend::class.java)
        }
    }

    fun setFriendList(context: Context, friends: List<Friend>) {
        val friendsJson = gson.toJson(friends)
        getPreferences(context).edit().putString(KEY_FRIEND_LIST, friendsJson).apply()
    }

    fun getFriendList(context: Context): List<Friend> {
        val friendsJson = getPreferences(context).getString(KEY_FRIEND_LIST, null)
        return friendsJson?.let {
            val type = object : TypeToken<List<Friend>>() {}.type
            gson.fromJson(it, type)
        } ?: emptyList()
    }

    fun resetPreferences(context: Context) {
        getPreferences(context).edit().clear().apply()
    }
}