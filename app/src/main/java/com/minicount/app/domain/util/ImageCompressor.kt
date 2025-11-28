package com.minicount.app.domain.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.min

/**
 * Utility for compressing and optimizing images for event photos.
 *
 * Handles:
 * - Image downsizing to reduce memory usage
 * - JPEG compression for storage efficiency
 * - EXIF orientation correction
 * - Memory-safe loading for large images
 */
object ImageCompressor {

    private const val TAG = "ImageCompressor"

    /**
     * Maximum dimension (width or height) for compressed images.
     * This ensures images are optimized for mobile display without excessive memory usage.
     */
    private const val MAX_DIMENSION = 1920

    /**
     * JPEG compression quality (0-100).
     * 85 provides good balance between quality and file size.
     */
    private const val JPEG_QUALITY = 85

    /**
     * Compresses an image from a URI and saves it to app's cache directory.
     *
     * This function:
     * 1. Loads the image efficiently using inSampleSize
     * 2. Corrects EXIF orientation if needed
     * 3. Resizes to maximum dimension while maintaining aspect ratio
     * 4. Compresses as JPEG at specified quality
     * 5. Saves to cache directory
     *
     * @param context Android context for accessing content resolver and cache
     * @param imageUri URI of the source image
     * @param targetFileName Optional custom filename (default: timestamp-based)
     * @return URI of the compressed image file, or null if compression failed
     */
    fun compressImage(
        context: Context,
        imageUri: Uri,
        targetFileName: String = "event_photo_${System.currentTimeMillis()}.jpg"
    ): Uri? {
        return try {
            // Get input stream from URI
            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: return null

            // Decode image with inSampleSize for memory efficiency
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            // Calculate sample size
            val sampleSize = calculateInSampleSize(options, MAX_DIMENSION, MAX_DIMENSION)

            // Decode actual bitmap
            val decodedInputStream = context.contentResolver.openInputStream(imageUri)
                ?: return null

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            var bitmap = BitmapFactory.decodeStream(decodedInputStream, null, decodeOptions)
            decodedInputStream.close()

            if (bitmap == null) {
                Log.e(TAG, "Failed to decode bitmap from URI: $imageUri")
                return null
            }

            // Correct orientation based on EXIF data
            bitmap = correctOrientation(context, imageUri, bitmap)

            // Resize if still too large
            bitmap = resizeBitmap(bitmap, MAX_DIMENSION)

            // Save compressed image
            val outputFile = File(context.cacheDir, targetFileName)
            FileOutputStream(outputFile).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream)
            }

            // Recycle bitmap to free memory
            bitmap.recycle()

            Log.d(TAG, "Image compressed successfully: ${outputFile.absolutePath}, size: ${outputFile.length() / 1024}KB")
            Uri.fromFile(outputFile)
        } catch (e: Exception) {
            Log.e(TAG, "Error compressing image", e)
            null
        }
    }

    /**
     * Calculates the optimal inSampleSize for bitmap loading.
     *
     * inSampleSize tells BitmapFactory to load a smaller version of the image,
     * reducing memory usage. For example, inSampleSize=2 loads an image at 1/2 dimensions.
     *
     * @param options BitmapFactory options containing image dimensions
     * @param reqWidth Required width
     * @param reqHeight Required height
     * @return Optimal sample size (power of 2)
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

            // Calculate the largest inSampleSize value that is a power of 2
            while ((halfHeight / inSampleSize) >= reqHeight &&
                (halfWidth / inSampleSize) >= reqWidth
            ) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    /**
     * Corrects image orientation based on EXIF data.
     *
     * Many cameras/phones store orientation in EXIF metadata rather than
     * rotating the actual pixels. This function applies the correct rotation.
     *
     * @param context Android context
     * @param imageUri URI of the image
     * @param bitmap The bitmap to rotate
     * @return Rotated bitmap if rotation was needed, otherwise original bitmap
     */
    private fun correctOrientation(
        context: Context,
        imageUri: Uri,
        bitmap: Bitmap
    ): Bitmap {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri) ?: return bitmap
            val exif = ExifInterface(inputStream)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            inputStream.close()

            val rotationDegrees = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }

            if (rotationDegrees != 0f) {
                val matrix = Matrix().apply { postRotate(rotationDegrees) }
                val rotated = Bitmap.createBitmap(
                    bitmap,
                    0,
                    0,
                    bitmap.width,
                    bitmap.height,
                    matrix,
                    true
                )
                bitmap.recycle() // Free original bitmap
                rotated
            } else {
                bitmap
            }
        } catch (e: IOException) {
            Log.w(TAG, "Could not read EXIF data, skipping orientation correction", e)
            bitmap
        }
    }

    /**
     * Resizes a bitmap to fit within maximum dimensions while maintaining aspect ratio.
     *
     * @param bitmap Source bitmap
     * @param maxDimension Maximum width or height
     * @return Resized bitmap, or original if already small enough
     */
    private fun resizeBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxDimension && height <= maxDimension) {
            return bitmap
        }

        val scale = min(
            maxDimension.toFloat() / width,
            maxDimension.toFloat() / height
        )

        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        val resized = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)

        if (resized != bitmap) {
            bitmap.recycle() // Free original bitmap
        }

        return resized
    }

    /**
     * Gets the size of an image file in kilobytes.
     *
     * @param context Android context
     * @param imageUri URI of the image
     * @return File size in KB, or 0 if unable to determine
     */
    fun getImageSize(context: Context, imageUri: Uri): Long {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val size = inputStream?.available()?.toLong() ?: 0L
            inputStream?.close()
            size / 1024 // Convert to KB
        } catch (e: Exception) {
            Log.e(TAG, "Error getting image size", e)
            0L
        }
    }

    /**
     * Deletes a compressed image file from cache.
     *
     * @param imageUri URI of the cached image
     * @return true if deleted successfully, false otherwise
     */
    fun deleteCachedImage(imageUri: Uri): Boolean {
        return try {
            val file = File(imageUri.path ?: return false)
            file.delete()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting cached image", e)
            false
        }
    }
}
