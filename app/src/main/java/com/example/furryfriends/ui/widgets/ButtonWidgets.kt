package com.example.furryfriends.ui.widgets

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun PetModalButton(
    modifier: Modifier = Modifier,
    title: String? = null,
    onDismiss: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    var open by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(onClick = { open = true }) { Text("Details") }
    }

    if (open) {
        Dialog(onDismissRequest = { open = false; onDismiss() }) {
            // constrain max height so content can scroll
            Box(
                modifier
                    .widthIn(max = 360.dp)
                    .heightIn(max = 480.dp)
                    .background(Color.Gray, shape = RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                ) {
                    if (title != null) {
                        Text(title, style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.height(8.dp))
                    Column(content = content)
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { open = false; onDismiss() }) { Text("Close") }
                    }
                }
            }
        }
    }
}

@Composable
fun ShareButton(
    modifier: Modifier = Modifier,
    label: String,
    linkUrl: String?,
    petName: String?,
    petBreed: String?,
    pictureUrl: String?,
    subject: String? = "Give this fur baby a home:", // optional email subject
    chooserTitle: String = "Share via"
) {
    val context = LocalContext.current

    Button(onClick = {
        if (linkUrl == null) return@Button

        val shareMessage = buildString {
            subject?.let { append("$it\n") }
            petName?.let { append("$it\n") }
            petBreed?.let { append("$it\n") }
            pictureUrl?.let { append("$it\n\n") }
            append("Adoption link: $linkUrl")
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareMessage)
            subject?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
        }

        val chooser = Intent.createChooser(intent, chooserTitle)
        // If context is not Activity, need FLAG_ACTIVITY_NEW_TASK
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }) {
        Text(label)
    }
}