package com.rahul.symptoscan.domain.model

data class AiQuestion(
    val id: String,
    val question: String,
    val options: List<String> = emptyList(),
    val type: QuestionType = QuestionType.CHOICE
)

enum class QuestionType {
    CHOICE, SCALE
}
