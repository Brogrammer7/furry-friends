package com.example.furryfriends.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat.startActivity
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.furryfriends.R
import com.example.furryfriends.network.Species
import com.example.furryfriends.ui.viewmodels.SearchPetsViewModel
import java.util.Locale

@Composable
fun SearchPetsScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchPetsViewModel = viewModel()
) {
    val uiState by viewModel.searchUiState.collectAsState()

    val zipIntState by viewModel.zipState.collectAsState()
    val zipErrorState by viewModel.zipError.collectAsState()
    val zipText = if (zipIntState == -1) "" else zipIntState.toString()

    Column(modifier = modifier
        .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FurryFriendsAppBar(titleText = stringResource(R.string.find_pets_screen_title))

        OutlinedTextField(
            value = zipText,
            onValueChange = { viewModel.updateZipInput(it) },
            label = { Text("Enter your ZIP Code") },
            isError = zipErrorState,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Row (modifier = Modifier.padding(bottom = 8.dp)) {
            Button(
                onClick = { viewModel.searchPetData(Species.CATS.type) },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    "Search pets"
                )
            }
            TextButton (
                onClick = { viewModel.clearZip()
                    viewModel.clearSearchData() },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    "Clear Results"
                )
            }
        }

        HorizontalDivider()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp)
        ) {
            val searchList = uiState.items?.data ?: emptyList()
            val includedList = uiState.items?.included
            items(
                items = searchList,
                key = { it.attributes.name ?: searchList.indexOf(it) }
            ) { animals ->
                animals.let {
                    // get first org relationship id for this resource (if any)
                    val orgRelId = animals.relationships.orgs?.data?.firstOrNull()?.id
                    // find included org by id and type "orgs"
                    val orgIncluded = includedList?.find { it.id == orgRelId && it.type == "orgs" }

                    Row (
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
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
                            ProperCaseText(animals.attributes.name)

                            orgIncluded?.attributes?.let {
                                Text(
                                    text = it.name!!,
                                    textAlign = TextAlign.Center,
                                    style = TextStyle(fontSize = 12.sp),
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                Text(
                                    text = it.city!! + ", " + it.state!!.uppercase(Locale.getDefault()),
                                    textAlign = TextAlign.Center,
                                    style = TextStyle(fontSize = 10.sp),
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }

                        Column (
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),         // <- fill same height as left column
                            verticalArrangement = Arrangement.SpaceEvenly,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CustomModalOnButtonClick() {
                                ProperCaseText(animals.attributes.name, 20.sp)
                                Text(
                                    text = animals.attributes.ageString ?: "(Age Unknown)",
                                    textAlign = TextAlign.Center,
                                    style = TextStyle(fontSize = 12.sp),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )

                                Text(
                                    text = "Available at:"
                                )
                                orgIncluded?.attributes?.let {
                                    Text(
                                        text = it.name!!,
                                        textAlign = TextAlign.Start,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                    Text(
                                        text = it.street!!,
                                        textAlign = TextAlign.Start,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                    Text(
                                        text = it.city!! + ", " + it.state!!.uppercase(Locale.getDefault()),
                                        textAlign = TextAlign.Start,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                    SetClickableContactInfo(it.phone, it.url)
                                }
                            }

                            ShareLinkButton(
                                linkUrl = animals.attributes.pictureThumbnailUrl,
                                petName = animals.attributes.name,
                                petBreed = animals.attributes.breedPrimary,
                            )
                        }
                    }
                    HorizontalDivider()
                }
            }
        }

    }

}

//Custom components:
@Composable
fun ProperCaseText(input: String?, fontSize: TextUnit = 18.sp) {
        val properCase = input
            ?.lowercase(Locale.getDefault())
            ?.split("\\s+".toRegex())
            ?.joinToString(" ") { word ->
                word.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                }
            }

        Text(
            text = properCase ?: "Name error",
            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = fontSize),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
}

@Composable
fun CustomModalOnButtonClick(
    modifier: Modifier = Modifier,
    title: String? = null,
    onDismiss: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    var open by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(onClick = { open = true }) { Text("More info") }
    }

    if (open) {
        Dialog(onDismissRequest = { open = false; onDismiss() }) {
            Box(
                modifier
                    .widthIn(max = 360.dp)
                    .background(Color.Gray, shape = RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    if (title != null) {
                        Text(title, style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.height(8.dp))
                    Column(content = content) // place user-supplied composables here
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.align(Alignment.End)) {
                        TextButton(onClick = { open = false; onDismiss() }) { Text("Close") }
                    }
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

@Composable
fun SetClickableContactInfo(phone: String?, url: String?) {
    val ctx = LocalContext.current

    phone?.let { value ->
        val interactionSource = remember { MutableInteractionSource() }
        Text(
            text = value,
            textAlign = TextAlign.Start,
            color = Color.White,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .clickable(
                    interactionSource = interactionSource,
                    onClick = {
                        val telUri = "tel:${value.filter { it.isDigit() || it == '+' }}".toUri()
                        val intent = Intent(Intent.ACTION_DIAL, telUri)
                        if (intent.resolveActivity(ctx.packageManager) != null) {
                            startActivity(ctx, intent, null)
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
            style = TextStyle(fontSize = 12.sp),
            color = Color(0xFF1E88E5),
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .clickable(
                    interactionSource = interactionSource,
                    onClick = {
                        val fixed = if (value.startsWith("http://") || value.startsWith("https://")) value else "https://$value"
                        val webIntent = Intent(Intent.ACTION_VIEW, fixed.toUri())
                        if (webIntent.resolveActivity(ctx.packageManager) != null) {
                            startActivity(ctx, webIntent, null)
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