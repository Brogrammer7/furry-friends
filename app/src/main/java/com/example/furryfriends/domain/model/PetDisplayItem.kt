package com.example.furryfriends.domain.model

import androidx.compose.runtime.Immutable

/**
 * A stable data class used for displaying pet information in the UI.
 * Using @Immutable ensures Jetpack Compose can optimize recomposition
 * by knowing these properties won't change unexpectedly.
 */
@Immutable
data class PetDisplayItem(
    val animal: ResourceItem,
    val organization: IncludedItem?
)
