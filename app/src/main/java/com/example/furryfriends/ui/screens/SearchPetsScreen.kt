package com.example.furryfriends.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import kotlinx.coroutines.launch

@Composable
fun SearchPetsScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchPetsViewModel = viewModel()
) {
    val zipIntState by viewModel.zipState.collectAsState()
    val zipText = if (zipIntState == -1) "" else zipIntState.toString()
    val zipErrorState by viewModel.zipError.collectAsState()
    val invalidZipProvided by viewModel.invalidZipProvided.collectAsState()

    //Clear focus and collaps keyboard after search input
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    fun hideKeyboard() {
        scope.launch {
            keyboardController?.hide()
        }
        focusManager.clearFocus()
    }

    fun performSearch() {
        viewModel.clearSearchData()
        viewModel.searchPetData(Species.CATS.type)
        hideKeyboard()
    }

    fun clearResults() {
        viewModel.clearZip()
        viewModel.clearSearchData()
        hideKeyboard()
    }

    val selectedSpecies by viewModel.selectedSpecies.collectAsState()
    val isLoadingOn by viewModel.isLoadingOn.collectAsState()

    val itemsRetrieved by viewModel.itemsRetrieved.collectAsState()
    val searchList = itemsRetrieved?.data ?: emptyList()
    val includedList = itemsRetrieved?.included
    val animalsWithOrgs = viewModel.getAnimalsWithOrgs(searchList, includedList)

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val customPlaceHolderTitle = buildAnnotatedString {
            append(stringResource(R.string.enter_zip_code_to_find))
            withStyle(style = androidx.compose.ui.text.SpanStyle(
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.ExtraBold)
            ) {
                append(selectedSpecies.type)
            }
        }

        ZipSearchField(
            zipText = zipText,
            zipError = zipErrorState,
            onZipChange = {
                /* viewModel must turn zipText (String) into an Int, then checks for a full 5 digits and auto-runs the search */
                viewModel.processZipInput(it)
                if (it.length == 5) performSearch()
                          },
            placeHolderTitle = customPlaceHolderTitle,
            onIconPressed = { if (viewModel.checkValidZip(zipIntState)) performSearch() },
            )

        Row(modifier = Modifier.padding(bottom = 8.dp)) {
            Button(
                enabled = viewModel.checkValidZip(zipIntState),
                onClick = { performSearch() },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    stringResource(R.string.search_options)
                )
            }
            TextButton(
                onClick = { clearResults() },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    stringResource(R.string.clear_results)
                )
            }
        }

        HorizontalDivider()

        if (invalidZipProvided) 
            CustomText(
            modifier = Modifier.padding(vertical = 8.dp),
            text = stringResource(R.string.invalid_zip_entered),
            color = MaterialTheme.colorScheme.error
        )

        itemsRetrieved?.meta?.countReturned?.let { count ->
            CustomText(
                modifier = Modifier.padding(vertical = 8.dp),
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
                modifier = Modifier.padding(vertical = 16.dp),
                text = "Finding your next pet! \uD83D\uDC31\uD83D\uDC36"
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
    zipError: Boolean,
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


@Preview
@Composable
fun SearchPesScreenPreview() {
    SearchPetsScreen()
}