package com.minicount.app.domain.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import androidx.core.content.FileProvider
import com.minicount.app.data.local.entity.Event
import com.minicount.app.domain.util.CountdownCalculator
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShareManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun shareEventAsText(event: Event): Intent {
        val targetDate = if (event.isRepeating) {
            CountdownCalculator.getNextOccurrence(event.targetDate, event.repeatInterval)
        } else {
            event.targetDate
        }

        val countdown = CountdownCalculator.calculate(targetDate)
        val formattedCountdown = CountdownCalculator.formatCountdown(countdown)

        val shareText = buildString {
            append("${event.category.icon} ${event.title}\n")
            if (event.description.isNotEmpty()) {
                append("${event.description}\n")
            }
            append("\n")
            if (countdown.isPast) {
                append("$formattedCountdown ago")
            } else {
                append("$formattedCountdown to go!")
            }
            append("\n\nTrack your countdowns with MiniCount")
        }

        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, event.title)
        }
    }

    fun createShareImage(event: Event): Bitmap {
        val width = 1080
        val height = 1080
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        val bgPaint = Paint().apply {
            color = event.color
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Category emoji
        val emojiPaint = Paint().apply {
            textSize = 120f
            color = android.graphics.Color.WHITE
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(event.category.icon, width / 2f, 250f, emojiPaint)

        // Title
        val titlePaint = Paint().apply {
            textSize = 64f
            color = android.graphics.Color.WHITE
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            isFakeBoldText = true
        }

        // Word wrap title
        val words = event.title.split(" ")
        var line = ""
        var y = 400f
        words.forEach { word ->
            val testLine = if (line.isEmpty()) word else "$line $word"
            val bounds = Rect()
            titlePaint.getTextBounds(testLine, 0, testLine.length, bounds)

            if (bounds.width() > width - 100) {
                canvas.drawText(line, width / 2f, y, titlePaint)
                line = word
                y += 80f
            } else {
                line = testLine
            }
        }
        if (line.isNotEmpty()) {
            canvas.drawText(line, width / 2f, y, titlePaint)
        }

        // Countdown
        val targetDate = if (event.isRepeating) {
            CountdownCalculator.getNextOccurrence(event.targetDate, event.repeatInterval)
        } else {
            event.targetDate
        }
        val countdown = CountdownCalculator.calculate(targetDate)

        val countdownPaint = Paint().apply {
            textSize = 96f
            color = android.graphics.Color.WHITE
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            isFakeBoldText = true
        }
        canvas.drawText(CountdownCalculator.formatShort(countdown), width / 2f, 700f, countdownPaint)

        // "to go" or "ago"
        val labelPaint = Paint().apply {
            textSize = 40f
            color = android.graphics.Color.WHITE
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(if (countdown.isPast) "ago" else "to go", width / 2f, 780f, labelPaint)

        // Date
        val datePaint = Paint().apply {
            textSize = 36f
            color = android.graphics.Color.WHITE
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val dateFormatter = java.time.format.DateTimeFormatter.ofPattern("MMMM dd, yyyy")
        canvas.drawText(targetDate.format(dateFormatter), width / 2f, 880f, datePaint)

        return bitmap
    }

    fun shareEventAsImage(event: Event): Intent {
        val bitmap = createShareImage(event)

        // Save to cache
        val imagesFolder = File(context.cacheDir, "images")
        imagesFolder.mkdirs()
        val file = File(imagesFolder, "countdown_${event.id}_${System.currentTimeMillis()}.png")

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        return Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
