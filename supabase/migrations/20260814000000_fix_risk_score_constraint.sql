-- Cleanup invalid risk scores before adding constraint
-- We set values > 100 or < 0 to NULL so they can be identified as invalid rather than fake medical data.
UPDATE public.assessment_results
SET risk_score = NULL
WHERE risk_score < 0 OR risk_score > 100;

-- Add check constraint to ensure risk_score is always 0-100
ALTER TABLE public.assessment_results
ADD CONSTRAINT risk_score_range CHECK (risk_score IS NULL OR (risk_score >= 0 AND risk_score <= 100));

-- Ensure assessment_id is unique so upsert works correctly as an idempotency mechanism
-- If it's already a primary key or has a unique constraint, this will be redundant but safe to check.
-- Note: In SymptoScan, one assessment should have exactly one result.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'assessment_results_assessment_id_key'
    ) THEN
        ALTER TABLE public.assessment_results ADD CONSTRAINT assessment_results_assessment_id_key UNIQUE (assessment_id);
    END IF;
END $$;
