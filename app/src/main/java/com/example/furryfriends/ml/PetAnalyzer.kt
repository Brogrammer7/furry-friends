package com.example.furryfriends.ml

import android.content.Context
import android.graphics.Bitmap
import com.example.furryfriends.BuildConfig
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PetAnalyzer @Inject constructor(@ApplicationContext context: Context) {
    
    // 1. Base Labeler: Identifies broad species (Horse, Bird, Rabbit, etc.)
    private val baseLabeler = ImageLabeling.getClient(
        ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.5f)
            .build()
    )

    // 2. Custom TFLite Labeler: Only for Dog/Cat Breed identification
    private val breedModel = LocalModel.Builder()
        .setAssetFilePath(BuildConfig.PET_BREED_MODEL_FILE)
        .build()

    private val breedLabeler = ImageLabeling.getClient(
        CustomImageLabelerOptions.Builder(breedModel)
            .setConfidenceThreshold(0.2f)
            .setMaxResultCount(1)
            .build()
    )

    // Noise labels to filter out from the base model
    private val genericLabels = setOf(
        "mammal", "vertebrate", "carnivore", "pet", "animal", 
        "skin", "nose", "eye", "ear", "fur", "hair", "whiskers", "snout",
        "selfie", "smile", "photography", "photo", "portrait", "neck",
        "eyelash", "muscle", "mouth", "poster", "room", "mousetrap", "plastic bag", "shield"
    )

    fun analyzeImage(bitmap: Bitmap, onSuccess: (List<String>) -> Unit, onFailure: (Exception) -> Unit) {
        val image = InputImage.fromBitmap(bitmap, 0)
        
        // Step 1: Run base analysis to find the general species
        baseLabeler.process(image)
            .addOnSuccessListener { baseLabels ->
                val sortedBase = baseLabels
                    .sortedByDescending { it.confidence }
                    .filter { !genericLabels.contains(it.text.lowercase()) }
                
                val topSpecies = sortedBase.firstOrNull()?.text ?: ""
                
                // Step 2: If it's a Dog, Cat, or Rabbit, try to get the specific Breed
                val isDog = topSpecies.contains("Dog", ignoreCase = true) || 
                            topSpecies.contains("Puppy", ignoreCase = true) ||
                            topSpecies.contains("Canidae", ignoreCase = true)
                val isCat = topSpecies.contains("Cat", ignoreCase = true) || 
                            topSpecies.contains("Kitten", ignoreCase = true) ||
                            topSpecies.contains("Felidae", ignoreCase = true)
                val isRabbit = topSpecies.contains("Rabbit", ignoreCase = true) || 
                               topSpecies.contains("Hare", ignoreCase = true) ||
                               topSpecies.contains("Leporidae", ignoreCase = true)

                if (isDog || isCat || isRabbit) {
                    val species = when {
                        isDog -> "Dog"
                        isCat -> "Cat"
                        else -> "Rabbit"
                    }
                    breedLabeler.process(image)
                        .addOnSuccessListener { breedLabels ->
                            val breed = breedLabels.firstOrNull()?.text
                            // Apply noise filter to the breed result as well
                            if (breed != null && !genericLabels.contains(breed.lowercase())) {
                                // Breed found: show ONLY the breed name as requested
                                onSuccess(listOf(breed))
                            } else {
                                // No breed found or filtered: show the species name
                                onSuccess(listOf(species))
                            }
                        }
                        .addOnFailureListener {
                            // On failure: fallback to species name
                            onSuccess(listOf(species))
                        }
                } else {
                    // For horses, birds, etc., just return the base species name
                    if (topSpecies.isNotEmpty()) {
                        onSuccess(listOf(topSpecies))
                    } else {
                        onSuccess(emptyList())
                    }
                }
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }
}
