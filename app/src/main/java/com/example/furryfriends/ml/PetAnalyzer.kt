package com.example.furryfriends.ml

import android.content.Context
import android.graphics.Bitmap
import com.example.furryfriends.BuildConfig
import com.example.furryfriends.domain.model.Species
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
        "mammal", "vertebrate", "carnivore", "pet", "animal", "skin", "nose", "eye", "ear", "fur", "hair", "whiskers", "snout", "selfie", "smile", "photography", "photo", "portrait", "neck", "eyelash", "muscle", "mouth", "poster", "room", "mousetrap", "plastic bag", "shield", "statue", "hat"
    )

    fun analyzeImage(bitmap: Bitmap, onSuccess: (List<String>) -> Unit, onFailure: (Exception) -> Unit) {
        val image = InputImage.fromBitmap(bitmap, 0)
        
        baseLabeler.process(image)
            .addOnSuccessListener { baseLabels ->
                val sortedBase = baseLabels
                    .sortedByDescending { it.confidence }
                    .filter { !genericLabels.contains(it.text.lowercase()) }
                
                val topSpecies = sortedBase.firstOrNull()?.text ?: ""
                
                // Identify the top species from our supported types
                val matchedSpecies = Species.fromMlLabel(topSpecies)

                if (matchedSpecies != null) {
                    if (matchedSpecies == Species.DOGS || matchedSpecies == Species.CATS || matchedSpecies == Species.RABBITS) {
                        val speciesName = matchedSpecies.mlLabel
                        breedLabeler.process(image)
                            .addOnSuccessListener { breedLabels ->
                                val breed = breedLabels.firstOrNull()?.text
                                if (breed != null && !genericLabels.contains(breed.lowercase())) {
                                    onSuccess(listOf(breed))
                                } else {
                                    onSuccess(listOf(speciesName))
                                }
                            }
                            .addOnFailureListener {
                                onSuccess(listOf(speciesName))
                            }
                    } else {
                        // For Birds and Horses
                        onSuccess(listOf(matchedSpecies.mlLabel))
                    }
                } else {
                    // No supported species found
                    onSuccess(emptyList())
                }
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }
}
