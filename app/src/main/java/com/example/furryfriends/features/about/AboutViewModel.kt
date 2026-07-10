package com.example.furryfriends.features.about

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.furryfriends.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class AboutViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    val exoPlayer = ExoPlayer.Builder(context).build().apply {
        val videoUri = "android.resource://${context.packageName}/raw/vest_box"
        val mediaItem = MediaItem.fromUri(videoUri)
        setMediaItem(mediaItem)
        prepare()
    }

    val vestPhotosList = listOf(
        R.drawable.vest1,
        R.drawable.vest2,
        R.drawable.vest3,
        R.drawable.vest4,
        R.drawable.vest5,
        R.drawable.vest6,
        R.drawable.vest7,
        R.drawable.vest_b
    )

    override fun onCleared() {
        super.onCleared()
        exoPlayer.release()
    }

}