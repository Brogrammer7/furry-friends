package com.example.furryfriends.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.furryfriends.ui.viewmodels.FindPetsViewModel

@Composable
fun FindPetsScreen(
    modifier: Modifier,
) {
    val viewModel = FindPetsViewModel()
    val uiState by viewModel.petsUiState.collectAsState()

    Column(modifier = modifier
        .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Adopt one of these fur babies!"
        )

        Button(
            onClick = { viewModel.getPetData() },
        ) {
            Text(
                "Find pets"
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp)
        ) {
            val animalsList = uiState.items?.data ?: emptyList()
            items(
                items = animalsList,
                key = { it?.attributes?.name ?: animalsList.indexOf(it) }
            ) { animals ->
                animals?.let {
                    Row (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Column (
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            AsyncImage(
                                model = animals.attributes.pictureThumbnailUrl,
                                contentDescription = "",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                            )
                            Text(
                                text = animals.attributes.name
                            )
                            Divider()
                        }
                        Button(
                            onClick = {

                            }
                        ) {
                            Text(
                                "Share me with a friend!"
                            )
                        }
                    }

                }
            }
        }

    }

}

