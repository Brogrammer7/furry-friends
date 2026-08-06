package com.example.furryfriends.features.search

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.furryfriends.R
import com.example.furryfriends.domain.model.PetDisplayItem
import com.example.furryfriends.domain.model.SearchResponse
import com.example.furryfriends.domain.model.Species
import com.example.furryfriends.ui.components.CustomText
import com.example.furryfriends.ui.components.PetSearchList
import com.example.furryfriends.ui.components.SortModal
import com.example.furryfriends.ui.components.SpinningLoader
import com.example.furryfriends.features.settings.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
@Composable
fun SearchPetsScreen(
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel,
    viewModel: SearchPetsViewModel = viewModel()
) {
    val storedZip by settingsViewModel.zip.collectAsState()
    val zipIntState by viewModel.zipState.collectAsState()
    val invalidZipProvided by viewModel.invalidZipProvided.collectAsState()
    val currentSortOption by viewModel.currentSortOption.collectAsState()
    val selectedSpecies by viewModel.selectedSpecies.collectAsState()
    val isLoadingOn by viewModel.isLoadingOn.collectAsState()
    val favoritePetIds by viewModel.favoritePetIds.collectAsState()
    val itemsRetrieved by viewModel.itemsRetrieved.collectAsState()

    val animalsWithOrgs = remember(itemsRetrieved, currentSortOption) {
        viewModel.getAnimalsWithOrgs(itemsRetrieved?.data, itemsRetrieved?.included)
    }

    SearchPetsContent(
        modifier = modifier,
        storedZip = storedZip,
        zipIntState = zipIntState,
        invalidZipProvided = invalidZipProvided,
        currentSortOption = currentSortOption,
        selectedSpecies = selectedSpecies,
        isLoadingOn = isLoadingOn,
        favoritePetIds = favoritePetIds,
        itemsRetrieved = itemsRetrieved,
        animalsWithOrgs = animalsWithOrgs,
        onProcessZipInput = { viewModel.processZipInput(it) },
        onCheckValidZip = { viewModel.checkValidZip(it) },
        onSearchPetData = { viewModel.searchPetData(it) },
        onClearSearchData = { viewModel.clearSearchData() },
        onClearZip = { viewModel.clearZip() },
        onToggleFavorite = { viewModel.toggleFavorite(it) },
        onUpdateSortOption = { viewModel.updateSortOption(it) },
        onUpdateSelectedSpecies = { viewModel.updateSelectedSpecies(it) },
        newResultsEvent = viewModel.newResultsEvent,
        favoriteEvent = viewModel.favoriteEvent
    )
}

@OptIn(FlowPreview::class)
@Composable
fun SearchPetsContent(
    modifier: Modifier = Modifier,
    storedZip: String?,
    zipIntState: Int,
    invalidZipProvided: Boolean,
    currentSortOption: SortOption,
    selectedSpecies: Species,
    isLoadingOn: Boolean,
    favoritePetIds: Set<String>,
    itemsRetrieved: SearchResponse?,
    animalsWithOrgs: List<PetDisplayItem>,
    onProcessZipInput: (String) -> Unit,
    onCheckValidZip: (Int) -> Boolean,
    onSearchPetData: (String) -> Unit,
    onClearSearchData: () -> Unit,
    onClearZip: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    onUpdateSortOption: (SortOption) -> Unit,
    onUpdateSelectedSpecies: (Species) -> Unit,
    newResultsEvent: SharedFlow<SearchPetsViewModel.SearchResultsEvent>,
    favoriteEvent: SharedFlow<SearchPetsViewModel.FavoriteEvent>
) {
    val context = LocalContext.current
    val zipText = if (zipIntState == -1) "" else zipIntState.toString()

    // Clear focus and collapse keyboard after search input
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    val showBottomSheet = remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val showSortModal = remember { mutableStateOf(false) }

    fun hideKeyboard() {
        scope.launch {
            keyboardController?.hide()
        }
        focusManager.clearFocus()
    }

    fun performSearch(species: Species? = null) {
        onClearSearchData()
        onSearchPetData(species?.type ?: selectedSpecies.type)
        hideKeyboard()
    }

    fun clearResults() {
        onClearZip()
        onClearSearchData()
        hideKeyboard()
    }

    fun getDefaultImageForSpecies(species: Species): Int = when (species) {
        Species.CATS -> R.drawable.mart_dom_2
        Species.DOGS -> R.drawable.stock_dog
        Species.RABBITS -> R.drawable.stock_rabbit
        Species.BIRDS -> R.drawable.stock_bird
        Species.HORSES -> R.drawable.stock_horse
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LaunchedEffect(storedZip) {
            if (storedZip != null) {
                onProcessZipInput(storedZip)
                // Only auto-search if we have NO cached results
                if (itemsRetrieved == null) {
                    performSearch()
                }
            }
        }

        LaunchedEffect(zipIntState) {
            // Only auto-search if we don't have results yet
            if (itemsRetrieved == null && zipIntState != -1 && zipIntState.toString().length == 5 && storedZip != zipIntState.toString()) {
                if (onCheckValidZip(zipIntState)) {
                    performSearch()
                }
            }
        }

        val customPlaceHolderTitle = buildAnnotatedString {
            append(stringResource(R.string.enter_zip_code_to_find))
            withStyle(style = SpanStyle(
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.ExtraBold)
            ) {
                append(selectedSpecies.type)
            }
        }

        ZipSearchField(
            zipText = zipText,
            onZipChange = {
                /* viewModel must turn zipText String into an Int and check for a full 5 digits, then it can auto-run the search */
                onProcessZipInput(it)
            },
            placeHolderTitle = customPlaceHolderTitle,
            onIconPressed = { if (onCheckValidZip(zipIntState)) performSearch() },
        )

        Row(
            modifier = Modifier.padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    showBottomSheet.value = true
                    scope.launch {
                        sheetState.show()
                        sheetState.expand()
                    }
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    stringResource(R.string.change_animal)
                )
            }

            TextButton(
                enabled = zipText.isNotEmpty(),
                onClick = { clearResults() },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    stringResource(R.string.clear_results)
                )
            }

            IconButton(
                enabled = animalsWithOrgs.isNotEmpty(),
                onClick = {
                    showSortModal.value = true
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Sort,
                    contentDescription = null,
                    tint = if (animalsWithOrgs.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }
        }

        SortModal(
            showSortModal = showSortModal,
            currentSortOption = currentSortOption,
            onSortOptionSelected = onUpdateSortOption
        )

        if (showBottomSheet.value)
            PetSelectionModal(
                sheetState = sheetState,
                showBottomSheet = showBottomSheet,
                scope = scope,
                selectedSpecies = selectedSpecies,
                onUpdateSelectedSpecies = onUpdateSelectedSpecies,
                onSpeciesSelected = { newSpecies ->
                    if (onCheckValidZip(zipIntState)) {
                        performSearch(newSpecies)
                    }
                }
            )

        HorizontalDivider()

        if (invalidZipProvided)
            CustomText(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                text = stringResource(R.string.invalid_zip_entered),
                color = MaterialTheme.colorScheme.error
            )

        LaunchedEffect(Unit) {
            newResultsEvent.collect { event ->
                val response = event.response
                val speciesType = event.speciesType
                response.meta.countReturned.let { count ->
                    val petCount = if (count >= 2) "$count $speciesType found"
                    else if (count == 1) "$count ${speciesType.replace("s", "")} found"
                    else "No $speciesType available. Please try a different ZIP Code."

                    Toast.makeText(context, petCount, Toast.LENGTH_LONG).show()
                }
            }
        }

        val petAddedMessage = stringResource(R.string.pet_added_to_favorites)
        val petRemovedMessage = stringResource(R.string.pet_removed)

        LaunchedEffect(Unit) {
            favoriteEvent
                .debounce(400.milliseconds)
                .collect { event ->
                    val message = if (event.isFavorite) {
                        petAddedMessage.format(event.petName)
                    } else {
                        petRemovedMessage
                    }
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
        }

        HorizontalDivider()

        if (isLoadingOn) {
            SpinningLoader(modifier = Modifier.padding(top = 16.dp))
            Text(
                modifier = Modifier.padding(16.dp),
                text = if (storedZip == zipText) stringResource(R.string.finding_next_pet_with_saved_zip) else stringResource(R.string.finding_next_pet_no_zip),
                textAlign = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (animalsWithOrgs.isEmpty()) {
                val defaultImage = getDefaultImageForSpecies(selectedSpecies)
                Box(Modifier.background(
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = CircleShape)
                ) {
                    Image(
                        painter = painterResource(defaultImage),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 24.dp)
                            .size(300.dp)
                            .clip(CircleShape)
                    )
                }
            } else {
                PetSearchList(
                    animalsWithOrgs = animalsWithOrgs,
                    favoritePetIds = favoritePetIds,
                    onFavoriteClick = onToggleFavorite
                )
            }
        }
    }
}

@Composable
fun ZipSearchField(
    zipText: String,
    onZipChange: (String) -> Unit,
    placeHolderTitle: AnnotatedString? = null,
    onIconPressed: () -> Unit,
) {
    val textStyle = MaterialTheme.typography.bodyLarge

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = CircleShape
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .padding(start = 24.dp, end = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = zipText,
                onValueChange = onZipChange,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                textStyle = textStyle.copy(
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 24.dp)
                    .padding(end = 4.dp)
            ) { innerTextField ->
                if (zipText.isEmpty()) {
                    if (placeHolderTitle != null) {
                        Text(
                            text = placeHolderTitle,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            style = textStyle
                        )
                    }
                }
                innerTextField()
            }
        }

        IconButton(
            onClick = onIconPressed,
            modifier = Modifier
                .padding(end = 8.dp)
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.search),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun PetSelectionModal(
    sheetState: SheetState,
    showBottomSheet: MutableState<Boolean>,
    scope: CoroutineScope,
    selectedSpecies: Species,
    onUpdateSelectedSpecies: (Species) -> Unit,
    speciesList: List<Species> = Species.entries.toList(),
    onSpeciesSelected: (Species) -> Unit = {}
) {
    var expandedDropdown by remember { mutableStateOf(false) }
    var anchorWidth by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    ModalBottomSheet(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        sheetState = sheetState,
        contentColor = MaterialTheme.colorScheme.onSurface,
        containerColor = MaterialTheme.colorScheme.surface,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f),
        onDismissRequest = {
            scope.launch {
                sheetState.hide()
            }.invokeOnCompletion {
                if (!sheetState.isVisible) {
                    showBottomSheet.value = false
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.help_me_find),
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dropdown selector
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        anchorWidth = with(density) { coordinates.size.width.toDp() }
                    }
            ) {
                OutlinedButton(
                    onClick = { expandedDropdown = !expandedDropdown },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = selectedSpecies.type.replaceFirstChar { it.uppercase() },
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start
                    )
                    Icon(
                        imageVector = if (expandedDropdown)
                            Icons.Default.KeyboardArrowUp
                        else
                            Icons.Default.KeyboardArrowDown,
                        contentDescription = stringResource(R.string.dropdown_arrow)
                    )
                }

                DropdownMenu(
                    expanded = expandedDropdown,
                    onDismissRequest = { expandedDropdown = false },
                    modifier = Modifier.width(anchorWidth)
                ) {
                    speciesList.forEach { species ->
                        DropdownMenuItem(
                            text = { Text(species.type.replaceFirstChar { it.uppercase() }) },
                            onClick = {
                                onUpdateSelectedSpecies(species)
                                expandedDropdown = false
                                // Close the modal
                                scope.launch {
                                    sheetState.hide()
                                    showBottomSheet.value = false
                                    onSpeciesSelected(species)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchPetsScreenPreview() {
    SearchPetsContent(
        storedZip = "90210",
        zipIntState = 90210,
        invalidZipProvided = false,
        currentSortOption = SortOption.NONE,
        selectedSpecies = Species.CATS,
        isLoadingOn = false,
        favoritePetIds = emptySet(),
        itemsRetrieved = null,
        animalsWithOrgs = emptyList(),
        onProcessZipInput = {},
        onCheckValidZip = { true },
        onSearchPetData = {},
        onClearSearchData = {},
        onClearZip = {},
        onToggleFavorite = {},
        onUpdateSortOption = {},
        onUpdateSelectedSpecies = {},
        newResultsEvent = MutableSharedFlow<SearchPetsViewModel.SearchResultsEvent>(),
        favoriteEvent = MutableSharedFlow()
    )
}
