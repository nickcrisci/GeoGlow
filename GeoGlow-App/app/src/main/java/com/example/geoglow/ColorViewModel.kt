package com.example.geoglow

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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
        val palette: Palette? = null
    )

    private val _colorState = MutableStateFlow(ColorState())
    val colorState = _colorState.asStateFlow()

    fun setColorState(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            getApplication<Application>().applicationContext.contentResolver.openInputStream(uri)?.use { stream ->
                val bitmap: Bitmap = BitmapFactory.decodeStream(stream)
                val palette = Palette.from(bitmap).generate()
                val rotatedImg = rotateImage(bitmap, uri)

                _colorState.update { currentState ->
                    currentState.copy(
                        imageBitmap = rotatedImg.asImageBitmap(),
                        palette = palette
                    )
                }
                Log.i("ViewModel", "colorState updated with bitmap: $bitmap")
            }
        }
    }
}