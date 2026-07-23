package com.example.furryfriends.features.savedpets

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.furryfriends.R
import com.example.furryfriends.model.PetDisplayItem
import com.example.furryfriends.ui.components.CustomText
import com.example.furryfriends.ui.components.PetSearchList
import com.example.furryfriends.features.search.SearchPetsViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.emptyFlow
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
@Composable
fun SavedPetsScreen(
    viewModel: SearchPetsViewModel,
    modifier: Modifier = Modifier
) {
    val favoriteAnimals by viewModel.favoriteAnimalsWithOrgs.collectAsState()
    val favoritePetIds by viewModel.favoritePetIds.collectAsState()

    SavedPetsContent(
        modifier = modifier,
        favoriteAnimals = favoriteAnimals,
        favoritePetIds = favoritePetIds,
        onFavoriteClick = { viewModel.toggleFavorite(it) },
        onClearAllFavorites = { viewModel.clearAllFavorites() },
        favoriteEvent = viewModel.favoriteEvent
    )
}

@OptIn(FlowPreview::class)
@Composable
fun SavedPetsContent(
    modifier: Modifier = Modifier,
    favoriteAnimals: List<PetDisplayItem>,
    favoritePetIds: Set<String>,
    onFavoriteClick: (String) -> Unit,
    onClearAllFavorites: () -> Unit,
    favoriteEvent: Flow<SearchPetsViewModel.FavoriteEvent>
) {
    val context = LocalContext.current
    val petRemovedMessage = stringResource(R.string.pet_removed)
    var showClearConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        favoriteEvent
            .debounce(400.milliseconds)
            .collect { event ->
                if (!event.isFavorite) {
                    Toast.makeText(context, petRemovedMessage, Toast.LENGTH_SHORT).show()
                }
            }
    }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text(stringResource(R.string.clear_all_saved_pets_title)) },
            text = { Text(stringResource(R.string.clear_all_saved_pets_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAllFavorites()
                        showClearConfirmation = false
                    }
                ) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) {
                    Text(stringResource(R.string.no))
                }
            }
        )
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val count = favoriteAnimals.size
        val countText = when (count) {
            1 -> "1 saved pet"
            else -> "$count saved pets"
        }

        if (favoriteAnimals.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CustomText(
                    text = stringResource(R.string.saved_pets_placeholder),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        } else {
            PetSearchList(
                modifier = Modifier.weight(1f),
                animalsWithOrgs = favoriteAnimals,
                favoritePetIds = favoritePetIds,
                onFavoriteClick = onFavoriteClick,
                onClearAllClick = { showClearConfirmation = true },
                petCountText = countText
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SavedPetsScreenPreview() {
    SavedPetsContent(
        favoriteAnimals = emptyList(),
        favoritePetIds = emptySet(),
        onFavoriteClick = {},
        onClearAllFavorites = {},
        favoriteEvent = emptyFlow()
    )
}
