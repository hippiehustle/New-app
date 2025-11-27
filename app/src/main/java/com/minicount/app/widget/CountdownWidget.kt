package com.minicount.app.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.minicount.app.data.local.MiniCountDatabase
import com.minicount.app.data.repository.EventRepository
import com.minicount.app.domain.util.CountdownCalculator
import com.minicount.app.presentation.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

class CountdownWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = com.minicount.app.data.local.DatabaseProvider.getDatabase(context)
        val repository = EventRepository(database.eventDao())

        provideContent {
            val events = withContext(Dispatchers.IO) {
                repository.getEventByIdSync(1) // Get first event for now
            }

            events?.let { event ->
                val targetDate = if (event.isRepeating) {
                    CountdownCalculator.getNextOccurrence(event.targetDate, event.repeatInterval)
                } else {
                    event.targetDate
                }
                val countdown = CountdownCalculator.calculate(targetDate)

                WidgetContent(
                    title = event.title,
                    category = event.category.icon,
                    countdown = CountdownCalculator.formatShort(countdown),
                    isPast = countdown.isPast,
                    color = Color(event.color),
                    photoUri = event.photoUri,
                    context = context
                )
            } ?: EmptyWidgetContent()
        }
    }
}

class CountdownWidgetSmall : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = com.minicount.app.data.local.DatabaseProvider.getDatabase(context)
        val repository = EventRepository(database.eventDao())

        provideContent {
            val events = withContext(Dispatchers.IO) {
                repository.getEventByIdSync(1)
            }

            events?.let { event ->
                val targetDate = if (event.isRepeating) {
                    CountdownCalculator.getNextOccurrence(event.targetDate, event.repeatInterval)
                } else {
                    event.targetDate
                }
                val countdown = CountdownCalculator.calculate(targetDate)

                SmallWidgetContent(
                    title = event.title,
                    countdown = CountdownCalculator.formatShort(countdown),
                    color = Color(event.color)
                )
            } ?: EmptyWidgetContent()
        }
    }
}

class CountdownWidgetLarge : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = com.minicount.app.data.local.DatabaseProvider.getDatabase(context)
        val repository = EventRepository(database.eventDao())

        provideContent {
            val events = withContext(Dispatchers.IO) {
                repository.getEventByIdSync(1)
            }

            events?.let { event ->
                val targetDate = if (event.isRepeating) {
                    CountdownCalculator.getNextOccurrence(event.targetDate, event.repeatInterval)
                } else {
                    event.targetDate
                }
                val countdown = CountdownCalculator.calculate(targetDate)

                LargeWidgetContent(
                    title = event.title,
                    description = event.description,
                    category = event.category.icon,
                    countdown = countdown,
                    color = Color(event.color),
                    photoUri = event.photoUri,
                    context = context
                )
            } ?: EmptyWidgetContent()
        }
    }
}

@Composable
fun WidgetContent(
    title: String,
    category: String,
    countdown: String,
    isPast: Boolean,
    color: Color,
    photoUri: String?,
    context: Context
) {
    val intent = Intent(context, MainActivity::class.java)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(color.copy(alpha = 0.9f)))
            .cornerRadius(16.dp)
            .padding(16.dp)
            .clickable { context.startActivity(intent) },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category,
                style = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(Color.White)
                )
            )
            Spacer(modifier = GlanceModifier.height(12.dp))
            Text(
                text = countdown,
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(Color.White)
                )
            )
            if (isPast) {
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = "ago",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = ColorProvider(Color.White.copy(alpha = 0.8f))
                    )
                )
            }
        }
    }
}

@Composable
fun SmallWidgetContent(
    title: String,
    countdown: String,
    color: Color
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(color.copy(alpha = 0.9f)))
            .cornerRadius(16.dp)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(Color.White)
                ),
                maxLines = 1
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = countdown,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(Color.White)
                )
            )
        }
    }
}

@Composable
fun LargeWidgetContent(
    title: String,
    description: String,
    category: String,
    countdown: com.minicount.app.domain.util.CountdownData,
    color: Color,
    photoUri: String?,
    context: Context
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(color.copy(alpha = 0.9f)))
            .cornerRadius(16.dp)
            .padding(16.dp)
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category,
                    style = TextStyle(fontSize = 40.sp)
                )
                Spacer(modifier = GlanceModifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = TextStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(Color.White)
                        )
                    )
                    if (description.isNotEmpty()) {
                        Text(
                            text = description,
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = ColorProvider(Color.White.copy(alpha = 0.8f))
                            ),
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = GlanceModifier.height(16.dp))

            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (countdown.years > 0) {
                    CountdownUnit(countdown.years.toString(), "Years")
                    Spacer(modifier = GlanceModifier.width(8.dp))
                }
                if (countdown.months > 0 || countdown.years > 0) {
                    CountdownUnit(countdown.months.toString(), "Months")
                    Spacer(modifier = GlanceModifier.width(8.dp))
                }
                CountdownUnit(countdown.days.toString(), "Days")
                Spacer(modifier = GlanceModifier.width(8.dp))
                CountdownUnit(countdown.hours.toString(), "Hours")
            }
        }
    }
}

@Composable
fun CountdownUnit(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = TextStyle(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = ColorProvider(Color.White)
            )
        )
        Text(
            text = label,
            style = TextStyle(
                fontSize = 12.sp,
                color = ColorProvider(Color.White.copy(alpha = 0.7f))
            )
        )
    }
}

@Composable
fun EmptyWidgetContent() {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color(0xFF6750A4)))
            .cornerRadius(16.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📅",
                style = TextStyle(fontSize = 48.sp)
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            Text(
                text = "Tap to add event",
                style = TextStyle(
                    fontSize = 16.sp,
                    color = ColorProvider(Color.White)
                )
            )
        }
    }
}
