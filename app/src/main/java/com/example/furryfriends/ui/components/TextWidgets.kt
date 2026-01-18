package com.example.furryfriends.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun CustomText(
    modifier: Modifier = Modifier,
    text: String = "",
    fontSize: TextUnit = 14.sp,
    textAlign: TextAlign = TextAlign.Center,
    color: Color = Color.Unspecified,
    style: TextStyle = LocalTextStyle.current,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
) {
    Text(
        modifier = modifier,
        text = text,
        fontSize = fontSize,
        textAlign = textAlign,
        color = color,
        style = style,
        lineHeight = lineHeight,
        overflow = overflow,
        maxLines = maxLines,
        )
}

/* The string input for pet names needs extensive cleanup because many shelters use inappropriate characters and phrases unrelated to the pet's actual name. This returns a clean name for the pet without any additional nonsense.
 */
@Composable
fun FormatPetName(input: String?, fontSize: TextUnit = 18.sp) {
    val cleaned = input
        // remove phrases: "courtesy post" and "adopt me" (any case, allows multiple whitespace)
        ?.replace(Regex("\\bcourtesy\\s+post\\b", RegexOption.IGNORE_CASE), "")
        ?.replace(Regex("\\badopt\\s+me\\b", RegexOption.IGNORE_CASE), "")
        // remove digits and punctuation/symbols (keep letters and whitespace)
        ?.replace(Regex("[^\\p{L}\\s]"), "")
        // collapse whitespace and trim
        ?.replace("\\s+".toRegex(), " ")
        ?.trim()

    val properCase = cleaned
        ?.lowercase(Locale.getDefault())
        ?.split("\\s+".toRegex())
        ?.joinToString(" ") { word ->
            word.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
        }

    Text(
        text = properCase?.ifEmpty { "Name error" } ?: "Name error",
        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = fontSize),
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 8.dp)
    )
}

