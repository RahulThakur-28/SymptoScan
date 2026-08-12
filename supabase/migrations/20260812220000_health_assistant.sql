-- health_conversations table
CREATE TABLE IF NOT EXISTS public.health_conversations (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    title text,
    language text DEFAULT 'en',
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

-- health_messages table
CREATE TABLE IF NOT EXISTS public.health_messages (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id uuid NOT NULL REFERENCES public.health_conversations(id) ON DELETE CASCADE,
    user_id uuid NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    role text NOT NULL CHECK (role IN ('user', 'assistant')),
    content text NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_health_conversations_user_id ON public.health_conversations(user_id);
CREATE INDEX IF NOT EXISTS idx_health_messages_conversation_id ON public.health_messages(conversation_id);
CREATE INDEX IF NOT EXISTS idx_health_messages_user_id ON public.health_messages(user_id);

-- Enable RLS
ALTER TABLE public.health_conversations ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.health_messages ENABLE ROW LEVEL SECURITY;

-- health_conversations policies
DROP POLICY IF EXISTS "Users can read their own conversations" ON public.health_conversations;
CREATE POLICY "Users can read their own conversations" ON public.health_conversations
    FOR SELECT USING (auth.uid() = user_id);

DROP POLICY IF EXISTS "Users can insert their own conversations" ON public.health_conversations;
CREATE POLICY "Users can insert their own conversations" ON public.health_conversations
    FOR INSERT WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS "Users can update their own conversations" ON public.health_conversations;
CREATE POLICY "Users can update their own conversations" ON public.health_conversations
    FOR UPDATE USING (auth.uid() = user_id);

DROP POLICY IF EXISTS "Users can delete their own conversations" ON public.health_conversations;
CREATE POLICY "Users can delete their own conversations" ON public.health_conversations
    FOR DELETE USING (auth.uid() = user_id);

-- health_messages policies
DROP POLICY IF EXISTS "Users can read messages in their conversations" ON public.health_messages;
CREATE POLICY "Users can read messages in their conversations" ON public.health_messages
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM public.health_conversations
            WHERE id = conversation_id AND user_id = auth.uid()
        )
    );

DROP POLICY IF EXISTS "Users can insert messages into their conversations" ON public.health_messages;
CREATE POLICY "Users can insert messages into their conversations" ON public.health_messages
    FOR INSERT WITH CHECK (
        auth.uid() = user_id AND
        EXISTS (
            SELECT 1 FROM public.health_conversations
            WHERE id = conversation_id AND user_id = auth.uid()
        )
    );

DROP POLICY IF EXISTS "Users can delete messages in their conversations" ON public.health_messages;
CREATE POLICY "Users can delete messages in their conversations" ON public.health_messages
    FOR DELETE USING (
        EXISTS (
            SELECT 1 FROM public.health_conversations
            WHERE id = conversation_id AND user_id = auth.uid()
        )
    );
