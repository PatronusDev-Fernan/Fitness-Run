package com.example.fitnessrun

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Base64
import android.widget.ImageView
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors

object AvatarPhoto {
    val executor = Executors.newSingleThreadExecutor()

    fun encode(context: Context, uri: Uri): String {
        require(uri.scheme == "content")
        val resolver = context.contentResolver
        require(resolver.getType(uri)?.startsWith("image/") == true)
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        require(bounds.outWidth > 0 && bounds.outHeight > 0)
        var sample = 1
        while (bounds.outWidth / sample > 512 || bounds.outHeight / sample > 512) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        val decoded = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
            ?: throw IllegalArgumentException("Invalid image")
        val bitmaps = mutableSetOf(decoded)
        try {
            val orientation = runCatching {
                resolver.openInputStream(uri)?.use { ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, 1) }
            }.getOrNull() ?: 1
            val transform = Matrix().apply {
                when (orientation) {
                    2 -> setScale(-1f, 1f)
                    3 -> setRotate(180f)
                    4 -> setScale(1f, -1f)
                    5 -> { setRotate(90f); postScale(-1f, 1f) }
                    6 -> setRotate(90f)
                    7 -> { setRotate(270f); postScale(-1f, 1f) }
                    8 -> setRotate(270f)
                }
            }
            val oriented = Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, transform, true).also { bitmaps.add(it) }
            val edge = minOf(oriented.width, oriented.height)
            val square = Bitmap.createBitmap(oriented, (oriented.width - edge) / 2, (oriented.height - edge) / 2, edge, edge).also { bitmaps.add(it) }
            val avatar = Bitmap.createScaledBitmap(square, 256, 256, true).also { bitmaps.add(it) }
            val output = ByteArrayOutputStream()
            for (quality in listOf(85, 65, 45)) {
                output.reset()
                require(avatar.compress(Bitmap.CompressFormat.JPEG, quality, output))
                if (output.size() <= ProfileIdentity.MAX_PHOTO * 3 / 4) break
            }
            // Re-encoding retains pixels only, not EXIF location or camera metadata.
            val encoded = Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
            require(ProfileIdentity.validPhoto(encoded))
            return encoded
        } finally { bitmaps.forEach { it.recycle() } }
    }

    fun show(view: ImageView, encoded: String?) {
        val bitmap = runCatching {
            val value = encoded.orEmpty()
            require(value.isNotEmpty() && ProfileIdentity.validPhoto(value))
            val bytes = Base64.decode(value, Base64.NO_WRAP)
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
            require(bounds.outWidth in 1..512 && bounds.outHeight in 1..512)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }.getOrNull()
        if (bitmap == null) view.setImageResource(R.drawable.fitnessrun_logo) else view.setImageBitmap(bitmap)
    }
}
