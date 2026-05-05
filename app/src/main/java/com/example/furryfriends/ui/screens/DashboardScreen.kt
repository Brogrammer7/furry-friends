package com.example.furryfriends.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.furryfriends.R
import com.example.furryfriends.ui.components.CustomText

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.dashboard_screen_background),
            contentDescription = null,
            modifier = Modifier
                .padding(top = 48.dp, bottom = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = 5.dp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = RoundedCornerShape(16.dp)
                )
        )

        CustomText(
            text = "We hope you find your next furry bundle of jou here",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Button(
            //TODO Add caching for saved pets list
            onClick = {
                Toast.makeText(context, R.string.feature_coming_soon, Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.view_your_saved_pets_list)
            )
        }

    }
}

@Preview
@Composable
fun DashboardScreenPreview() {
    DashboardScreen()
}
