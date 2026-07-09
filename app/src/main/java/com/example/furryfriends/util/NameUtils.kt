package com.example.furryfriends.util

import java.util.Locale

// Pre-compiled Regex constants to avoid re-allocation in lists
private val COURTESY_POST_REGEX = Regex("\\bcourtesy\\s+post\\b", RegexOption.IGNORE_CASE)
private val ADOPT_ME_REGEX = Regex("\\badopt\\s+me\\b", RegexOption.IGNORE_CASE)
private val CLEANUP_REGEX = Regex("[^\\p{L}\\s]")
private val WHITESPACE_REGEX = "\\s+".toRegex()

/**
 * Cleans up and formats pet names from shelter data (many shelters use inappropriate characters and phrases unrelated to the pet's actual name).
 * Optimized to reuse pre-compiled Regex patterns.
 */
fun formatPetName(input: String?): String {
    if (input.isNullOrBlank()) return "Name error"

    val cleanedName = input
        .replace(COURTESY_POST_REGEX, "")
        .replace(ADOPT_ME_REGEX, "")
        .replace(CLEANUP_REGEX, "")
        .replace(WHITESPACE_REGEX, " ")
        .trim()

    val properCase = cleanedName
        .lowercase(Locale.getDefault())
        .split(WHITESPACE_REGEX)
        .joinToString(" ") { word ->
            word.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
        }

    return properCase.ifEmpty { "Name error" }
}
