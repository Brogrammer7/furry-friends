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
        "Mammal", "Vertebrate", "Carnivore", "Pet", "Animal", 
        "Skin", "Nose", "Eye", "Ear", "Fur", "Hair", "Whiskers", "Snout",
        "Selfie", "Smile", "Photography", "Photo", "Portrait", "Neck"
    )

    fun analyzeImage(bitmap: Bitmap, onSuccess: (List<String>) -> Unit, onFailure: (Exception) -> Unit) {
        val image = InputImage.fromBitmap(bitmap, 0)
        
        // Step 1: Run base analysis to find the general species
        baseLabeler.process(image)
            .addOnSuccessListener { baseLabels ->
                val sortedBase = baseLabels
                    .sortedByDescending { it.confidence }
                    .filter { !genericLabels.contains(it.text) }
                
                val topSpecies = sortedBase.firstOrNull()?.text ?: ""
                
                // Step 2: If it's a Dog or Cat, try to get the specific Breed
                val isDog = topSpecies.contains("Dog", ignoreCase = true) || 
                            topSpecies.contains("Puppy", ignoreCase = true) ||
                            topSpecies.contains("Canidae", ignoreCase = true)
                val isCat = topSpecies.contains("Cat", ignoreCase = true) || 
                            topSpecies.contains("Kitten", ignoreCase = true) ||
                            topSpecies.contains("Felidae", ignoreCase = true)

                if (isDog || isCat) {
                    val species = if (isDog) "Dog" else "Cat"
                    breedLabeler.process(image)
                        .addOnSuccessListener { breedLabels ->
                            val breed = breedLabels.firstOrNull()?.text
                            if (breed != null) {
                                // Combine breed with species (e.g., "Golden Retriever dog")
                                // unless the breed name already includes the species.
                                val result = if (breed.contains(species, ignoreCase = true)) {
                                    breed
                                } else {
                                    "$breed ${species.lowercase()}"
                                }
                                onSuccess(listOf(result))
                            } else {
                                // If custom breed model fails to find a specific breed, return the failure message
                                onSuccess(listOf("Machine Learning detects a ${species.lowercase()} here, but couldn't discern its breed"))
                            }
                        }
                        .addOnFailureListener {
                            // If TFLite model is missing or fails, return the failure message
                            onSuccess(listOf("Machine Learning detects a ${species.lowercase()} here, but couldn't discern its breed"))
                        }
                } else {
                    // For horses, birds, rabbits, etc., return a message that we couldn't detect the specific breed
                    if (topSpecies.isNotEmpty()) {
                        onSuccess(listOf("Machine Learning detects a ${topSpecies.lowercase()} here, but couldn't discern its breed"))
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
