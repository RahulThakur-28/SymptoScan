package com.rahul.symptoscan.domain.model

data class CareGuidance(
    val title: String,
    val description: String,
    val action: String
)

object CareGuidanceProvider {
    fun getGuidance(urgency: String?): CareGuidance {
        return when (urgency?.lowercase()) {
            "emergency" -> CareGuidance(
                title = "Seek Medical Care Now",
                description = "Your reported symptoms may require urgent medical evaluation.",
                action = "Go to emergency medical care or contact local emergency services."
            )
            "urgent" -> CareGuidance(
                title = "Contact a Doctor Soon",
                description = "Your symptoms may need professional medical evaluation soon.",
                action = "Arrange medical evaluation as soon as reasonably possible."
            )
            "routine" -> CareGuidance(
                title = "Consider Seeing a Doctor",
                description = "Your symptoms do not appear to indicate an immediate emergency based on the information provided, but professional evaluation may be appropriate if symptoms persist, worsen, or concern you.",
                action = "Consider contacting a doctor or healthcare professional."
            )
            "self_care" -> CareGuidance(
                title = "Self-Care & Monitor",
                description = "Based on the information provided, immediate medical evaluation may not be necessary, but continue monitoring your symptoms.",
                action = "Seek professional care if symptoms worsen, persist, or new warning signs appear."
            )
            else -> CareGuidance(
                title = "General Guidance",
                description = "Please monitor your symptoms closely.",
                action = "Consult a healthcare professional if you have concerns."
            )
        }
    }
}
