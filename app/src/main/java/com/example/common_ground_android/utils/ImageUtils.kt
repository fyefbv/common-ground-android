package com.example.common_ground_android.utils

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.InputStream

object ImageUtils {

    fun getBitmapFromUri(contentResolver: ContentResolver, uri: Uri): Bitmap {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        return bitmap
    }

    fun compressBitmapToByteArray(bitmap: Bitmap, quality: Int = 80): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        return stream.toByteArray()
    }
}