package com.example.furryfriends.features.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.furryfriends.R
import com.example.furryfriends.ui.components.FurryFriendsAppBar

@Composable
fun FindPetsScreen(
    viewModel: FindPetsViewModel = viewModel(),
    modifier: Modifier
) {
    val uiState by viewModel.petsUiState.collectAsState()

    Column(modifier = modifier
        .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FurryFriendsAppBar(titleText = stringResource(R.string.find_pets_screen_title))

        Row {
            Button(
                onClick = { viewModel.getPetData() },
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp)
            ) {
                Text(
                    "Find pets"
                )
            }
            Button(
                onClick = { viewModel.clearPetData() },
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp)
            ) {
                Text(
                    "Reset Results"
                )
            }
        }

        HorizontalDivider()

        
    }

}
