package com.example.rahul.symptoscan.data.remote

// =======================
// 🔥 REQUEST MODELS
// =======================

data class GeminiRequest(
    val contents: List<RequestContent>
)

data class RequestContent(
    val parts: List<RequestPart>
)

data class RequestPart(
    val text: String
)


// =======================
// 🔥 RESPONSE MODELS
// =======================

data class GeminiResponse(
    val candidates: List<Candidate>?
)

data class Candidate(
    val content: ResponseContent?
)

data class ResponseContent(
    val parts: List<ResponsePart>?
)

data class ResponsePart(
    val text: String?
)


// =======================
// ✅ SAFE OUTPUT MODEL
// =======================

data class SymptomAnalysis(
    val conditions: List<String> = emptyList(),
    val riskLevel: String = "Unknown",
    val suggestedAction: String = "Consult a professional"
)