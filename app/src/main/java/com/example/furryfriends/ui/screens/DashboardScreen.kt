package com.example.furryfriends.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.furryfriends.R

@Composable
fun DashboardScreen(
    titleText: String,
    modifier: Modifier = Modifier
) {
    FurryFriendsAppBar(
        titleText = stringResource(R.string.dashboard_screen_title)
    )
}

@Composable
fun FurryFriendsAppBar(
    titleText: String,
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        title = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = titleText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    )

}


@Preview
@Composable
fun DashboardScreenPreview() {
    DashboardScreen(
        titleText = stringResource(R.string.dashboard_screen_title))
}