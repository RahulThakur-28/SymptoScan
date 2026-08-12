CREATE TABLE IF NOT EXISTS public.emergency_contacts (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    name text NOT NULL,
    phone_number text NOT NULL,
    relationship text NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    UNIQUE(user_id)
);

ALTER TABLE public.emergency_contacts ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can manage their own emergency contact" ON public.emergency_contacts
    FOR ALL USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);
