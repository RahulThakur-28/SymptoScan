-- Add image_url to assessments table
ALTER TABLE public.assessments ADD COLUMN IF NOT EXISTS image_url text;

-- Create a bucket for assessment images
INSERT INTO storage.buckets (id, name, public)
VALUES ('assessment-images', 'assessment-images', false)
ON CONFLICT (id) DO NOTHING;

-- RLS for storage bucket
CREATE POLICY "Users can upload their own assessment images"
ON storage.objects FOR INSERT TO authenticated
WITH CHECK (bucket_id = 'assessment-images' AND (storage.foldername(name))[1] = auth.uid()::text);

CREATE POLICY "Users can view their own assessment images"
ON storage.objects FOR SELECT TO authenticated
USING (bucket_id = 'assessment-images' AND (storage.foldername(name))[1] = auth.uid()::text);

CREATE POLICY "Users can delete their own assessment images"
ON storage.objects FOR DELETE TO authenticated
USING (bucket_id = 'assessment-images' AND (storage.foldername(name))[1] = auth.uid()::text);
