package com.example.furryfriends.features.about

import androidx.annotation.OptIn
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.furryfriends.R
import com.example.furryfriends.ui.components.CopyrightText
import com.example.furryfriends.ui.components.CustomText
import com.example.furryfriends.ui.components.LocalListLazyRow

@Composable
fun AboutScreen(
    modifier: Modifier = Modifier,
    viewModel: AboutViewModel = hiltViewModel()
) {
    val vesterPhotos = viewModel.vestPhotosList

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CustomText(
            text = "Chris is an avid mobile app developer who loves Android and helping pets find forever homes.",
            lineHeight = 16.sp,
            modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
        )
        Column(
            modifier = Modifier.padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LocalListLazyRow(vesterPhotos)

            CustomText(
                text = stringResource(R.string.dedicated_to_vestie),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp),
                style = TextStyle(fontStyle = FontStyle.Italic),
                maxLines = 2
            )

            CustomVideoPlayer(
                player = viewModel.exoPlayer,
                modifier = Modifier.fillMaxWidth()
            )

            CopyrightText()

        }

    }
}

@OptIn(UnstableApi::class)
@Composable
fun CustomVideoPlayer(player: Player, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { context ->
                PlayerView(context, null).apply {
                    this.player = player
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 240.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = 3.dp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = RoundedCornerShape(16.dp)
                )
        )
    }
}

@Preview
@Composable
fun AboutScreenPreview() {
    AboutScreen()
}
