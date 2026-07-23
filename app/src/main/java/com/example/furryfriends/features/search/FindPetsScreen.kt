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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.furryfriends.R
import com.example.furryfriends.ui.components.FurryFriendsAppBar

@Composable
fun FindPetsScreen(
    viewModel: FindPetsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    FindPetsContent(
        modifier = modifier,
        onFindPetsClick = { viewModel.getPetData() },
        onResetResultsClick = { viewModel.clearPetData() }
    )
}

@Composable
fun FindPetsContent(
    modifier: Modifier = Modifier,
    onFindPetsClick: () -> Unit,
    onResetResultsClick: () -> Unit
) {
    Column(modifier = modifier
        .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FurryFriendsAppBar(titleText = stringResource(R.string.find_pets_screen_title))

        Row {
            Button(
                onClick = onFindPetsClick,
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp)
            ) {
                Text(
                    "Find pets"
                )
            }
            Button(
                onClick = onResetResultsClick,
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

@Preview(showBackground = true)
@Composable
fun FindPetsScreenPreview() {
    FindPetsContent(
        onFindPetsClick = {},
        onResetResultsClick = {}
    )
}
