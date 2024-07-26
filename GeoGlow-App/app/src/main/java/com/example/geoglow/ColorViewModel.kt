package com.example.geoglow

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class ColorViewModel(application: Application): AndroidViewModel(application) {

    data class ColorState(
        val imageBitmap: ImageBitmap? = null,
        val androidPalette: Palette? = null,
        val colorList: List<Array<Int>>? = null
    )

    private val _colorState = MutableStateFlow(ColorState())
    val colorState = _colorState.asStateFlow()

    fun setColorState(uri: Uri, rotate: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            getApplication<Application>().applicationContext.contentResolver.openInputStream(uri)?.use { stream ->
                val bitmap: Bitmap = BitmapFactory.decodeStream(stream)
                val rotatedImg = rotateImage(bitmap)
                val palette = Palette.from(bitmap).generate()

                _colorState.update { currentState ->
                    currentState.copy(
                        imageBitmap = if (rotate) rotatedImg.asImageBitmap() else bitmap.asImageBitmap(),
                        androidPalette = palette
                    )
                }
                Log.i("ViewModel", "colorState updated with bitmap: $bitmap")
            }
        }
    }

    fun updateColorList(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            getApplication<Application>().applicationContext.contentResolver.openInputStream(uri)?.use { stream ->
                val bitmap: Bitmap = BitmapFactory.decodeStream(stream)
                val resizedBitmap = resizeBitmap(bitmap, 400, 400)
                val colorList = extractColorsColorThief(resizedBitmap)

                _colorState.update { currentState ->
                    currentState.copy(
                        colorList = colorList
                    )
                }
                Log.i("ViewModel", "colorState updated with colorList: $colorList")
            }
        }
    }

    fun resetColorState() {
        viewModelScope.launch(Dispatchers.IO) {
            _colorState.update { currentState ->
                currentState.copy(
                    imageBitmap = null,
                    androidPalette = null,
                    colorList = null
                )
            }
            Log.i("ViewModel", "colorState reset")
        }
    }
}