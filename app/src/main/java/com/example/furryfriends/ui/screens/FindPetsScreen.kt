package com.example.furryfriends.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.furryfriends.R
import com.example.furryfriends.ui.viewmodels.FindPetsViewModel

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
                            .padding(top = 8.dp, bottom = 8.dp, end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Column (
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AsyncImage(
                                model = animals.attributes.pictureThumbnailUrl ?: R.drawable.no_image_icon,
                                contentDescription = "",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.padding(top = 8.dp)
                                    .size(120.dp)
                            )
                            Text(
                                text = animals.attributes.name,
                                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Text(
                                text = animals.attributes.breedPrimary,
                                style = TextStyle(fontSize = 12.sp),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            //Ignore warning below because it's possible for ageString to be null
                            if (animals.attributes.ageString != null) {
                                Text(
                                    text = animals.attributes.ageString,
                                    style = TextStyle(fontSize = 12.sp),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                        }
                        ShareLinkButton(
                            linkUrl = animals.attributes.pictureThumbnailUrl,
                            petName = animals.attributes.name,
                            petBreed = animals.attributes.breedPrimary,
                            petAge = animals.attributes.ageString
                        )
                    }
                    HorizontalDivider()
                }
            }
        }

    }

}

@Composable
fun ShareLinkButton(
    label: String = "Share me!",
    linkUrl: String?,
    petName: String?,
    petBreed: String?,
    petAge: String?,
    subject: String? = "Give this fur baby a home:", // optional email subject
    chooserTitle: String = "Share via",
    modifier: Modifier = Modifier,
    ) {
    val context = LocalContext.current

    Button(onClick = {
        // Handle null URLs
        if (linkUrl == null) return@Button

        // Prepare the share message
        val shareMessage = buildString {
            subject?.let { append("$it\n") }
            petName?.let { append("$it\n") }
            petBreed?.let { append("$it\n") }
            petAge?.let { append("$it Old\n") }
            append(linkUrl)
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareMessage)
            subject?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
        }

        val chooser = Intent.createChooser(intent, chooserTitle)
        // If context is not Activity, need FLAG_ACTIVITY_NEW_TASK
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }) {
        Text(label)
    }
}
