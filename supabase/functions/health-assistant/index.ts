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
      return new Response(JSON.stringify({ error: 'UNAUTHORIZED', message: 'Authentication required.' }), { status: 401, headers: { 'Content-Type': 'application/json' } })
    }

    const token = authHeader.replace('Bearer ', '')
    const { data: { user }, error: authError } = await supabaseClient.auth.getUser(token)

    if (authError || !user) {
      return new Response(JSON.stringify({ error: 'UNAUTHORIZED', message: 'Authentication required.' }), { status: 401, headers: { 'Content-Type': 'application/json' } })
    }

    // 2. Request Validation
    const rawBody = await req.text()

    let body
    try {
      body = JSON.parse(rawBody)
    } catch (e) {
      return new Response(JSON.stringify({ error: 'BAD_REQUEST', message: 'Invalid JSON' }), { status: 400, headers: { 'Content-Type': 'application/json' } })
    }

    const { conversationId, message, language = 'en' } = body

    if (!conversationId || !message || message.trim().length === 0) {
      console.error(`Validation failed. conversationId: ${conversationId}, message length: ${message?.length}`)
      return new Response(JSON.stringify({ error: 'BAD_REQUEST', message: 'conversationId and message are required.' }), { status: 400, headers: { 'Content-Type': 'application/json' } })
    }

    if (language !== 'en' && language !== 'hi') {
      return new Response(JSON.stringify({ error: 'BAD_REQUEST', message: 'Language must be en or hi.' }), { status: 400, headers: { 'Content-Type': 'application/json' } })
    }

    console.log(`Processing health assistant request for conversationId: ${conversationId}, authenticated user: ${user.id}`)

    // 3. Conversation Ownership
    const { data: conversation, error: convError } = await supabaseClient
      .from('health_conversations')
      .select('user_id')
      .eq('id', conversationId)
      .maybeSingle()

    if (convError) {
      console.error(`Database error during conversation lookup: ${convError.message}`)
      throw new Error('Database lookup failed')
    }

    if (!conversation) {
      console.error(`Conversation not found in database for ID: ${conversationId}`)
      return new Response(JSON.stringify({ error: 'NOT_FOUND', message: 'Conversation not found.' }), { status: 404, headers: { 'Content-Type': 'application/json' } })
    }

    if (conversation.user_id !== user.id) {
      console.error(`Ownership mismatch. Conversation owner: ${conversation.user_id}, Authenticated user: ${user.id}`)
      return new Response(JSON.stringify({ error: 'NOT_FOUND', message: 'Conversation not found.' }), { status: 404, headers: { 'Content-Type': 'application/json' } })
    }

    // 4. Save User Message
    const { error: msgInsertError } = await supabaseClient
      .from('health_messages')
      .insert({
        conversation_id: conversationId,
        user_id: user.id,
        role: 'user',
        content: message.trim()
      })

    if (msgInsertError) {
      console.error('Failed to save user message:', msgInsertError)
      throw new Error('Database error')
    }

    // 5. Load History (max 20)
    const { data: history, error: historyError } = await supabaseClient
      .from('health_messages')
      .select('role, content')
      .eq('conversation_id', conversationId)
      .order('created_at', { ascending: false })
      .limit(20)

    if (historyError) {
      console.error('Failed to load history:', historyError)
      throw new Error('Database error')
    }

    const messagesForGemini = history.reverse().map((m: any) => ({
      role: m.role === 'assistant' ? 'model' : 'user',
      parts: [{ text: m.content }]
    }))

    // 6. Gemini System Prompt & API Call
    const systemInstruction = `You are SymptoScan Health Assistant.
You provide general health education and wellness information.
You are not a doctor.
You must not provide definitive medical diagnoses.
You must not prescribe medication.
You must not provide medication dosage.
You must not claim certainty.
Use cautious language such as "may be associated with", "possible explanations include", "can sometimes occur with".
You may explain common health concepts, explain symptoms generally, provide general wellness information, provide general self-care information, explain warning signs, and recommend professional medical care when appropriate.
You must NOT diagnose diseases, prescribe medication, provide dosage, perform pregnancy assessment, perform pediatric assessment, diagnose mental health conditions, diagnose chronic diseases, or replace professional medical evaluation.
If potentially serious warning signs are described, clearly recommend urgent or emergency medical evaluation.
Respond in the requested language: ${language === 'hi' ? 'Hindi' : 'English'}.`

    const geminiBody = {
      model: "gemini-3.6-flash",
      input: systemInstruction + "\n\nConversation history:\n" + JSON.stringify(messagesForGemini) + "\n\nUser message: " + message
    }

    const geminiResponse = await fetch("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + GEMINI_API_KEY, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        contents: messagesForGemini.concat([{
            role: 'user',
            parts: [{ text: message }]
        }])
      })
    })

    if (!geminiResponse.ok) {
      if (geminiResponse.status === 429) {
        return new Response(JSON.stringify({ error: 'AI_QUOTA_EXCEEDED', message: 'AI service quota is temporarily unavailable. Please try again later.' }), { status: 429, headers: { 'Content-Type': 'application/json' } })
      }
      const errorText = await geminiResponse.text()
      console.error(`Gemini API error (${geminiResponse.status}): ${errorText}`)
      throw new Error('Gemini API failed')
    }

    const geminiData = await geminiResponse.json()
    const aiText = geminiData.candidates?.[0]?.content?.parts?.[0]?.text

    if (!aiText) throw new Error("Invalid AI response text")

    const cleanAiText = aiText.trim()
    if (aiText.startsWith("```")) {
      aiText = aiText.replace(/^```json\s*/, "").replace(/```$/, "").trim()
    }

    // 7. Save Assistant Message
    const { error: assistantInsertError } = await supabaseClient
      .from('health_messages')
      .insert({
        conversation_id: conversationId,
        user_id: user.id,
        role: 'assistant',
        content: aiText
      })

    if (assistantInsertError) {
      console.error('Failed to save assistant message:', assistantInsertError)
      throw new Error('Database error')
    }

    // 8. Update Conversation Timestamp
    await supabaseClient
      .from('health_conversations')
      .update({ updated_at: new Date().toISOString() })
      .eq('id', conversationId)

    return new Response(JSON.stringify({ conversationId, message: aiText }), {
      headers: { 'Content-Type': 'application/json' },
    })

  } catch (err) {
    console.error('Health assistant error:', err)
    return new Response(JSON.stringify({ error: 'INTERNAL_ERROR', message: 'Unable to get a response right now. Please try again.' }), {
      status: 500,
      headers: { 'Content-Type': 'application/json' }
    })
  }
})
