package com.example.geoglow

import android.annotation.SuppressLint
import android.content.Context
import androidx.palette.graphics.Palette
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import org.json.JSONArray
import org.json.JSONObject


@SuppressLint("SimpleDateFormat")
fun Context.createImageFile(): File {
    val timeStamp = SimpleDateFormat("yyyy_MM_dd_HH:mm:ss").format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    val image = File.createTempFile(
        imageFileName, /* prefix */
        ".jpg", /* suffix */
        externalCacheDir /* directory */
    )
    return image
}

fun transformListToJson(name: String, list: List<Array<Int>>): JSONObject {
    val jsonArray = JSONArray()

    for (array in list) {
        val jsonArrayElement = JSONArray()
        for (value in array) {
            jsonArrayElement.put(value)
        }
        jsonArray.put(jsonArrayElement)
    }

    val jsonObject = JSONObject()
    jsonObject.put(name, jsonArray)
    return jsonObject
}

fun jsonStringToList(jsonString: String): List<Friend> {
    val friendList = mutableListOf<Friend>()
    val jsonArray = JSONArray(jsonString)

    for (i in 0 until jsonArray.length()) {
        val jsonObject = jsonArray.getJSONObject(i)
        val name = jsonObject.getString("name")
        val id = jsonObject.getString("id")
        friendList.add(Friend(name, id))
    }

    return friendList
}

fun listToJsonString(friendList: List<Friend>): String {
    val jsonArray = JSONArray()

    for (friend in friendList) {
        val jsonObject = JSONObject()
        jsonObject.put("name", friend.name)
        jsonObject.put("id", friend.id)
        jsonArray.put(jsonObject)
    }

    return jsonArray.toString()
}

fun paletteToRgbList(palette: Palette): List<Array<Int>> {
    val rgbList = mutableListOf<Array<Int>>()

    val vibrant = palette.vibrantSwatch?.rgb
    val lightVibrant = palette.lightVibrantSwatch?.rgb
    val darkVibrant = palette.darkVibrantSwatch?.rgb
    val muted = palette.mutedSwatch?.rgb
    val lightMuted = palette.lightMutedSwatch?.rgb
    val darkMuted = palette.darkMutedSwatch?.rgb

    fun convertToRgb(color: Int?): Array<Int>? {
        return color?.let {
            arrayOf(
                (it shr 16) and 0xFF, // Red
                (it shr 8) and 0xFF,  // Green
                it and 0xFF           // Blue
            )
        }
    }

    vibrant?.let { convertToRgb(it)?.let { rgbList.add(it) } }
    lightVibrant?.let { convertToRgb(it)?.let { rgbList.add(it) } }
    darkVibrant?.let { convertToRgb(it)?.let { rgbList.add(it) } }
    muted?.let { convertToRgb(it)?.let { rgbList.add(it) } }
    lightMuted?.let { convertToRgb(it)?.let { rgbList.add(it) } }
    darkMuted?.let { convertToRgb(it)?.let { rgbList.add(it) } }

    return rgbList
}
