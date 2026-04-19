-- Update existing campaigns table to add lead tracking columns
USE marketing_erp;

-- Add lead_target column if it doesn't exist
ALTER TABLE campaigns ADD COLUMN lead_target INT DEFAULT 100;

-- Add leads_generated column if it doesn't exist
ALTER TABLE campaigns ADD COLUMN leads_generated INT DEFAULT 0;

-- Update existing campaigns with lead tracking data (if migration is needed)
UPDATE campaigns SET lead_target = 100, leads_generated = 45 WHERE lead_target IS NULL OR lead_target = 0;
