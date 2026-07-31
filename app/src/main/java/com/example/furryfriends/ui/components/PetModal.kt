package com.example.furryfriends.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.CachePolicy
import coil3.request.crossfade
import com.example.furryfriends.R
import com.example.furryfriends.model.PetDisplayItem
import java.util.Locale

@Composable
fun PetModalContent(
    petDisplayItem: PetDisplayItem
) {
    val animal = petDisplayItem.animal
    val org = petDisplayItem.organization
    if (animal.attributes.pictureThumbnailUrl != null) {
        var isModalLoading by remember { mutableStateOf(true) }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    width = 4.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(animal.attributes.pictureThumbnailUrl)
                    .crossfade(true)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .build(),
                contentDescription = stringResource(R.string.pet_image_large),
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
