package com.example.furryfriends.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.CachePolicy
import coil3.request.crossfade
import com.example.furryfriends.R
import com.example.furryfriends.model.PetDisplayItem
import com.example.furryfriends.util.formatPetName
import java.util.Locale

@Composable
fun PetCard(
    petDisplayItem: PetDisplayItem,
    showModal: MutableState<Boolean>,
    favoritePetIds: Set<String>,
    onFavoriteClick: (String) -> Unit
) {
    val animal = petDisplayItem.animal
    val org = petDisplayItem.organization
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
            contentColor = MaterialTheme.colorScheme.surfaceVariant
        )
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
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { showModal.value = true },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(animal.attributes.pictureThumbnailUrl ?: R.drawable.no_image_icon)
                            .crossfade(true)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .build(),
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
