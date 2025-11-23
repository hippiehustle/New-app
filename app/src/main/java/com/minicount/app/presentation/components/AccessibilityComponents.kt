package com.minicount.app.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp

/**
 * Accessible button that provides proper content descriptions and semantic properties
 */
@Composable
fun AccessibleIconButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: @Composable () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.semantics {
            this.contentDescription = contentDescription
            this.role = Role.Button
            this.disabled = !enabled
        },
        enabled = enabled
    ) {
        icon()
    }
}

/**
 * Accessible card with proper semantic grouping
 */
@Composable
fun AccessibleCard(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.semantics(mergeDescendants = true) {
            this.contentDescription = title
            this.role = Role.Button
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            content()
        }
    }
}

/**
 * Accessible progress indicator with description
 */
@Composable
fun AccessibleLinearProgressIndicator(
    progress: Float,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    this.contentDescription = "$label: ${(progress * 100).toInt()}% complete"
                    this.stateDescription = "Progress"
                }
        )
    }
}

/**
 * Accessible text field with clear labels
 */
@Composable
fun AccessibleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null,
    supportingText: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.semantics {
            this.contentDescription = label
            if (isError && errorMessage != null) {
                this.error(errorMessage)
            }
        },
        enabled = enabled,
        isError = isError,
        supportingText = if (isError && errorMessage != null) {
            { Text(errorMessage) }
        } else if (supportingText != null) {
            { Text(supportingText) }
        } else null
    )
}

/**
 * Accessible switch with label
 */
@Composable
fun AccessibleSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier.semantics(mergeDescendants = true) {
            this.contentDescription = "$label, ${if (checked) "On" else "Off"}"
            this.role = Role.Switch
            this.toggleableState = ToggleableState(checked)
        },
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

/**
 * Accessible slider with label and value announcement
 */
@Composable
fun AccessibleSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    label: String,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    modifier: Modifier = Modifier,
    formatValue: (Float) -> String = { it.toString() }
) {
    Column(
        modifier = modifier.semantics {
            this.contentDescription = "$label: ${formatValue(value)}"
        }
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(4.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps
        )
        Text(
            formatValue(value),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}
