package com.example.furryfriends.ui.components

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.example.furryfriends.R
import com.example.furryfriends.model.IncludedItem
import com.example.furryfriends.model.ResourceItem
import java.util.Locale

@Composable
fun PetSearchList(
    modifier: Modifier = Modifier,
    animalsWithOrgs: List<Pair<ResourceItem, IncludedItem?>>,
    favoritePetIds: Set<String> = emptySet(),
    onFavoriteClick: (String) -> Unit = {},
    onClearAllClick: (() -> Unit)? = null,
    petCountText: String? = null
) {
    val listState = rememberLazyListState()

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
                key = { it.first.id }
            ) { (animal, org) ->
                val showModal = remember { mutableStateOf(false) }

                animal.let {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            contentColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 4.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                var isLoading by remember { mutableStateOf(true) }

                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { showModal.value = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = animal.attributes.pictureThumbnailUrl
                                            ?: R.drawable.no_image_icon,
                                        contentDescription = "pet image",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize(),
                                        onState = { state ->
                                            isLoading = state is AsyncImagePainter.State.Loading
                                        }
                                    )

                                    if (isLoading) {
                                        SpinningLoader(size = 32.dp)
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Buttons row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    FavoriteButton(
                                        isFavorite = favoritePetIds.contains(animal.id),
                                        modifier = Modifier.weight(1f),
                                        onFavoriteClick = { onFavoriteClick(animal.id) }
                                    )
                                    ShareButton(
                                        modifier = Modifier.weight(1f),
                                        pictureUrl = animal.attributes.pictureThumbnailUrl,
                                        petName = formatPetName(animal.attributes.name),
                                        petBreed = animal.attributes.breedPrimary,
                                        linkUrl = org?.attributes?.url,
                                        phoneNumber = org?.attributes?.phone
                                        )
                                    PetModalButton(
                                        showModal = showModal,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                // Info column
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .offset(y = (-8).dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    FormatPetName(
                                        input = animal.attributes.name,
                                        fontSize = 16.sp
                                    )
                                    org?.attributes?.let {
                                        Text(
                                            text = it.name!!,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.primary,
                                            style = TextStyle(fontSize = 10.sp),
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                        )
                                        Text(
                                            text = it.city!! + ", " + it.state!!.uppercase(Locale.getDefault()),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            textAlign = TextAlign.Center,
                                            style = TextStyle(fontSize = 10.sp),
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        )
                                    }
                                }
                            }

                        }
                    }
                }

                PetModal(showModal = showModal) {
                    if (animal.attributes.pictureThumbnailUrl != null) {
                        var isModalLoading by remember { mutableStateOf(true) }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = animal.attributes.pictureThumbnailUrl,
                                contentDescription = "pet image large",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                                onState = { state ->
                                    isModalLoading = state is AsyncImagePainter.State.Loading
                                }
                            )
                            if (isModalLoading) {
                                SpinningLoader(size = 48.dp)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    FormatPetName(animal.attributes.name, 22.sp)
                    Text(
                        text = animal.attributes.ageString ?: stringResource(R.string.age_unknown),
                        textAlign = TextAlign.Center,
                        style = TextStyle(fontSize = 12.sp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Text(
                        text = stringResource(R.string.contact_info),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    org?.attributes?.let {
                        it.name?.let { value ->
                            Text(
                                text = value,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                        it.street?.let { value ->
                            Text(
                                text = value,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                        Text(
                            text = (it.city ?: "") + ", " + (it.state?.uppercase(Locale.getDefault()) ?: ""),
                            textAlign = TextAlign.Start,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        SetClickableContactInfo(
                            phone = it.phone,
                            url = it.url
                        )

                        if (it.adoptionProcess?.isNotBlank() == true) {
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
                                        append(stringResource(id = R.string.adoption_process))
                                    }
                                    append("\n${it.adoptionProcess}")
                                },
                                textAlign = TextAlign.Start,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
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
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shadowElevation = 4.dp
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = "Scroll down",
                    modifier = Modifier.size(32.dp)
                )
            }
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
