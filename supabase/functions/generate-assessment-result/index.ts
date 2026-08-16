import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from 'https://esm.sh/@supabase/supabase-js@2'

const GEMINI_API_KEY = Deno.env.get('GEMINI_API_KEY')
const SUPABASE_URL = Deno.env.get('SUPABASE_URL')
const SUPABASE_SERVICE_ROLE_KEY = Deno.env.get('SUPABASE_SERVICE_ROLE_KEY')

serve(async (req) => {
  const supabaseClient = createClient(SUPABASE_URL!, SUPABASE_SERVICE_ROLE_KEY!)

  try {
    // 1. Authentication
    console.log("[Assessment] Request received");
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

    console.log("[Assessment] Authentication complete");

    const { assessmentId } = await req.json()
    if (!assessmentId) {
      return new Response(JSON.stringify({ error: 'assessmentId is required' }), { status: 400, headers: { 'Content-Type': 'application/json' } })
    }

    console.log(`Processing generate-assessment-result for assessmentId: ${assessmentId}, userId: ${user.id}`)

    // 2. Fetch all data in parallel
    const [assessmentRes, symptomsRes, questionsRes, profileRes] = await Promise.all([
        supabaseClient
          .from('assessments')
          .select(`
            user_id,
            body_temperature,
            additional_notes,
            image_url
          `)
          .eq('id', assessmentId)
          .single(),
        supabaseClient
          .from('assessment_symptoms')
          .select('*')
          .eq('assessment_id', assessmentId),
        supabaseClient
          .from('assessment_questions')
          .select('*')
          .eq('assessment_id', assessmentId),
        supabaseClient
          .from('health_profiles')
          .select('*')
          .eq('user_id', user.id)
          .maybeSingle()
    ])

    const assessment = assessmentRes.data
    if (assessmentRes.error || !assessment) {
      console.error('Assessment not found or error:', assessmentRes.error, 'assessmentId:', assessmentId)
      return new Response(JSON.stringify({ error: 'Assessment not found', assessmentId: assessmentId }), { status: 404, headers: { 'Content-Type': 'application/json' } })
    }

    if (assessment.user_id !== user.id) {
        console.error('Assessment ownership mismatch. Assessment owner:', assessment.user_id, 'Authenticated user:', user.id)
        return new Response(JSON.stringify({ error: 'Assessment not found' }), { status: 404, headers: { 'Content-Type': 'application/json' } })
    }

    console.log("[Assessment] Data loaded");

    const symptoms = symptomsRes.data || []
    const questions = questionsRes.data || []
    const profile = profileRes.data

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

    let profileText = "None provided"
    if (profile) {
        profileText = `
Biological Sex: ${profile.biological_sex || 'Not provided'}
Height: ${profile.height_cm || '--'}cm, Weight: ${profile.weight_kg || '--'}kg
Allergies: ${profile.allergies || 'None'}
Chronic Conditions: ${profile.medical_conditions || 'None'}
Current Medications: ${profile.medications || 'None'}
`.trim()
    }

    const contextText = `
Body Temperature: ${assessment.body_temperature}°C
Additional Notes: ${assessment.additional_notes || 'None'}
`.trim()

    let prompt = `You are a medical AI assistant for SymptoScan. Analyze the following user health report, considering their medical background, reported symptoms, and follow-up answers. Provide general health guidance and a risk assessment.

USER HEALTH CONTEXT:
${profileText}

CURRENT ASSESSMENT:
${contextText}

SYMPTOMS:
${symptomsText}

FOLLOW-UP Q&A:
${qaText}
`

    if (assessment.image_url) {
        prompt += `\nAn image has been provided by the user for visual context. Use it to inform your analysis but prioritize safety and mention that visual analysis is limited.
        IMPORTANT: If the image is irrelevant, unclear, or poor quality, do NOT invent observations. Rely more heavily on the text-based symptoms and description.`
    }

    prompt += `

Instructions:
1. Provide a concise summary of the reported symptoms and the analysis.
2. List possible conditions (at least 2-3). For each, provide a name, severity level (Mild, Moderate, Severe), and a confidence percentage (1-100).
3. Provide general health recommendations (lifestyle, first aid, OTC if appropriate, but avoid specific dosages).
4. List specific warning signs that would require immediate medical attention.
5. Assign an overall risk score as an integer from 0 to 100 and an urgency level: "emergency", "urgent", "moderate", "routine", or "self_care".
   - risk_score: 0 = lowest overall risk, 100 = highest overall risk. Never return null.
6. Provide a recommendation for which type of medical specialist to see (e.g., "General Practitioner", "Cardiologist").
7. Include a clear medical disclaimer.
8. Return strictly valid JSON.

Constraints:
- DO NOT provide a definitive diagnosis.
- DO NOT prescribe specific medications or dosages.
- DO NOT claim certainty.
- DO NOT replace a professional medical evaluation.
- DO NOT recommend starting or stopping prescription medicines.
- Return ONLY valid JSON.

Response format:
{
  "summary": "...",
  "risk_score": 32,
  "urgency_level": "routine",
  "conditions": [
    { "name": "Condition Name", "severity": "Mild", "confidence": 75, "icon": "🤒" }
  ],
  "recommendation_items": [
    { "title": "Recommendation", "icon": "💊" }
  ],
  "warning_signs": ["...", "..."],
  "specialist": {
    "title": "Specialist Title",
    "description": "Why they should see this specialist..."
  },
  "disclaimer": "This information is for general educational purposes..."
}`

    // 6. Call Gemini (Multimodal support)
    let input: any = prompt

    if (assessment.image_url) {
        console.log(`Fetching image from storage: ${assessment.image_url}`)
        const { data: imageData, error: imageError } = await supabaseClient
            .storage
            .from('assessment-images')
            .download(assessment.image_url)

        if (imageData && !imageError) {
            const buffer = await imageData.arrayBuffer()
            // Convert to base64
            let binary = '';
            const bytes = new Uint8Array(buffer);
            const len = bytes.byteLength;
            for (let i = 0; i < len; i++) {
                binary += String.fromCharCode(bytes[i]);
            }
            const base64Image = btoa(binary)

            const mimeType = assessment.image_url.toLowerCase().endsWith('.png') ? 'image/png' :
                             assessment.image_url.toLowerCase().endsWith('.webp') ? 'image/webp' : 'image/jpeg'

            input = [
                { text: prompt },
                {
                    inline_data: {
                        mime_type: mimeType,
                        data: base64Image
                    }
                }
            ]
            console.log('Multimodal input prepared')
        } else {
            console.error('Failed to download image:', imageError)
            // Fallback to text-only if image download fails
        }
    }

    console.log("[Assessment] Image check complete");
    console.log("[Assessment] Gemini request started");

    const geminiResponse = await fetch("https://generativelanguage.googleapis.com/v1/interactions", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "x-goog-api-key": GEMINI_API_KEY!
      },
      body: JSON.stringify({
        model: "gemini-3.6-flash",
        input: input
      })
    })

    if (!geminiResponse.ok) {
      const errorText = await geminiResponse.text()
      console.error(`Gemini Interactions API error: ${errorText}`)
      throw new Error('Gemini API failed')
    }

    console.log("[Assessment] Gemini result generation completed");

    const interactionData = await geminiResponse.json()
    let aiOutputText = ""

    // Extract text from model_output step
    if (interactionData.steps && Array.isArray(interactionData.steps)) {
      const modelOutputStep = [...interactionData.steps].reverse().find(step => step.type === "model_output")
      if (modelOutputStep && modelOutputStep.text) {
        aiOutputText = modelOutputStep.text
      }
    }

    if (!aiOutputText) {
      console.error('No model_output found in Gemini response:', interactionData)
      throw new Error('Invalid AI response format')
    }

    let aiResult
    try {
        aiResult = JSON.parse(aiOutputText)
    } catch (e) {
        console.error('Failed to parse Gemini JSON:', e, aiOutputText)
        throw new Error('Invalid AI response format')
    }

    console.log("[Assessment] Gemini response parsed");

    // 7. Validate structured response
    const validUrgencyLevels = ['emergency', 'urgent', 'moderate', 'routine', 'self_care']
    if (!aiResult.summary || !validUrgencyLevels.includes(aiResult.urgency_level)) {
        console.error('AI result basic validation failed:', aiResult)
        throw new Error('AI result validation failed')
    }

    // Risk score validation (0-100)
    console.log(`[AI][Result] Generated risk score: ${aiResult.risk_score}`)
    if (aiResult.risk_score === undefined || aiResult.risk_score === null || typeof aiResult.risk_score !== "number" || !Number.isFinite(aiResult.risk_score)) {
        console.error('[AI][Result] ERROR: Invalid risk score type or missing:', aiResult.risk_score)
        throw new Error("Invalid risk_score returned by AI")
    }

    const validatedRiskScore = Math.round(Math.max(0, Math.min(100, aiResult.risk_score)))
    console.log(`[AI][Result] Validated risk score: ${validatedRiskScore}`)

    // 8. Save result (Upsert for idempotency)
    console.log(`[AI][Result] Saving assessment result: ${assessmentId}`);
    const { error: resultError } = await supabaseClient
      .from('assessment_results')
      .upsert({
        assessment_id: assessmentId,
        summary: aiResult.summary,
        urgency_level: aiResult.urgency_level,
        disclaimer: aiResult.disclaimer,
        risk_score: validatedRiskScore,
        possible_causes: aiResult.conditions?.map((c: any) => `${c.name} (${c.confidence}%)`) || [],
        recommendations: aiResult.recommendation_items?.map((r: any) => r.title) || [],
        warning_signs: aiResult.warning_signs || []
      })

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

    console.log("[Assessment] Database update completed");
    console.log(`[AI][Result] Saved risk score: ${validatedRiskScore}`);
    console.log("[Assessment] HTTP response being returned");

    return new Response(JSON.stringify({ ...aiResult, risk_score: validatedRiskScore }), {
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
