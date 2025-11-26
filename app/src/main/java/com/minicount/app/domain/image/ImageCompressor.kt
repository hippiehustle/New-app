package com.minicount.app.domain.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles image compression and optimization
 */
@Singleton
class ImageCompressor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val DEFAULT_MAX_WIDTH = 1920
        private const val DEFAULT_MAX_HEIGHT = 1920
        private const val DEFAULT_QUALITY = 85
        private const val THUMBNAIL_SIZE = 512
        private const val THUMBNAIL_QUALITY = 75
    }

    /**
     * Compress an image from URI
     */
    suspend fun compressImage(
        sourceUri: Uri,
        maxWidth: Int = DEFAULT_MAX_WIDTH,
        maxHeight: Int = DEFAULT_MAX_HEIGHT,
        quality: Int = DEFAULT_QUALITY
    ): File? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return@withContext null

            // Decode image bounds first
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)
            options.inJustDecodeBounds = false

            // Decode actual bitmap
            val secondInputStream = context.contentResolver.openInputStream(sourceUri)
            val bitmap = BitmapFactory.decodeStream(secondInputStream, null, options)
            secondInputStream?.close()

            bitmap ?: return@withContext null

            // Rotate if needed based on EXIF data
            val rotatedBitmap = rotateImageIfRequired(bitmap, sourceUri)

            // Resize if still too large
            val resizedBitmap = resizeBitmap(rotatedBitmap, maxWidth, maxHeight)

            // Save to file
            val outputFile = createTempImageFile()
            FileOutputStream(outputFile).use { out ->
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }

            // Clean up bitmaps
            if (rotatedBitmap != bitmap) {
                bitmap.recycle()
            }
            if (resizedBitmap != rotatedBitmap) {
                rotatedBitmap.recycle()
            }
            resizedBitmap.recycle()

            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Create a thumbnail from an image URI
     */
    suspend fun createThumbnail(sourceUri: Uri): File? = withContext(Dispatchers.IO) {
        compressImage(sourceUri, THUMBNAIL_SIZE, THUMBNAIL_SIZE, THUMBNAIL_QUALITY)
    }

    /**
     * Compress a bitmap directly
     */
    suspend fun compressBitmap(
        bitmap: Bitmap,
        maxWidth: Int = DEFAULT_MAX_WIDTH,
        maxHeight: Int = DEFAULT_MAX_HEIGHT,
        quality: Int = DEFAULT_QUALITY
    ): File? = withContext(Dispatchers.IO) {
        try {
            val resizedBitmap = resizeBitmap(bitmap, maxWidth, maxHeight)

            val outputFile = createTempImageFile()
            FileOutputStream(outputFile).use { out ->
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }

            if (resizedBitmap != bitmap) {
                resizedBitmap.recycle()
            }

            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Calculate sample size for efficient memory usage
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight &&
                (halfWidth / inSampleSize) >= reqWidth
            ) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    /**
     * Resize bitmap to fit within max dimensions while maintaining aspect ratio
     */
    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }

        val aspectRatio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int

        if (width > height) {
            newWidth = maxWidth
            newHeight = (maxWidth / aspectRatio).toInt()
        } else {
            newHeight = maxHeight
            newWidth = (maxHeight * aspectRatio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * Rotate image based on EXIF orientation
     */
    private fun rotateImageIfRequired(bitmap: Bitmap, imageUri: Uri): Bitmap {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val exif = inputStream?.let { ExifInterface(it) }
            inputStream?.close()

            val orientation = exif?.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            ) ?: ExifInterface.ORIENTATION_NORMAL

            return when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                else -> bitmap
            }
        } catch (e: IOException) {
            return bitmap
        }
    }

    /**
     * Rotate bitmap by degrees
     */
    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    /**
     * Create a temporary file for compressed images
     */
    private fun createTempImageFile(): File {
        val storageDir = context.cacheDir
        return File.createTempFile(
            "minicount_${System.currentTimeMillis()}_",
            ".jpg",
            storageDir
        )
    }

    /**
     * Get image dimensions without loading the full bitmap
     */
    suspend fun getImageDimensions(uri: Uri): Pair<Int, Int>? = withContext(Dispatchers.IO) {
        try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            val inputStream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            Pair(options.outWidth, options.outHeight)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get estimated file size after compression
     */
    suspend fun getEstimatedCompressedSize(
        uri: Uri,
        quality: Int = DEFAULT_QUALITY
    ): Long = withContext(Dispatchers.IO) {
        try {
            val dimensions = getImageDimensions(uri) ?: return@withContext 0L
            val (width, height) = dimensions

            // Rough estimation: quality% of (width * height * 3 bytes per pixel / 10)
            (width * height * 3 * quality / 1000).toLong()
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Clean up old temporary image files
     */
    suspend fun cleanupTempFiles(olderThanMillis: Long = 24 * 60 * 60 * 1000) = withContext(Dispatchers.IO) {
        try {
            val now = System.currentTimeMillis()
            context.cacheDir.listFiles()?.forEach { file ->
                if (file.name.startsWith("minicount_") &&
                    file.name.endsWith(".jpg") &&
                    (now - file.lastModified()) > olderThanMillis
                ) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
