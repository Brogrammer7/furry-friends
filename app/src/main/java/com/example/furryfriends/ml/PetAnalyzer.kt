package com.example.furryfriends.ml

import android.content.Context
import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PetAnalyzer @Inject constructor(@ApplicationContext context: Context) {
    private val labeler = ImageLabeling.getClient(
        ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.6f)
            .build()
    )

    //Prevent ML from displaying unnecessary or nonsensical pet descriptors
    private val genericLabels = setOf(
        "Mammal", "Vertebrate", "Carnivore", "Pet", "Animal", 
        "Skin", "Nose", "Eye", "Ear", "Fur", "Whiskers", "Snout",
        "Selfie", "Smile", "Photography", "Photo", "Portrait", "Neck",
        "Canidae", "Felidae", "Companion dog", "Street dog"
    )

    fun analyzeImage(bitmap: Bitmap, onSuccess: (List<String>) -> Unit, onFailure: (Exception) -> Unit) {
        val image = InputImage.fromBitmap(bitmap, 0)
        labeler.process(image)
            .addOnSuccessListener { labels ->
                val sortedLabels = labels.sortedByDescending { it.confidence }
                
                val filteredResults = mutableListOf<String>()
                var dogDetected = false
                var catDetected = false

                for (label in sortedLabels) {
                    val text = label.text
                    
                    // Skip generic noise
                    if (genericLabels.contains(text)) continue
                    
                    // Mutual exclusion: if we have a high confidence dog, don't add cat (and vice-versa)
                    if (text.contains("Dog", ignoreCase = true)) {
                        if (catDetected) continue
                        dogDetected = true
                    }
                    if (text.contains("Cat", ignoreCase = true)) {
                        if (dogDetected) continue
                        catDetected = true
                    }

                    filteredResults.add(text)
                    if (filteredResults.size >= 2) break
                }

                onSuccess(filteredResults)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }
}
