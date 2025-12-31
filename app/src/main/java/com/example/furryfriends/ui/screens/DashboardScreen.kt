package com.example.furryfriends.ui.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
    TopAppBar(
        title = {
            Text(titleText)
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = modifier,
    )


}