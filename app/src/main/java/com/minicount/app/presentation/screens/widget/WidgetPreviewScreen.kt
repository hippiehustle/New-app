package com.minicount.app.presentation.screens.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.minicount.app.data.local.entity.Event
import com.minicount.app.domain.util.CountdownCalculator
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetPreviewScreen(
    onNavigateBack: () -> Unit,
    eventId: Long,
    viewModel: WidgetPreviewViewModel = hiltViewModel()
) {
    val event by viewModel.getEvent(eventId).collectAsState(initial = null)

    var showSeconds by remember { mutableStateOf(false) }
    var opacity by remember { mutableStateOf(1.0f) }
    var showCategoryIcon by remember { mutableStateOf(true) }
    var showEventName by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Widget Preview") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Preview how your widget will look on the home screen",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            item {
                Text(
                    "Small Widget (2x2)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                event?.let {
                    SmallWidgetPreview(
                        event = it,
                        showSeconds = showSeconds,
                        opacity = opacity,
                        showCategoryIcon = showCategoryIcon,
                        showEventName = showEventName
                    )
                }
            }

            item {
                Text(
                    "Medium Widget (3x2)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                event?.let {
                    MediumWidgetPreview(
                        event = it,
                        showSeconds = showSeconds,
                        opacity = opacity,
                        showCategoryIcon = showCategoryIcon,
                        showEventName = showEventName
                    )
                }
            }

            item {
                Text(
                    "Large Widget (4x2)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                event?.let {
                    LargeWidgetPreview(
                        event = it,
                        showSeconds = showSeconds,
                        opacity = opacity,
                        showCategoryIcon = showCategoryIcon,
                        showEventName = showEventName
                    )
                }
            }

            item {
                Divider()
                Spacer(Modifier.height(8.dp))
                Text(
                    "Widget Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Show Seconds")
                    Switch(
                        checked = showSeconds,
                        onCheckedChange = { showSeconds = it }
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Show Category Icon")
                    Switch(
                        checked = showCategoryIcon,
                        onCheckedChange = { showCategoryIcon = it }
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Show Event Name")
                    Switch(
                        checked = showEventName,
                        onCheckedChange = { showEventName = it }
                    )
                }
            }

            item {
                Column {
                    Text("Opacity: ${(opacity * 100).toInt()}%")
                    Slider(
                        value = opacity,
                        onValueChange = { opacity = it },
                        valueRange = 0.3f..1.0f
                    )
                }
            }
        }
    }
}

@Composable
fun SmallWidgetPreview(
    event: Event,
    showSeconds: Boolean,
    opacity: Float,
    showCategoryIcon: Boolean,
    showEventName: Boolean
) {
    val countdown = CountdownCalculator.calculate(event.targetDate)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(event.color).copy(alpha = opacity * 0.3f),
                        Color(event.color).copy(alpha = opacity * 0.1f)
                    )
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (showCategoryIcon) {
                Text(
                    text = event.category.icon,
                    fontSize = 32.sp
                )
            }
            if (showEventName) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${countdown.days}d ${countdown.hours}h",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(event.color)
            )
        }
    }
}

@Composable
fun MediumWidgetPreview(
    event: Event,
    showSeconds: Boolean,
    opacity: Float,
    showCategoryIcon: Boolean,
    showEventName: Boolean
) {
    val countdown = CountdownCalculator.calculate(event.targetDate)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(event.color).copy(alpha = opacity * 0.3f),
                        Color(event.color).copy(alpha = opacity * 0.1f)
                    )
                )
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (showCategoryIcon) {
                    Text(
                        text = event.category.icon,
                        fontSize = 40.sp
                    )
                }
                if (showEventName) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2
                    )
                }
                Text(
                    text = event.targetDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${countdown.days}",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(event.color)
                )
                Text(
                    text = "days",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
fun LargeWidgetPreview(
    event: Event,
    showSeconds: Boolean,
    opacity: Float,
    showCategoryIcon: Boolean,
    showEventName: Boolean
) {
    val countdown = CountdownCalculator.calculate(event.targetDate)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(event.color).copy(alpha = opacity * 0.3f),
                        Color(event.color).copy(alpha = opacity * 0.15f),
                        Color(event.color).copy(alpha = opacity * 0.05f)
                    )
                )
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (showCategoryIcon) {
                        Text(
                            text = event.category.icon,
                            fontSize = 32.sp
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    if (showEventName) {
                        Column {
                            Text(
                                text = event.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = event.targetDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CountdownUnit(value = countdown.days, label = "Days", color = Color(event.color))
                CountdownUnit(value = countdown.hours, label = "Hours", color = Color(event.color))
                CountdownUnit(value = countdown.minutes, label = "Minutes", color = Color(event.color))
                if (showSeconds) {
                    CountdownUnit(value = countdown.seconds, label = "Seconds", color = Color(event.color))
                }
            }
        }
    }
}

@Composable
fun CountdownUnit(value: Int, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value.toString().padStart(2, '0'),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}
