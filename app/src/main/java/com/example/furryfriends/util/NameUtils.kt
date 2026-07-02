package com.example.furryfriends.util

import java.util.Locale

/* The string input for pet names needs extensive cleanup because many shelters use inappropriate characters and phrases unrelated to the pet's actual name. This returns a clean name for the pet without any additional nonsense.
 */
fun formatPetName(input: String?): String {
    val cleanedName = input
        // remove phrases: "courtesy post" and "adopt me" (any case, allows multiple whitespace)
        ?.replace(Regex("\\bcourtesy\\s+post\\b", RegexOption.IGNORE_CASE), "")
        ?.replace(Regex("\\badopt\\s+me\\b", RegexOption.IGNORE_CASE), "")
        // remove digits and punctuation/symbols (keep letters and whitespace)
        ?.replace(Regex("[^\\p{L}\\s]"), "")
        // collapse whitespace and trim
        ?.replace("\\s+".toRegex(), " ")
        ?.trim()

    val properCase = cleanedName
        ?.lowercase(Locale.getDefault())
        ?.split("\\s+".toRegex())
        ?.joinToString(" ") { word ->
            word.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
        }

    return properCase?.ifEmpty { "Name error" } ?: "Name error"
}
