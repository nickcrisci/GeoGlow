package com.example.geoglow

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import androidx.palette.graphics.Palette
import com.example.geoglow.color_palette.PaletteGenerator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream


@SuppressLint("SimpleDateFormat")
fun Context.createImageFile(): File {
    //val timeStamp = SimpleDateFormat("yyyy_MM_dd HH:mm:ss").format(Date())
    val imageFileName = "JPEG_" //+ timeStamp + "_"
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

fun jsonStringToFriendList(jsonString: String): List<Friend> {
    val gson = Gson()
    val friendType = object : TypeToken<List<Friend>>() {}.type
    val friends: List<Friend> = gson.fromJson(jsonString, friendType)

    return friends.map { friend ->
        Friend(
            name = friend.name,
            id = friend.id,
            devices = friend.devices.toMutableList()
        )
    }
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

fun extractColorsColorThief(bitmap: Bitmap, colorCount: Int = 10): List<Array<Int>> {
    val colorPalette = PaletteGenerator.compute(bitmap, colorCount)
    return colorPalette?.map { color ->
        arrayOf(color[0], color[1], color[2])
    } ?: emptyList()
}

fun resizeBitmap(bitmap: Bitmap, factor: Int = 4): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    val scaleWidth = width / factor
    val scaleHeight = height / factor
    return Bitmap.createScaledBitmap(bitmap, scaleWidth, scaleHeight, true)
}

//TODO: rotate image properly
fun rotateImage(bitmap: Bitmap): Bitmap {
    val rotationDegrees =  90
    val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}
