import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from "https://esm.sh/@supabase/supabase-js@2"

const GEMINI_API_KEY = Deno.env.get("GEMINI_API_KEY")
const SUPABASE_URL = Deno.env.get("SUPABASE_URL")
const SUPABASE_SERVICE_ROLE_KEY = Deno.env.get(
  "SUPABASE_SERVICE_ROLE_KEY"
)

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers":
    "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
  "Content-Type": "application/json",
}

function jsonResponse(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: corsHeaders,
  })
}

serve(async (req) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders })
  }

  const supabaseClient = createClient(
    SUPABASE_URL!,
    SUPABASE_SERVICE_ROLE_KEY!
  )

  try {
    // =========================================================
    // 1. AUTHENTICATION
    // =========================================================
    console.log("[Assessment] Request received")

    const authHeader = req.headers.get("Authorization")

    if (!authHeader) {
      return jsonResponse(
        { error: "Unauthorized" },
        401
      )
    }

    const token = authHeader.replace(
      /^Bearer\s+/i,
      ""
    )

    const {
      data: { user },
      error: authError,
    } = await supabaseClient.auth.getUser(token)

    if (authError || !user) {
      console.error(
        "[Assessment] Auth error:",
        authError
      )

      return jsonResponse(
        { error: "Unauthorized" },
        401
      )
    }

    console.log(
      "[Assessment] Authentication complete"
    )

    // =========================================================
    // 2. REQUEST BODY
    // =========================================================
    const body = await req.json()

    const assessmentId =
      body?.assessmentId

    const completeContext =
      body?.completeContext

    if (
      !assessmentId ||
      typeof assessmentId !== "string"
    ) {
      return jsonResponse(
        {
          error:
            "assessmentId is required",
        },
        400
      )
    }

    console.log(
      `[Assessment] Processing assessmentId=${assessmentId}, userId=${user.id}`
    )

    // =========================================================
    // 3. FETCH ASSESSMENT + PROFILE
    // =========================================================
    const [
      assessmentRes,
      profileRes,
    ] = await Promise.all([
      supabaseClient
        .from("assessments")
        .select(
          "id, user_id, body_temperature, additional_notes, image_url"
        )
        .eq("id", assessmentId)
        .single(),

      supabaseClient
        .from("health_profiles")
        .select("*")
        .eq("user_id", user.id)
        .maybeSingle(),
    ])

    const assessment =
      assessmentRes.data

    const profile =
      profileRes.data

    if (
      assessmentRes.error ||
      !assessment
    ) {
      console.error(
        "[Assessment] Fetch error:",
        assessmentRes.error
      )

      return jsonResponse(
        {
          error:
            "Assessment not found",
          assessmentId,
        },
        404
      )
    }

    // =========================================================
    // 4. OWNERSHIP
    // =========================================================
    if (
      assessment.user_id !== user.id
    ) {
      console.error(
        "[Assessment] Ownership mismatch"
      )

      return jsonResponse(
        {
          error:
            "Assessment not found",
        },
        404
      )
    }

    console.log(
      "[Assessment] Ownership verified"
    )

    // =========================================================
    // 5. PREPARE ASSESSMENT CONTEXT
    // =========================================================
    let assessmentData =
      completeContext?.initialContext

    let symptomsText = ""
    let qaText = ""

    if (!assessmentData) {
      console.log(
        "[Result] Context not provided, fetching from DB"
      )

      const [
        symptomsRes,
        questionsRes,
      ] = await Promise.all([
        supabaseClient
          .from("assessment_symptoms")
          .select("*")
          .eq(
            "assessment_id",
            assessmentId
          ),

        supabaseClient
          .from("assessment_questions")
          .select("*")
          .eq(
            "assessment_id",
            assessmentId
          ),
      ])

      if (symptomsRes.error) {
        console.error(
          "[Assessment] Symptoms fetch error:",
          symptomsRes.error
        )

        throw new Error(
          "Failed to fetch assessment symptoms"
        )
      }

      if (questionsRes.error) {
        console.error(
          "[Assessment] Questions fetch error:",
          questionsRes.error
        )

        throw new Error(
          "Failed to fetch assessment questions"
        )
      }

      const symptoms =
        symptomsRes.data ?? []

      const questions =
        questionsRes.data ?? []

      if (symptoms.length === 0) {
        throw new Error(
          "No symptoms found for assessment"
        )
      }

      assessmentData = {
        bodyTemperature:
          assessment.body_temperature,

        additionalNotes:
          assessment.additional_notes,

        imageUrl:
          assessment.image_url,

        symptoms:
          symptoms.map(
            (s: any) =>
              s.symptom_name
          ),

        healthProfile:
          profile
            ? {
                age:
                  profile.age,

                biologicalSex:
                  profile.biological_sex,

                existingConditions:
                  profile.medical_conditions,

                currentMedicines:
                  profile.medications,

                allergies:
                  profile.allergies,
              }
            : null,
      }

      symptomsText =
        symptoms
          .map(
            (s: any) =>
              `- ${s.symptom_name} (Severity: ${
                s.severity ?? "N/A"
              }/10, Pain: ${
                s.pain_level ?? "N/A"
              }/10, Duration: ${
                s.duration ?? "N/A"
              }, Frequency: ${
                s.frequency ?? "N/A"
              }, Onset: ${
                s.onset ?? "N/A"
              })`
          )
          .join("\n")

      qaText =
        questions
          .map(
            (q: any) =>
              `Q: ${q.question}\nA: ${
                q.answer?.trim() ||
                "Not answered"
              }`
          )
          .join("\n\n")
    } else {
      console.log(
        "[Result] Using provided context from request"
      )

      const symptoms =
        Array.isArray(
          assessmentData.symptoms
        )
          ? assessmentData.symptoms
          : []

      const followUpAnswers =
        Array.isArray(
          completeContext?.followUpAnswers
        )
          ? completeContext.followUpAnswers
          : []

      symptomsText =
        symptoms.join(", ")

      qaText =
        followUpAnswers
          .map(
            (item: any) =>
              `Q: ${item.question}\nA: ${item.answer}`
          )
          .join("\n\n")
    }

    // =========================================================
    // 6. BUILD PROMPT
    // =========================================================
    const bodyTemperature =
      assessmentData?.bodyTemperature ??
      assessment.body_temperature ??
      "Unknown"

    const additionalNotes =
      assessmentData?.additionalNotes ??
      assessment.additional_notes ??
      "None"

    const contextText = `
Body Temperature: ${bodyTemperature}°C
Additional Notes: ${additionalNotes}
`.trim()

    let prompt = `
You are a medical AI assistant for SymptoScan.

Analyze the user's reported health information and provide general health guidance.

Do not provide a definitive diagnosis.
Do not prescribe medications or dosages.
Do not claim certainty.
Do not replace professional medical evaluation.

HEALTH CONTEXT:
${contextText}
`.trim()

    const healthProfile =
      assessmentData?.healthProfile

    if (healthProfile) {
      prompt += `

USER HEALTH PROFILE:
Age: ${
        healthProfile.age ??
        "Unknown"
      }
Biological Sex: ${
        healthProfile.biologicalSex ??
        "Unknown"
      }
Existing Conditions: ${
        healthProfile.existingConditions ??
        "None"
      }
Current Medicines: ${
        healthProfile.currentMedicines ??
        "None"
      }
Allergies: ${
        healthProfile.allergies ??
        "None"
      }
`
    }

    prompt += `

SYMPTOMS:
${symptomsText || "None provided"}

FOLLOW-UP QUESTIONS AND ANSWERS:
${qaText || "None provided"}

INSTRUCTIONS:
1. Provide a concise summary.
2. Provide 2-3 possible conditions or explanations using cautious language.
3. Provide general recommendations.
4. Provide warning signs requiring urgent medical attention.
5. Assign urgency using ONLY:
   "emergency", "urgent", "moderate", "routine", "self_care".
6. Include a medical disclaimer.
7. Generate an overall risk_score as an INTEGER from 0 to 100.
8. 0 = lowest overall risk.
9. 100 = highest overall risk.
10. risk_score MUST NOT be null.
11. Return ONLY valid JSON.

RISK SCORE:
Base the score on:
- reported symptoms
- severity
- pain
- duration
- frequency
- onset
- follow-up answers
- warning signs
- urgency

RESPONSE FORMAT:
{
  "summary": "...",
  "risk_score": 32,
  "urgency_level": "routine",
  "possible_causes": ["...", "..."],
  "recommendations": ["...", "..."],
  "warning_signs": ["...", "..."],
  "disclaimer": "This information is for general educational purposes..."
}
`.trim()

    // =========================================================
    // 7. OPTIONAL IMAGE
    // =========================================================
    let input: any = prompt

    const imagePath =
      assessment.image_url ||
      assessmentData?.imageUrl

    if (imagePath) {
      console.log(
        "[Assessment] Fetching image"
      )

      const {
        data: imageData,
        error: imageError,
      } =
        await supabaseClient.storage
          .from(
            "assessment-images"
          )
          .download(imagePath)

      if (
        imageData &&
        !imageError
      ) {
        const buffer =
          await imageData.arrayBuffer()

        const bytes =
          new Uint8Array(buffer)

        let binary = ""

        for (
          let i = 0;
          i < bytes.length;
          i++
        ) {
          binary += String.fromCharCode(
            bytes[i]
          )
        }

        const base64Image =
          btoa(binary)

        const lowerPath =
          imagePath.toLowerCase()

        const mimeType =
          lowerPath.endsWith(
            ".png"
          )
            ? "image/png"
            : lowerPath.endsWith(
                ".webp"
              )
            ? "image/webp"
            : "image/jpeg"

        input = [
          { text: prompt },
          {
            inline_data: {
              mime_type:
                mimeType,
              data:
                base64Image,
            },
          },
        ]

        console.log(
          "[Assessment] Multimodal input prepared"
        )
      } else {
        console.warn(
          "[Assessment] Image unavailable; using text-only analysis"
        )
      }
    }

    // =========================================================
    // 8. GEMINI REQUEST
    // =========================================================
    console.log(
      "[Assessment] Gemini request started"
    )

    const geminiResponse =
      await fetch(
        "https://generativelanguage.googleapis.com/v1/interactions",
        {
          method: "POST",

          headers: {
            "Content-Type":
              "application/json",
            "x-goog-api-key":
              GEMINI_API_KEY!,
          },

          body: JSON.stringify({
            model:
              "gemini-3.6-flash",

            input,
          }),
        }
      )

    if (!geminiResponse.ok) {
      const errorText =
        await geminiResponse.text()

      console.error(
        "[Gemini] API error:",
        errorText
      )

      throw new Error(
        "Gemini API failed"
      )
    }

    console.log(
      "[Assessment] Gemini result generation completed"
    )

    const interactionData =
      await geminiResponse.json()

    // =========================================================
    // 9. EXTRACT MODEL OUTPUT
    // Gemini Interactions API:
    // steps[].content[].text
    // =========================================================
    let aiOutputText = ""

    if (
      Array.isArray(
        interactionData?.steps
      )
    ) {
      const modelOutputStep =
        [...interactionData.steps]
          .reverse()
          .find(
            (step: any) =>
              step?.type ===
              "model_output"
          )

      if (modelOutputStep) {
        const textItem =
          Array.isArray(
            modelOutputStep.content
          )
            ? modelOutputStep.content.find(
                (item: any) =>
                  item?.type ===
                    "text" &&
                  typeof item?.text ===
                    "string"
              )
            : null

        if (
          textItem?.text
        ) {
          aiOutputText =
            textItem.text
        }
      }
    }

    if (!aiOutputText) {
      console.error(
        "[AI][Result] No text found in model_output.content"
      )

      throw new Error(
        "Invalid AI response format"
      )
    }

    // =========================================================
    // 10. CLEAN JSON
    // =========================================================
    aiOutputText =
      aiOutputText.trim()

    if (
      aiOutputText.startsWith(
        "```"
      )
    ) {
      aiOutputText =
        aiOutputText
          .replace(
            /^```json\s*/i,
            ""
          )
          .replace(
            /```\s*$/i,
            ""
          )
          .trim()
    }

    let aiResult: any

    try {
      const jsonMatch =
        aiOutputText.match(
          /\{[\s\S]*\}/
        )

      const cleanJson =
        jsonMatch
          ? jsonMatch[0]
          : aiOutputText

      aiResult =
        JSON.parse(
          cleanJson
        )
    } catch (error) {
      console.error(
        "[AI][Result] JSON parse failed:",
        error
      )

      throw new Error(
        "Invalid AI response format"
      )
    }

    console.log(
      "[Assessment] Gemini response parsed successfully"
    )

    // =========================================================
    // 11. VALIDATE RESULT
    // =========================================================
    const validUrgencyLevels = [
      "emergency",
      "urgent",
      "moderate",
      "routine",
      "self_care",
    ]

    if (
      typeof aiResult?.summary !==
        "string" ||
      aiResult.summary.trim()
        .length === 0
    ) {
      throw new Error(
        "AI result summary missing"
      )
    }

    if (
      !validUrgencyLevels.includes(
        aiResult.urgency_level
      )
    ) {
      throw new Error(
        `Invalid urgency level: ${aiResult.urgency_level}`
      )
    }

    // =========================================================
    // 12. STRICT RISK SCORE
    // =========================================================
    const rawRiskScore =
      aiResult?.risk_score

    console.log(
      `[AI][Result] Raw risk score: ${rawRiskScore} (type: ${typeof rawRiskScore})`
    )

    if (
      typeof rawRiskScore !==
        "number" ||
      !Number.isFinite(
        rawRiskScore
      ) ||
      !Number.isInteger(
        rawRiskScore
      ) ||
      rawRiskScore < 0 ||
      rawRiskScore > 100
    ) {
      console.error(
        `[AI][Result] Invalid risk_score: ${rawRiskScore}`
      )

      throw new Error(
        "Invalid risk_score returned by AI"
      )
    }

    const riskScore =
      rawRiskScore

    console.log(
      `[AI][Result] Final risk score to save: ${riskScore}`
    )

    // =========================================================
    // 13. SAVE ONLY EXISTING DB COLUMNS
    // =========================================================
    console.log(
      `[AI][Result] Saving assessment result for ${assessmentId}`
    )

    const {
      data: savedResult,
      error: resultError,
    } =
      await supabaseClient
        .from(
          "assessment_results"
        )
        .upsert(
          {
            assessment_id:
              assessmentId,

            summary:
              aiResult.summary.trim(),

            possible_causes:
              Array.isArray(
                aiResult.possible_causes
              )
                ? aiResult.possible_causes
                : [],

            recommendations:
              Array.isArray(
                aiResult.recommendations
              )
                ? aiResult.recommendations
                : [],

            warning_signs:
              Array.isArray(
                aiResult.warning_signs
              )
                ? aiResult.warning_signs
                : [],

            urgency_level:
              aiResult.urgency_level,

            disclaimer:
              typeof aiResult.disclaimer ===
              "string"
                ? aiResult.disclaimer.trim()
                : "This information is for general educational purposes only.",

            risk_score:
              riskScore,
          },
          {
            onConflict:
              "assessment_id",
          }
        )
        .select()
        .single()

    if (resultError) {
      console.error(
        "[DB] Assessment result save error:",
        resultError
      )

      throw new Error(
        `Failed to save assessment result: ${resultError.message}`
      )
    }

    // =========================================================
    // 14. VERIFY SAVED SCORE
    // =========================================================
    if (
      savedResult?.risk_score !==
      riskScore
    ) {
      console.error(
        "[DB] Risk score mismatch",
        {
          expected:
            riskScore,
          actual:
            savedResult?.risk_score,
        }
      )

      throw new Error(
        "Risk score was not persisted correctly"
      )
    }

    console.log(
      `[AI][Result] Result persisted successfully`
    )

    // =========================================================
    // 15. MARK AS COMPLETED
    // =========================================================
    const completedAt =
      new Date().toISOString()

    const {
      error: updateError,
    } =
      await supabaseClient
        .from("assessments")
        .update({
          status:
            "completed",

          completed_at:
            completedAt,
        })
        .eq(
          "id",
          assessmentId
        )
        .eq(
          "user_id",
          user.id
        )

    if (updateError) {
      console.error(
        "[DB] Assessment update error"
      )

      throw new Error(
        `Failed to update assessment status`
      )
    }

    console.log(
      "[Assessment] Database update completed"
    )

    // =========================================================
    // 16. FINAL ANDROID RESPONSE
    // Only fields that actually exist in
    // assessment_results + optional app-side fields.
    // =========================================================
    const finalResponse = {
      id:
        savedResult?.id ??
        null,

      assessment_id:
        assessmentId,

      summary:
        savedResult?.summary ??
        aiResult.summary,

      possible_causes:
        savedResult?.possible_causes ??
        [],

      recommendations:
        savedResult?.recommendations ??
        [],

      warning_signs:
        savedResult?.warning_signs ??
        [],

      urgency_level:
        savedResult?.urgency_level ??
        aiResult.urgency_level,

      disclaimer:
        savedResult?.disclaimer ??
        aiResult.disclaimer,

      risk_score:
        savedResult?.risk_score ??
        riskScore,
    }

    return jsonResponse(
      finalResponse,
      200
    )
  } catch (error) {
    console.error(
      "[AI][Result] Final result generation error:",
      error
    )

    return jsonResponse(
      {
        error: "Unable to generate assessment result. Please try again.",
      },
      500
    )
  }
})
