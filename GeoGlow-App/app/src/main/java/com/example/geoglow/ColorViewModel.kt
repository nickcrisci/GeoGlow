package com.example.geoglow

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
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
import java.io.IOException


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

    private fun rotateImage(bitmap: Bitmap, uri: Uri): Bitmap {
        try {
            val exifInterface = ExifInterface(uri.path ?: "")
            val orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL) //ExifInterface.ORIENTATION_UNDEFINED
            val matrix = Matrix()

            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(270f)
            }

            val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            Log.i("Viewmodel", "rotatedBitmap: $rotatedBitmap")
            return rotatedBitmap
        } catch (e: IOException) {
            Log.e("ViewModel","Error: ${e.message}")
            return bitmap
        }
    }
}