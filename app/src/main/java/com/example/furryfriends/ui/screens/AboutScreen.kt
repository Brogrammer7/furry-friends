package com.example.furryfriends.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.furryfriends.R
import com.example.furryfriends.ui.widgets.CustomText
import com.example.furryfriends.ui.widgets.FurryFriendsAppBar

@Composable
fun AboutScreen(
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            FurryFriendsAppBar(stringResource(R.string.about_screen_title))
        }
    ) { innerPadding ->
        // innerPadding ensures content is placed below the topBar automatically
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),      // required to avoid overlap with the top bar
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
                Image(
                    painter = painterResource(id = R.drawable.vestie_kitten),
                    contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .border(width = 2.dp, color = Color.Red, shape = CircleShape),
                    contentScale = ContentScale.Crop
                )
                CustomText(
                    text = "Dedicated to Vestie, the best rescue kitty ever",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp),
                    style = TextStyle(fontStyle = FontStyle.Italic),
                    maxLines = 2
                )
            }
            
        }
    }
}

@Preview
@Composable
fun AboutScreenPreview() {
    AboutScreen()
}
