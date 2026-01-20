package com.example.furryfriends.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.furryfriends.R
import com.example.furryfriends.network.Species
import com.example.furryfriends.ui.components.CustomText
import com.example.furryfriends.ui.components.FormatPetName
import com.example.furryfriends.ui.components.PetModalButton
import com.example.furryfriends.ui.components.ShareButton
import com.example.furryfriends.ui.components.SpinningLoader
import com.example.furryfriends.ui.viewmodels.SearchPetsViewModel
import com.example.furryfriends.ui.viewmodels.SettingsViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun SearchPetsScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchPetsViewModel = viewModel()
) {
    val isLoadingOn by viewModel.isLoadingOn.collectAsState()
    val itemsRetrieved by viewModel.itemsRetrieved.collectAsState()
    val selectedSpecies by viewModel.selectedSpecies.collectAsState()

    val zipIntState by viewModel.zipState.collectAsState()
    val zipText = if (zipIntState == -1) "" else zipIntState.toString()
    val zipErrorState by viewModel.zipError.collectAsState()
    val invalidZipProvided by viewModel.invalidZipProvided.collectAsState()

    //Clear focus and collaps keyboard after search input
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    //Grab a reference to Dark Mode for UI
    val owner = LocalViewModelStoreOwner.current
        ?: throw IllegalStateException("No ViewModelStoreOwner available")
    val settingsViewModel: SettingsViewModel = ViewModelProvider(owner).get(SettingsViewModel::class.java)
    val isDarkTheme by settingsViewModel.darkThemeEnabled.collectAsState()

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = zipText,
            onValueChange = { viewModel.processZipInput(it) },
            label = { Text("Enter your ZIP Code") },
            isError = zipErrorState,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Row(modifier = Modifier.padding(bottom = 8.dp)) {
            Button(
                enabled = viewModel.checkValidZip(zipIntState),
                onClick = {
                    viewModel.clearSearchData()
                    viewModel.searchPetData(Species.CATS.type)
                    scope.launch {
                        keyboardController?.hide()
                    }
                    focusManager.clearFocus()
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    "Search ${selectedSpecies.type}"
                )
            }
            TextButton(
                onClick = {
                    viewModel.clearZip()
                    viewModel.clearSearchData()
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    "Clear Results"
                )
            }
        }

        HorizontalDivider()

        if (invalidZipProvided) 
            CustomText(
            modifier = Modifier.padding(vertical = 8.dp),
            text = "Invalid ZIP Code entered. Please double-check your input and re-enter"
        )

        itemsRetrieved?.meta?.countReturned?.let { count ->
            CustomText(
                modifier = Modifier.padding(vertical = 8.dp),
                text = if (count >= 2) "$count ${selectedSpecies.type} found"
                else if (count == 1) "$count ${selectedSpecies.type.replace("s", "")} found"
                else "No ${selectedSpecies.type} are available in this area. Please try a different ZIP Code.",
            )
        }

        HorizontalDivider()

        if (isLoadingOn) {
            SpinningLoader(modifier = Modifier.padding(top = 16.dp))
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = "Finding your next pet! \uD83D\uDC31\uD83D\uDC36"
            )
        }

        val searchList = itemsRetrieved?.data ?: emptyList()
        val includedList = itemsRetrieved?.included

        if (searchList.isEmpty() || includedList.isNullOrEmpty()) {
            Image(
                painter = painterResource(R.drawable.mart_dom_2),
                contentDescription = null,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp)
            ) {

                items(
                    items = searchList,
                    key = { it.attributes.name ?: searchList.indexOf(it) }
                ) { animal ->
                    animal.let {
                        val org = viewModel.getOrganizationForAnimal(animal, includedList)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .padding(top = 8.dp, bottom = 8.dp, end = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AsyncImage(
                                    model = animal.attributes.pictureThumbnailUrl
                                        ?: R.drawable.no_image_icon,
                                    contentDescription = "",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.padding(top = 8.dp)
                                        .size(130.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                )
                                FormatPetName(input = animal.attributes.name, fontSize = 16.sp)

                                org?.attributes?.let {
                                    Text(
                                        text = it.name!!,
                                        textAlign = TextAlign.Center,
                                        color = if (!isDarkTheme) Color.Green else Color.Yellow,
                                        style = TextStyle(fontSize = 10.sp),
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                    )
                                    Text(
                                        text = it.city!! + ", " + it.state!!.uppercase(Locale.getDefault()),
                                        textAlign = TextAlign.Center,
                                        style = TextStyle(fontSize = 10.sp),
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),         // <- fill same height as left column
                                verticalArrangement = Arrangement.SpaceEvenly,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                PetModalButton {
                                    FormatPetName(animal.attributes.name, 22.sp)
                                    Text(
                                        text = animal.attributes.ageString ?: "(Age Unknown)",
                                        textAlign = TextAlign.Center,
                                        style = TextStyle(fontSize = 12.sp),
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )

                                    Text(
                                        text = "Contact info:"
                                    )
                                    org?.attributes?.let {
                                        it.name?.let { value ->
                                            Text(
                                                text = value,
                                                textAlign = TextAlign.Start,
                                                modifier = Modifier.padding(horizontal = 16.dp)
                                            )
                                        }
                                        it.street?.let { value ->
                                            Text(
                                                text = value,
                                                textAlign = TextAlign.Start,
                                                modifier = Modifier.padding(horizontal = 16.dp)
                                            )
                                        }
                                        Text(
                                            text = it.city + ", " + it.state?.uppercase(Locale.getDefault()),
                                            textAlign = TextAlign.Start,
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        )

                                        SetClickableContactInfo(
                                            phone = it.phone,
                                            url = it.url
                                        )

                                        it.adoptionProcess.let { value ->
                                            Text(
                                                text = "Adoption process:\n$value",
                                                textAlign = TextAlign.Start,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(horizontal = 16.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                ShareButton(
                                    label = "Share me!",
                                    linkUrl = org?.attributes?.url,
                                    petName = animal.attributes.name,
                                    petBreed = animal.attributes.breedPrimary,
                                    pictureUrl = animal.attributes.pictureThumbnailUrl
                                )
                            }

                        }
                        HorizontalDivider()
                    }
                }
            }

        }
    }
}
//Custom components:

@Composable
fun SetClickableContactInfo(
    modifier: Modifier = Modifier,
    phone: String?,
    url: String?
) {
    val ctx = LocalContext.current

    // A simple launcher wrapper that uses the Activity Result API to start an arbitrary Intent.
    // It does not expect a result; it just starts the intent via an ActivityResultRegistry owner.
    val activityStarter = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { /* no-op: we don't need a result */ }

    fun canResolve(intent: Intent): Boolean =
        intent.resolveActivity(ctx.packageManager) != null

    phone?.let { value ->
        val interactionSource = remember { MutableInteractionSource() }
        Text(
            text = value,
            textAlign = TextAlign.Start,
            color = Color.White,
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                .clickable(
                    interactionSource = interactionSource,
                    onClick = {
                        val telUri = "tel:${value.filter { it.isDigit() || it == '+' }}".toUri()
                        val intent = Intent(Intent.ACTION_DIAL, telUri)
                        if (canResolve(intent)) {
                            activityStarter.launch(intent)
                        }
                    }
                )
        )
    }

    url?.let { value ->
        val interactionSource = remember { MutableInteractionSource() }
        Text(
            text = value,
            textAlign = TextAlign.Start,
            color = Color.Blue,
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                .clickable(
                    interactionSource = interactionSource,
                    onClick = {
                        val fixed = if (value.startsWith("http://") || value.startsWith("https://")) value else "https://$value"
                        val webIntent = Intent(Intent.ACTION_VIEW, fixed.toUri())
                        if (canResolve(webIntent)) {
                            activityStarter.launch(webIntent)
                        }
                    }
                )
        )
    }
}

@Preview
@Composable
fun SearchPesScreenPreview() {
    SearchPetsScreen()
}