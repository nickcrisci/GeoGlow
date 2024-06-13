package com.example.geoglow

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract

private const val IMAGE_MIME_TYPE = "image/*"

class CustomGalleryContract: ActivityResultContract<Void?, Uri?>() {
    override fun createIntent(context: Context, input: Void?): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Intent(MediaStore.ACTION_PICK_IMAGES)
        } else {
            Intent(Intent.ACTION_GET_CONTENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
        }.apply { type = IMAGE_MIME_TYPE }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return intent?.takeIf { resultCode == RESULT_OK }?.data
    }
}