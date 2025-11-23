package com.minicount.app.presentation.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.minicount.app.data.local.entity.Event
import com.minicount.app.data.local.entity.EventCategory
import com.minicount.app.domain.util.CountdownCalculator
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EnhancedEventList(
    events: List<Event>,
    searchQuery: String,
    selectedCategory: EventCategory?,
    sortBy: SortOption,
    onEventClick: (Long) -> Unit,
    onEventDelete: (Event) -> Unit,
    onEventTogglePin: (Event) -> Unit,
    onEventShare: (Event) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    val filteredAndSortedEvents = remember(events, searchQuery, selectedCategory, sortBy) {
        events
            .filter { event ->
                val matchesSearch = searchQuery.isEmpty() ||
                        event.title.contains(searchQuery, ignoreCase = true) ||
                        event.description.contains(searchQuery, ignoreCase = true)

                val matchesCategory = selectedCategory == null || event.category == selectedCategory

                matchesSearch && matchesCategory
            }
            .sortedWith(
                when (sortBy) {
                    SortOption.DATE_ASC -> compareBy { it.targetDate }
                    SortOption.DATE_DESC -> compareByDescending { it.targetDate }
                    SortOption.NAME_ASC -> compareBy { it.title.lowercase() }
                    SortOption.NAME_DESC -> compareByDescending { it.title.lowercase() }
                    SortOption.CATEGORY -> compareBy { it.category.name }
                    SortOption.PINNED_FIRST -> compareByDescending<Event> { it.isPinned }
                        .thenBy { it.targetDate }
                }
            )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = filteredAndSortedEvents,
            key = { it.id }
        ) { event ->
            AnimatedEventCard(
                event = event,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onEventClick(event.id)
                },
                onDelete = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onEventDelete(event)
                },
                onTogglePin = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onEventTogglePin(event)
                },
                onShare = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onEventShare(event)
                },
                modifier = Modifier.animateItemPlacement(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            )
        }

        if (filteredAndSortedEvents.isEmpty()) {
            item {
                EmptySearchResults(
                    searchQuery = searchQuery,
                    selectedCategory = selectedCategory
                )
            }
        } else {
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun AnimatedEventCard(
    event: Event,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onTogglePin: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isExpanded) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    val targetDate = if (event.isRepeating) {
        CountdownCalculator.getNextOccurrence(event.targetDate, event.repeatInterval)
    } else {
        event.targetDate
    }

    val countdown = CountdownCalculator.calculate(targetDate)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isExpanded) 8.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(event.color).copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = event.category.icon,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = event.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (event.description.isNotEmpty()) {
                            Text(
                                text = event.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Row {
                    IconButton(onClick = onTogglePin) {
                        Icon(
                            if (event.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Pin",
                            tint = if (event.isPinned) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    IconButton(onClick = { isExpanded = !isExpanded }) {
                        Icon(
                            if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expand"
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = if (countdown.isPast) "Since" else "Until",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = targetDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(event.color).copy(alpha = 0.2f)
                ) {
                    Text(
                        text = CountdownCalculator.formatShort(countdown),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(event.color)
                    )
                }
            }

            // Expanded actions
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    Divider()
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(onClick = onShare) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    if (event.isRepeating) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Repeats ${event.repeatInterval.name.lowercase()}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Event") },
            text = { Text("Are you sure you want to delete \"${event.title}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Search events...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Search")
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun FilterChipRow(
    selectedCategory: EventCategory?,
    onCategorySelect: (EventCategory?) -> Unit,
    sortBy: SortOption,
    onSortChange: (SortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSortMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // All category chip
        FilterChip(
            selected = selectedCategory == null,
            onClick = { onCategorySelect(null) },
            label = { Text("All") },
            leadingIcon = {
                Icon(
                    Icons.Default.Apps,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        )

        // Sort chip
        Box {
            FilterChip(
                selected = false,
                onClick = { showSortMenu = true },
                label = { Text("Sort") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Sort,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )

            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { showSortMenu = false }
            ) {
                SortOption.values().forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.displayName) },
                        onClick = {
                            onSortChange(option)
                            showSortMenu = false
                        },
                        leadingIcon = {
                            if (sortBy == option) {
                                Icon(Icons.Default.Check, contentDescription = null)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptySearchResults(
    searchQuery: String,
    selectedCategory: EventCategory?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "No events found",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        if (searchQuery.isNotEmpty() || selectedCategory != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Try adjusting your search or filters",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

enum class SortOption(val displayName: String) {
    PINNED_FIRST("Pinned First"),
    DATE_ASC("Date (Earliest)"),
    DATE_DESC("Date (Latest)"),
    NAME_ASC("Name (A-Z)"),
    NAME_DESC("Name (Z-A)"),
    CATEGORY("Category")
}
