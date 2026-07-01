package com.example.furryfriends.features.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.furryfriends.R
import com.example.furryfriends.ui.components.CustomText

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    savedPetsCount: Int = 0,
    onViewSavedPetsClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Dashboard",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp),
            color = MaterialTheme.colorScheme.onSurface
        )

        Image(
            painter = painterResource(R.drawable.dashboard_screen_background),
            contentDescription = null,
            modifier = Modifier
                .padding(top = 16.dp, bottom = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = 5.dp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = RoundedCornerShape(16.dp)
                )
        )

        CustomText(
            text = stringResource(R.string.here_to_help),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            BadgedBox(
                modifier = Modifier.padding(top = 24.dp, bottom = 16.dp),
                badge = {
                    if (savedPetsCount > 0) {
                        Box {
                            // Shadow layer for depth
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = Color.Black.copy(alpha = 0.3f),
                                modifier = Modifier.offset(x = 1.dp, y = 1.dp)
                            )
                            // Gradient layer for color depth
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Saved Pets Star",
                                tint = Color.White,
                                modifier = Modifier
                                    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                                    .drawWithCache {
                                        val brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color(0xFFFFE259), // Light Gold
                                                Color(0xFFFFA751)  // Deep Gold
                                            )
                                        )
                                        onDrawWithContent {
                                            drawContent()
                                            drawRect(brush = brush, blendMode = BlendMode.SrcIn)
                                        }
                                    }
                            )
                        }
                    }
                }
            ) {
                Button(
                    onClick = {
                        onViewSavedPetsClick()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.view_your_saved_pets)
                    )
                }
            }
        }


    }
}

@Preview
@Composable
fun DashboardScreenPreview() {
    DashboardScreen()
}
