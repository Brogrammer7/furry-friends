package com.example.furryfriends.ui.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PhoneInTalk
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.furryfriends.R

@Composable
fun PetModalButton(
    modifier: Modifier = Modifier,
    title: String? = null,
    onDismiss: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    var open by remember { mutableStateOf(false) }

    IconButton(onClick = { open = true }) {
        Icon(
            imageVector = Icons.Outlined.PhoneInTalk,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
    }

    if (open) {
        Dialog(onDismissRequest = { open = false; onDismiss() }) {
            // constrain max height so content can scroll
            Box(
                modifier
                    .widthIn(max = 360.dp)
                    .heightIn(max = 480.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            ) {
                val scrollState = rememberScrollState()
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
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
}

@Composable
fun ShareButton(
    modifier: Modifier = Modifier,
    pictureUrl: String?,
    subject: String? = "Give this pet a home:", // optional email subject
    petName: String?,
    petBreed: String?,
    linkUrl: String?,
    phoneNumber: String?,
    chooserTitle: String = "Share via",
) {
    val context = LocalContext.current

    IconButton(onClick = {
        if (linkUrl == null) return@IconButton

        val shareMessage = buildString {
            pictureUrl?.let { append("$it\n\n") }
            subject?.let { append("$it\n") }
            petName?.let { append("$it, ") }
            petBreed?.let { append("$it\n\n") }
            append("Adoption link: $linkUrl\n")
            phoneNumber?.let { append("\nContact:\n$it") }
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareMessage)
        }

        val chooser = Intent.createChooser(intent, chooserTitle)
        // If context is not Activity, need FLAG_ACTIVITY_NEW_TASK
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }) {
        Icon(
            imageVector = Icons.Outlined.Share,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun FavoriteButton(
    isFavorite: Boolean,
    onFavoriteClick: (Boolean) -> Unit
) {
    IconButton(onClick = {
        onFavoriteClick(!isFavorite)
        //TODO implement click logic
    }) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = null,
            tint = if (isFavorite) Color.Yellow else MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun SignOutButton(
    modifier: Modifier = Modifier,
    onSignOut: () -> Unit = {}
) {
    var open by remember { mutableStateOf(false) }

    TextButton(onClick = { open = true }, modifier = modifier) {
        Text(stringResource(R.string.sign_out))
    }

    if (open) {
        Dialog(onDismissRequest = {}) {
            Box(
                modifier
                    .widthIn(max = 360.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            ) {
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.are_you_sure_you_want_to_exit))
                        Spacer(Modifier.height(16.dp))
                        Row(Modifier.fillMaxWidth()) {
                            TextButton(
                                onClick = {},
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(stringResource(R.string.no))
                            }
                            Text("|", modifier = Modifier.align(Alignment.CenterVertically))
                            TextButton(
                                onClick = { onSignOut() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(stringResource(R.string.yes))
                            }
                        }
                    }
                }
            }
        }
    }
}