package com.example.furryfriends.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.furryfriends.R
import com.example.furryfriends.ui.components.CustomText
import com.example.furryfriends.ui.components.LocalListLazyRow
import com.example.furryfriends.ui.viewmodels.AboutViewModel

@Composable
fun AboutScreen(
    modifier: Modifier = Modifier,
    viewModel: AboutViewModel = viewModel()
) {
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
                color = Color.Cyan,
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
                style = TextStyle(fontStyle = FontStyle.Italic),
                maxLines = 2
            )
        }

    }
}

@Preview
@Composable
fun AboutScreenPreview() {
    AboutScreen()
}
