package com.example.furryfriends.features.dashboard

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.furryfriends.R
import com.example.furryfriends.ui.components.CustomText
import com.example.furryfriends.ui.components.PhotoOptionsDialog
import com.example.furryfriends.ui.theme.FurryFriendsTheme

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel? = null,
    onViewSavedPetsClick: () -> Unit = {}
) {
    val context = LocalContext.current
    
    // Collecting states safely for both runtime and preview
    val dashboardImageUri = viewModel?.dashboardImage?.collectAsState()?.value
    val savedPetsCount = viewModel?.savedPetsCount?.collectAsState()?.value ?: 0

    var showPhotoOptions by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                try {
                    context.contentResolver.takePersistableUriPermission(it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (_: Exception) {}
                viewModel?.setDashboardImage(it.toString())
            }
        }
    )

    DashboardContent(
        modifier = modifier,
        dashboardImageUri = dashboardImageUri,
        savedPetsCount = savedPetsCount,
        showPhotoOptions = showPhotoOptions,
        onShowPhotoOptionsChange = { showPhotoOptions = it },
        onAddPhotoClick = { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
        onDeletePhotoClick = { viewModel?.setDashboardImage(null) },
        onViewSavedPetsClick = onViewSavedPetsClick
    )
}

@Composable
fun DashboardContent(
    modifier: Modifier = Modifier,
    dashboardImageUri: String?,
    savedPetsCount: Int,
    showPhotoOptions: Boolean,
    onShowPhotoOptionsChange: (Boolean) -> Unit,
    onAddPhotoClick: () -> Unit,
    onDeletePhotoClick: () -> Unit,
    onViewSavedPetsClick: () -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.dashboard_header),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(top = 16.dp, bottom = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (dashboardImageUri != null) {
                        AsyncImage(
                            model = dashboardImageUri,
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 400.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                        )
                        IconButton(
                            onClick = { onShowPhotoOptionsChange(true) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 4.dp, end = 4.dp)
                                .size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = stringResource(R.string.photo_options),
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                        shape = CircleShape
                                    )
                                    .padding(4.dp)
                            )
                        }
                    } else {
                        Image(
                            painter = painterResource(R.drawable.dashboard_screen_background),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                        )
                    }
                }

                CustomText(
                    text = stringResource(R.string.here_to_help),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                if (dashboardImageUri == null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onAddPhotoClick,
                        modifier = Modifier.fillMaxWidth(0.9f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = stringResource(R.string.add_a_photo_of_your_pet), maxLines = 1)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                val buttonModifier = Modifier.fillMaxWidth(0.9f)
                val buttonShape = RoundedCornerShape(12.dp)

                if (savedPetsCount > 0) {
                    Button(
                        onClick = onViewSavedPetsClick,
                        modifier = buttonModifier,
                        shape = buttonShape
                    ) {
                        Text(text = stringResource(R.string.view_your_saved_pets) + " ($savedPetsCount)", maxLines = 1)
                    }
                } else {
                    Button(
                        onClick = onViewSavedPetsClick,
                        enabled = false,
                        modifier = buttonModifier,
                        shape = buttonShape
                    ) {
                        Text(text = stringResource(R.string.view_your_saved_pets) + " ($savedPetsCount)", maxLines = 1)
                    }
                    Text(
                        text = stringResource(R.string.find_new_pets_prompt),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp).padding(horizontal = 32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showPhotoOptions) {
        PhotoOptionsDialog(
            onDismiss = { onShowPhotoOptionsChange(false) },
            onReplace = {
                onAddPhotoClick()
                onShowPhotoOptionsChange(false)
            },
            onDelete = {
                onDeletePhotoClick()
                onShowPhotoOptionsChange(false)
            }
        )
    }
}

@Preview(showBackground = true, name = "Empty State")
@Composable
fun DashboardScreenEmptyPreview() {
    FurryFriendsTheme {
        Surface {
            DashboardContent(
                dashboardImageUri = null,
                savedPetsCount = 0,
                showPhotoOptions = false,
                onShowPhotoOptionsChange = {},
                onAddPhotoClick = {},
                onDeletePhotoClick = {},
                onViewSavedPetsClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "With Pets and Photo")
@Composable
fun DashboardScreenPopulatedPreview() {
    FurryFriendsTheme {
        Surface {
            DashboardContent(
                dashboardImageUri = "https://example.com/pet.jpg",
                savedPetsCount = 5,
                showPhotoOptions = false,
                onShowPhotoOptionsChange = {},
                onAddPhotoClick = {},
                onDeletePhotoClick = {},
                onViewSavedPetsClick = {}
            )
        }
    }
}
