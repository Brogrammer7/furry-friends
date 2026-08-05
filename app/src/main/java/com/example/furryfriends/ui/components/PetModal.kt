package com.example.furryfriends.ui.components

import android.graphics.Bitmap
import android.speech.tts.TextToSpeech
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.crossfade
import coil3.toBitmap
import com.example.furryfriends.R
import com.example.furryfriends.domain.model.PetDisplayItem
import com.example.furryfriends.domain.model.Species
import com.example.furryfriends.viewmodel.PetMlViewModel
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun PetModalContent(
    petDisplayItem: PetDisplayItem
) {
    val animal = petDisplayItem.animal
    val org = petDisplayItem.organization
    val mlViewModel: PetMlViewModel = hiltViewModel()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val flashAlpha = remember { Animatable(0f) }

    val expectedSpecies = remember(animal.type) { Species.fromType(animal.type) }

    var ttsInitialized by remember { mutableStateOf(false) }
    val tts = remember {
        TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsInitialized = true
            }
        }
    }

    // Clear labels when the modal content is disposed (modal closed)
    DisposableEffect(Unit) {
        onDispose {
            mlViewModel.clearLabels()
            tts.stop()
            tts.shutdown()
        }
    }

    val displayText by mlViewModel.displayText.collectAsState()
    val isAnalyzing by mlViewModel.isAnalyzing.collectAsState()

    LaunchedEffect(displayText, ttsInitialized) {
        if (displayText.isNotEmpty() && ttsInitialized) {
            tts.speak(displayText, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    var petBitmap by remember { mutableStateOf<Bitmap?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                mlViewModel.clearLabels()
                tts.stop()
            }
    ) {
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
                    )
                    .clickable {
                        scope.launch {
                            repeat(2) {
                                flashAlpha.animateTo(0.2f, animationSpec = tween(300))
                                flashAlpha.animateTo(0f, animationSpec = tween(300))
                            }
                            petBitmap?.let { mlViewModel.analyzePetImage(it, expectedSpecies) }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(animal.attributes.pictureThumbnailUrl)
                        .crossfade(true)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .allowHardware(false)
                        .build(),
                    contentDescription = stringResource(R.string.pet_image_large),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    onState = { state ->
                        isModalLoading = state is AsyncImagePainter.State.Loading
                        if (state is AsyncImagePainter.State.Success) {
                            petBitmap = state.result.image.toBitmap()
                        }
                    }
                )

                // Flash overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Yellow.copy(alpha = flashAlpha.value))
                )

                if (isModalLoading || isAnalyzing) {
                    SpinningLoader(size = 48.dp)
                }

                this@Column.AnimatedVisibility(
                    visible = displayText.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = displayText,
                            color = Color.White,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
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
