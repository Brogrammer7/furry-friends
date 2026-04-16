package com.example.furryfriends.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.furryfriends.R
import com.example.furryfriends.ui.components.CustomText
import com.example.furryfriends.ui.components.LocalListLazyRow
import com.example.furryfriends.ui.viewmodels.AboutViewModel

@Composable
fun AboutScreen(
    modifier: Modifier = Modifier,
    viewModel: AboutViewModel = viewModel()
) {
    val context = LocalContext.current
    val vesterPhotos = viewModel.vestPhotosList

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CustomText(
            text = "Chris is an avid mobile app developer who loves Android and helping pets find forever homes.",
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
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
                style = TextStyle(fontStyle = FontStyle.Italic),
                maxLines = 2
            )

            CustomVideoPlayer(
                videoUri = "android.resource://${context.packageName}/raw/vest_box",
                modifier = Modifier.fillMaxWidth()
            )
        }

    }
}

@Composable
fun CustomVideoPlayer(videoUri: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val player: Player = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(videoUri)
            setMediaItem(mediaItem)
            prepare()
        } as Player
    }

    DisposableEffect(Unit) {
        onDispose {
            player.release()
        }
    }

    Box(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { context ->
                PlayerView(context, null).apply {
                    this.player = player
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .heightIn(max = 400.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = 2.dp,
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
