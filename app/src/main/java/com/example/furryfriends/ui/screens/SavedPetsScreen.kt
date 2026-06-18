package com.example.furryfriends.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.furryfriends.R
import com.example.furryfriends.ui.components.CustomText
import com.example.furryfriends.ui.components.PetSearchList
import com.example.furryfriends.ui.viewmodels.SearchPetsViewModel

@Composable
fun SavedPetsScreen(
    viewModel: SearchPetsViewModel,
    modifier: Modifier = Modifier
) {
    val favoriteAnimals by viewModel.favoriteAnimalsWithOrgs.collectAsState()
    val favoritePetIds by viewModel.favoritePetIds.collectAsState()

    if (favoriteAnimals.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CustomText(
                text = stringResource(R.string.saved_pets_placeholder),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    } else {
        PetSearchList(
            modifier = modifier,
            animalsWithOrgs = favoriteAnimals,
            favoritePetIds = favoritePetIds,
            onFavoriteClick = { viewModel.toggleFavorite(it) },
        )
    }
}
