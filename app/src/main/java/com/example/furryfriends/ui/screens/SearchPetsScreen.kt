package com.example.furryfriends.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
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
                    performSearch()
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    "Search ${selectedSpecies.type}"
                )
            }
            TextButton(
                onClick = {
                    clearResults()
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
            text = "Invalid ZIP Code entered. Please double-check your input and re-enter",
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

@Preview
@Composable
fun SearchPesScreenPreview() {
    SearchPetsScreen()
}