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

    const body = await req.json()
    const assessmentId = body.assessmentId

    if (!assessmentId) {
      return new Response(JSON.stringify({ error: 'assessmentId is required' }), { status: 400, headers: { 'Content-Type': 'application/json' } })
    }

    console.log(`Processing generate-assessment-questions for assessmentId: ${assessmentId}, userId: ${user.id}`)

    // 2. Verify ownership
    const { data: assessment, error: assessmentError } = await supabaseClient
      .from('assessments')
      .select('user_id, body_temperature, additional_notes')
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

    // 3. Fetch symptoms
    const { data: symptoms, error: symptomsError } = await supabaseClient
      .from('assessment_symptoms')
      .select('*')
      .eq('assessment_id', assessmentId)

    if (symptomsError || !symptoms || symptoms.length === 0) {
      console.error('No symptoms found:', symptomsError)
      return new Response(JSON.stringify({ error: 'No symptoms found for this assessment' }), { status: 400, headers: { 'Content-Type': 'application/json' } })
    }

    const symptomsDescription = symptoms.map((s: any) =>
      `- ${s.symptom_name} (Severity: ${s.severity}/10, Pain: ${s.pain_level}/10, Duration: ${s.duration}, Frequency: ${s.frequency}, Onset: ${s.onset})`
    ).join('\n')

    const contextText = `
Body Temperature: ${assessment.body_temperature}°C
Additional Notes: ${assessment.additional_notes || 'None'}
`.trim()

    // 4. AI Prompt
    const prompt = `You are a medical health assistant for SymptoScan. Based on the following symptoms and health context reported by a user, generate 3 to 5 concise, relevant follow-up questions to better understand their condition.

Health Context:
${contextText}

Symptoms:
${symptomsDescription}

Instructions:
1. Questions must be concise, understandable, and non-leading.
2. Do NOT provide any diagnosis or prescriptions.
3. Focus on clarifying the symptoms (e.g., when it happens, what makes it better/worse).
4. Return ONLY a JSON object with a "questions" field containing an array of strings.

Response format:
{
  "questions": ["Question 1", "Question 2", ...]
}`

    // 5. Call Gemini (Migrated to Current Interactions API)
    // Using gemini-3.6-flash and the interactions endpoint
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

    let aiOutput
    try {
      aiOutput = JSON.parse(aiOutputText)
    } catch (e) {
      console.error('Failed to parse Gemini JSON:', e, aiOutputText)
      throw new Error('Invalid AI response format')
    }

    const questionsList = aiOutput.questions
    if (!Array.isArray(questionsList) || questionsList.length < 3 || questionsList.length > 5) {
      console.error('AI questions validation failed:', questionsList)
      throw new Error('AI failed to generate valid questions')
    }

    // Validate that every question is a non-empty string
    if (questionsList.some((q: any) => typeof q !== 'string' || q.trim().length === 0)) {
       console.error('One or more AI questions are invalid:', questionsList)
       throw new Error('AI failed to generate valid questions')
    }

    // 6. Cleanup old questions (Idempotency)
    await supabaseClient
      .from('assessment_questions')
      .delete()
      .eq('assessment_id', assessmentId)

    const questionsToInsert = questionsList.map((q: string, i: number) => ({
      assessment_id: assessmentId,
      question: q,
      question_order: i + 1
    }))

    // 7. Insert new questions
    const { data: insertedQuestions, error: insertError } = await supabaseClient
      .from('assessment_questions')
      .insert(questionsToInsert)
      .select()

    if (insertError) {
      console.error('Database insert error:', insertError)
      throw new Error('Failed to save questions')
    }

    return new Response(JSON.stringify({ questions: insertedQuestions }), {
      headers: { 'Content-Type': 'application/json' },
    })

  } catch (err) {
    console.error('Question generation error:', err)
    return new Response(JSON.stringify({ error: 'Unable to generate follow-up questions. Please try again.' }), {
      status: 500,
      headers: { 'Content-Type': 'application/json' }
    })
  }
})
