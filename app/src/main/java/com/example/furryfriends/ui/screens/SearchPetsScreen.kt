package com.example.furryfriends.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.graphics.SolidColor
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
import com.example.furryfriends.network.Species
import com.example.furryfriends.ui.components.CustomText
import com.example.furryfriends.ui.components.PetSearchList
import com.example.furryfriends.ui.components.SpinningLoader
import com.example.furryfriends.ui.viewmodels.SearchPetsViewModel
import com.example.furryfriends.ui.viewmodels.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SearchPetsScreen(
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel,
    viewModel: SearchPetsViewModel = viewModel()
) {
    val storedZip by settingsViewModel.zip.collectAsState()

    val zipIntState by viewModel.zipState.collectAsState()
    val zipText = if (zipIntState == -1) "" else zipIntState.toString()
    val invalidZipProvided by viewModel.invalidZipProvided.collectAsState()

    // Clear focus and collapse keyboard after search input
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    val showBottomSheet = remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    val selectedSpecies by viewModel.selectedSpecies.collectAsState()
    val isLoadingOn by viewModel.isLoadingOn.collectAsState()

    val itemsRetrieved by viewModel.itemsRetrieved.collectAsState()
    val searchList = itemsRetrieved?.data ?: emptyList()
    val includedList = itemsRetrieved?.included
    val animalsWithOrgs = viewModel.getAnimalsWithOrgs(searchList, includedList)

    fun hideKeyboard() {
        scope.launch {
            keyboardController?.hide()
        }
        focusManager.clearFocus()
    }

    fun performSearch() {
        viewModel.clearSearchData()
        viewModel.searchPetData(selectedSpecies.type)
        hideKeyboard()
    }

    fun clearResults() {
        viewModel.clearZip()
        viewModel.clearSearchData()
        hideKeyboard()
    }
    
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LaunchedEffect(storedZip) {
            if (storedZip != null) {
                delay(1000)
                viewModel.processZipInput(storedZip!!)
                performSearch()
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
                /* viewModel must turn zipText (String) into an Int and check for a full 5 digits, then it can auto-run the search */
                viewModel.processZipInput(it)
                if (it.length == 5) performSearch()
                          },
            placeHolderTitle = customPlaceHolderTitle,
            onIconPressed = { if (viewModel.checkValidZip(zipIntState)) performSearch() },
            )

        Row(modifier = Modifier.padding(bottom = 8.dp)) {
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
                    stringResource(R.string.search_options)
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
        }

        if (showBottomSheet.value)
            PetSelectionModal(
                sheetState = sheetState,
                scope = scope,
                showBottomSheet = showBottomSheet,
                viewModel = viewModel,
            )

        HorizontalDivider()

        if (invalidZipProvided) 
            CustomText(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
            text = stringResource(R.string.invalid_zip_entered),
            color = MaterialTheme.colorScheme.error
        )

        itemsRetrieved?.meta?.countReturned?.let { count ->
            CustomText(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                text = if (count >= 2) "$count ${selectedSpecies.type} found"
                else if (count == 1) "$count ${selectedSpecies.type.replace("s", "")} found"
                else "No ${selectedSpecies.type} are available in this area. Please try a different ZIP Code.",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
        }

        HorizontalDivider()

        if (isLoadingOn) {
            SpinningLoader(modifier = Modifier.padding(top = 16.dp))
            Text(
                modifier = Modifier.padding(16.dp),
                text = if (storedZip == zipText) "Using your saved ZIP Code to find your next pet! \uD83D\uDC31\uD83D\uDC36" else "Finding your next pet! \uD83D\uDC31\uD83D\uDC36",
                textAlign = TextAlign.Center
            )
        }

        Column(modifier = Modifier.background(color = MaterialTheme.colorScheme.onPrimaryContainer)
        ) {
            if (animalsWithOrgs.isEmpty()) {
                Image(
                    painter = painterResource(R.drawable.mart_dom_2),
                    contentDescription = null,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)
                )
            } else {
                PetSearchList(animalsWithOrgs)
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
    scope: CoroutineScope,
    showBottomSheet: MutableState<Boolean>,
    viewModel: SearchPetsViewModel,
    speciesList: List<Species> = Species.entries.toList()
) {
    LaunchedEffect(sheetState) { sheetState.show() }

    val selectedSpecies by viewModel.selectedSpecies.collectAsState()
    var expandedDropdown by remember { mutableStateOf(false) }

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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Filter options",
                modifier = Modifier.align(alignment = Alignment.CenterHorizontally),
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dropdown selector
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
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
                        contentDescription = "Dropdown arrow"
                    )
                }

                DropdownMenu(
                    expanded = expandedDropdown,
                    onDismissRequest = { expandedDropdown = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    speciesList.forEach { species ->
                        DropdownMenuItem(
                            text = { Text(species.type.replaceFirstChar { it.uppercase() }) },
                            onClick = {
                                viewModel.updateSelectedSpecies(species)
                                expandedDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview
@Composable
fun SearchPesScreenPreview() {
//    SearchPetsScreen(
//        settingsViewModel = TODO(),
//    )
}