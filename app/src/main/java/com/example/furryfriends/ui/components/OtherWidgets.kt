package com.example.furryfriends.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.furryfriends.R
import com.example.furryfriends.ui.viewmodels.SortOption
import kotlin.math.min

@Composable
fun FurryFriendsAppBar(
    titleText: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = titleText,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(0.7f)
            )
        },
        navigationIcon = navigationIcon,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = modifier
    )
}

@Composable
fun SpinningLoader(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = 4.dp
) {
    // Animate 0..1 and convert to degrees to avoid snapping
    val anim by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing)
        )
    )
    val angle = anim * 360f

    Canvas(modifier = modifier.size(size)) {
        val canvasSize = this.size
        val radius = min(canvasSize.width, canvasSize.height) / 2f
        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)

        // faded background circle
        drawCircle(color = color.copy(alpha = 0.18f), radius = radius, style = stroke)

        // rotate around canvas center and draw the arc
        rotate(degrees = angle, pivot = Offset(canvasSize.width / 2f, canvasSize.height / 2f)) {
            drawArc(
                color = color,
                startAngle = 0f,
                sweepAngle = 270f,
                useCenter = false,
                style = stroke,
                topLeft = Offset((canvasSize.width - 2 * radius) / 2f, (canvasSize.height - 2 * radius) / 2f),
                size = Size(2 * radius, 2 * radius)
            )
        }
    }
}

@Composable
fun CopyrightText() {
    val currentYear = remember {
        java.time.LocalDate.now().year
    }

    Text(
        text = "© $currentYear CSO Industries Inc.",
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.padding(bottom = 16.dp)
    )
}

@Composable
fun SortModal(
    showSortModal: MutableState<Boolean>,
    currentSortOption: SortOption,
    onSortOptionSelected: (SortOption) -> Unit
) {
    if (showSortModal.value) {
        var selectedOption by remember { mutableStateOf(currentSortOption) }

        AlertDialog(
            onDismissRequest = { showSortModal.value = false },
            title = { Text(stringResource(R.string.sort_results_by)) },
            text = {
                Column {
                    SortOptionRow(stringResource(R.string.pet_name), SortOption.PET_NAME, selectedOption) { 
                        onSortOptionSelected(it)
                        showSortModal.value = false
                    }
                    SortOptionRow(stringResource(R.string.shelter_name), SortOption.SHELTER_NAME, selectedOption) { 
                        onSortOptionSelected(it)
                        showSortModal.value = false
                    }
                    SortOptionRow(stringResource(R.string.location), SortOption.CITY, selectedOption) { 
                        onSortOptionSelected(it)
                        showSortModal.value = false
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSortModal.value = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun SortOptionRow(
    label: String,
    option: SortOption,
    selectedOption: SortOption,
    onClick: (SortOption) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(48.dp)
            .selectable(
                selected = (option == selectedOption),
                onClick = { onClick(option) },
                role = Role.RadioButton
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = (option == selectedOption),
            onClick = null
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}
