package com.example.furryfriends.ui.components

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.example.furryfriends.R
import com.example.furryfriends.domain.model.PetDisplayItem

@Composable
fun PetSearchList(
    modifier: Modifier = Modifier,
    animalsWithOrgs: List<PetDisplayItem>,
    favoritePetIds: Set<String> = emptySet(),
    onFavoriteClick: (String) -> Unit = {},
    onClearAllClick: (() -> Unit)? = null,
    petCountText: String? = null
) {
    val listState = rememberLazyListState()

    val infiniteTransition = rememberInfiniteTransition(label = "arrow bounce")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrow offset"
    )

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (petCountText != null && animalsWithOrgs.isNotEmpty()) {
                item {
                    Text(
                        text = petCountText,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 8.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            items(
                items = animalsWithOrgs,
                key = { it.animal.id }
            ) { petDisplayItem ->
                val showModal = remember { mutableStateOf(false) }

                PetCard(
                    petDisplayItem = petDisplayItem,
                    showModal = showModal,
                    favoritePetIds = favoritePetIds,
                    onFavoriteClick = onFavoriteClick
                )

                PetModal(showModal = showModal) {
                    PetModalContent(petDisplayItem = petDisplayItem)
                }
            }

            if (onClearAllClick != null && animalsWithOrgs.isNotEmpty()) {
                item {
                    TextButton(
                        onClick = onClearAllClick,
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.clear_all_saved_pets),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Floating scroll indicator for the list
        AnimatedVisibility(
            visible = listState.canScrollForward,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = stringResource(R.string.scroll_down),
                modifier = Modifier
                    .size(48.dp)
                    .offset(y = offsetY.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun SetClickableContactInfo(
    phone: String?,
    url: String?
) {
    val ctx = LocalContext.current

    val activityStarter = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { /* no-op: we don't need a result */ }

    fun canResolve(intent: Intent): Boolean =
        intent.resolveActivity(ctx.packageManager) != null

    fun isValidUrl(urlString: String?): Boolean {
        if (urlString.isNullOrBlank()) return false

        return try {
            val uri = urlString.toUri()
            // Check for a non-empty, meaningful host
            !uri.host.isNullOrBlank() &&
                    // Exclude bare "http://" or "https://"
                    uri.host != "http" &&
                    uri.host != "https" &&
                    // Additional check to ensure it's not just a protocol
                    urlString.trim() != "http://" &&
                    urlString.trim() != "https://"
        } catch (e: Exception) {
            false
        }
    }

    phone?.let { value ->
        val interactionSource = remember { MutableInteractionSource() }
        Text(
            text = value,
            textAlign = TextAlign.Start,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp, top = 8.dp)
                .clickable(
                    interactionSource = interactionSource,
                    onClick = {
                        val telUri = "tel:${value.filter { it.isDigit() || it == '+' }}".toUri()
                        val intent = Intent(Intent.ACTION_DIAL, telUri)
                        if (canResolve(intent)) {
                            activityStarter.launch(intent)
                        }
                    }
                )
        )
    }

    url?.let { value ->
        // Only show the URL if it's a valid, meaningful URL
        if (isValidUrl(value)) {
            val interactionSource = remember { MutableInteractionSource() }
            Text(
                text = value,
                textAlign = TextAlign.Start,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, bottom = 8.dp, top = 8.dp)
                    .clickable(
                        interactionSource = interactionSource,
                        onClick = {
                            val processedUrl = try {
                                val uri = value.toUri()

                                // If no scheme is present, prepend https://
                                if (uri.scheme.isNullOrBlank()) {
                                    "https://$value"
                                } else if (uri.scheme == "http") {
                                    // Upgrade http to https
                                    value.replace("http://", "https://")
                                } else {
                                    value
                                }
                            } catch (e: Exception) {
                                // Fallback to https:// if parsing fails
                                "https://$value"
                            }

                            val webIntent = Intent(Intent.ACTION_VIEW, processedUrl.toUri())
                            if (canResolve(webIntent)) {
                                activityStarter.launch(webIntent)
                            }
                        }
                    )
            )
        }
    }
}

@Composable
fun LocalListLazyRow(petPhotos: List<Int>) {
    LazyRow(
        modifier = Modifier.padding(end = 16.dp),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(petPhotos) { resId ->
            Image(
                painter = painterResource(id = resId),
                contentDescription = null,
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .border(width = 3.dp, color = MaterialTheme.colorScheme.primary, shape = CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}
