import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from 'https://esm.sh/@supabase/supabase-js@2'

const GEMINI_API_KEY = Deno.env.get('GEMINI_API_KEY')
const SUPABASE_URL = Deno.env.get('SUPABASE_URL')
const SUPABASE_SERVICE_ROLE_KEY = Deno.env.get('SUPABASE_SERVICE_ROLE_KEY')

serve(async (req) => {
  const supabaseClient = createClient(SUPABASE_URL!, SUPABASE_SERVICE_ROLE_KEY!)

  try {
    // 1. Authentication
    const authHeader = req.headers.get('Authorization')
    if (!authHeader) {
      return new Response(JSON.stringify({ error: 'Unauthorized' }), { status: 401, headers: { 'Content-Type': 'application/json' } })
    }

    const token = authHeader.replace('Bearer ', '')
    const { data: { user }, error: authError } = await supabaseClient.auth.getUser(token)

    if (authError || !user) {
      console.error('Auth error:', authError)
      return new Response(JSON.stringify({ error: 'Unauthorized' }), { status: 401, headers: { 'Content-Type': 'application/json' } })
    }

    const { assessmentId } = await req.json()
    if (!assessmentId) {
      return new Response(JSON.stringify({ error: 'assessmentId is required' }), { status: 400, headers: { 'Content-Type': 'application/json' } })
    }

    console.log(`Processing generate-assessment-result for assessmentId: ${assessmentId}, userId: ${user.id}`)

    // 2. Verify ownership & Fetch data
    const { data: assessment, error: assessmentError } = await supabaseClient
      .from('assessments')
      .select(`
        user_id,
        body_temperature,
        additional_notes,
        assessment_symptoms (*),
        assessment_questions (*)
      `)
      .eq('id', assessmentId)
      .single()

    if (assessmentError || !assessment) {
      console.error('Assessment not found or error:', assessmentError, 'assessmentId:', assessmentId)
      return new Response(JSON.stringify({ error: 'Assessment not found', assessmentId: assessmentId }), { status: 404, headers: { 'Content-Type': 'application/json' } })
    }

    if (assessment.user_id !== user.id) {
        console.error('Assessment ownership mismatch. Assessment owner:', assessment.user_id, 'Authenticated user:', user.id)
        return new Response(JSON.stringify({ error: 'Assessment not found' }), { status: 404, headers: { 'Content-Type': 'application/json' } })
    }

    const symptoms = assessment.assessment_symptoms || []
    const questions = assessment.assessment_questions || []

    // 3. Validate presence of data
    if (symptoms.length === 0) {
      return new Response(JSON.stringify({ error: 'No symptoms found for this assessment' }), { status: 400, headers: { 'Content-Type': 'application/json' } })
    }

    if (questions.length === 0) {
      return new Response(JSON.stringify({ error: 'No follow-up questions found for this assessment' }), { status: 400, headers: { 'Content-Type': 'application/json' } })
    }

    // 4. Validate all questions are answered
    const unanswered = questions.some((q: any) => !q.answer || q.answer.trim().length === 0)
    if (unanswered) {
      return new Response(JSON.stringify({ error: 'Please answer all assessment questions' }), { status: 400, headers: { 'Content-Type': 'application/json' } })
    }

    // 5. Build AI prompt
    const symptomsText = symptoms.map((s: any) =>
      `- ${s.symptom_name} (Severity: ${s.severity}/10, Pain: ${s.pain_level}/10, Duration: ${s.duration}, Frequency: ${s.frequency}, Onset: ${s.onset})`
    ).join('\n')

    const qaText = questions.map((q: any) =>
      `Q: ${q.question}\nA: ${q.answer}`
    ).join('\n\n')

    const contextText = `
Body Temperature: ${assessment.body_temperature}°C
Additional Notes: ${assessment.additional_notes || 'None'}
`.trim()

    const prompt = `You are a medical AI assistant for SymptoScan. Analyze the following user health report and provide general health guidance.

Health Context:
${contextText}

Symptoms:
${symptomsText}

Follow-up Questions & Answers:
${qaText}

Instructions:
1. Provide a concise summary of the reported symptoms.
2. List possible causes or explanations (use cautious language like "This may be associated with...").
3. Provide general health recommendations.
4. List specific warning signs that would require immediate medical attention.
5. Assign an urgency level: "emergency", "urgent", "routine", or "self_care".
6. Include a clear medical disclaimer.
7. Return strictly valid JSON.

Constraints:
- DO NOT provide a definitive diagnosis.
- DO NOT prescribe specific medications or dosages.
- DO NOT claim certainty.
- DO NOT replace a professional medical evaluation.
- Return ONLY valid JSON.

Response format:
{
  "summary": "...",
  "possible_causes": ["...", "..."],
  "recommendations": ["...", "..."],
  "warning_signs": ["...", "..."],
  "urgency_level": "routine",
  "disclaimer": "This information is for general educational purposes..."
}`

    // 6. Call Gemini (Migrated to Current Interactions API)
    const geminiResponse = await fetch("https://generativelanguage.googleapis.com/v1/interactions", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "x-goog-api-key": GEMINI_API_KEY!
      },
      body: JSON.stringify({
        model: "gemini-3.6-flash",
        input: prompt
      })
    })

    if (!geminiResponse.ok) {
      const errorText = await geminiResponse.text()
      console.error(`Gemini Interactions API error: ${errorText}`)
      throw new Error('Gemini API failed')
    }

    const interactionData = await geminiResponse.ok ? await geminiResponse.json() : {}
    console.log("Gemini result generation completed")

    // Find the model_output step
    const modelOutputStep = interactionData.steps?.find(
        (step: any) => step.type === "model_output"
    )

    if (!modelOutputStep) {
      console.error("No model_output found in Gemini response")
      throw new Error("Invalid AI response format")
    }

    // Find the text item in content
    const textContent = modelOutputStep.content?.find(
        (item: any) => item.type === "text"
    )

    let aiOutputText = textContent?.text
    if (!aiOutputText || typeof aiOutputText !== "string") {
      console.error("Invalid Gemini model output text")
      throw new Error("Invalid Gemini model output")
    }

    // JSON CLEANING
    aiOutputText = aiOutputText.trim()
    if (aiOutputText.startsWith("```")) {
      aiOutputText = aiOutputText.replace(/^```json\s*/, "").replace(/```$/, "").trim()
    }

    // Fallback cleaning if needed
    if (!aiOutputText.startsWith("{")) {
       const start = aiOutputText.indexOf("{")
       const end = aiOutputText.lastIndexOf("}")
       if (start !== -1 && end !== -1 && end > start) {
         aiOutputText = aiOutputText.substring(start, end + 1)
       }
    }

    let aiResult
    try {
        aiResult = JSON.parse(aiOutputText)
    } catch (e) {
        console.error('Failed to parse Gemini JSON:', e, aiOutputText)
        throw new Error('Invalid AI response format')
    }

    // 7. Validate structured response
    const validUrgencyLevels = ['emergency', 'urgent', 'routine', 'self_care']
    if (!aiResult.summary || typeof aiResult.summary !== 'string' ||
        !Array.isArray(aiResult.possible_causes) ||
        !Array.isArray(aiResult.recommendations) ||
        !Array.isArray(aiResult.warning_signs) ||
        !aiResult.disclaimer || typeof aiResult.disclaimer !== 'string' ||
        !validUrgencyLevels.includes(aiResult.urgency_level)) {
        console.error('AI result validation failed:', aiResult)
        throw new Error('AI result validation failed')
    }

    // 8. Save result (Upsert for idempotency)
    const { data: savedResult, error: resultError } = await supabaseClient
      .from('assessment_results')
      .upsert(
        {
          assessment_id: assessmentId,
          summary: aiResult.summary.trim(),
          possible_causes: aiResult.possible_causes,
          recommendations: aiResult.recommendations,
          warning_signs: aiResult.warning_signs,
          urgency_level: aiResult.urgency_level,
          disclaimer: aiResult.disclaimer.trim()
        },
        { onConflict: 'assessment_id' }
      )
      .select()
      .single()

    if (resultError) {
        console.error('Database result save error:', resultError)
        throw new Error('Failed to save assessment result')
    }

    // 9. Mark assessment as completed with server timestamp
    const { error: updateError } = await supabaseClient
      .from('assessments')
      .update({
        status: 'completed',
        completed_at: new Date().toISOString()
      })
      .eq('id', assessmentId)

    if (updateError) {
        console.error('Database assessment update error:', updateError)
        throw new Error('Failed to update assessment status')
    }

    return new Response(JSON.stringify(savedResult), {
      headers: { 'Content-Type': 'application/json' },
    })

  } catch (err) {
    console.error('Final result generation error:', err)
    // 10. Generic error handling for client
    return new Response(JSON.stringify({ error: 'Unable to generate assessment result. Please try again.' }), {
        status: 500,
        headers: { 'Content-Type': 'application/json' }
    })
  }
})
