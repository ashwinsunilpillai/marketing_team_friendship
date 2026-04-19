USE marketing_erp;

-- Add campaign_type column to campaigns table if it doesn't exist
ALTER TABLE campaigns 
ADD COLUMN campaign_type VARCHAR(50) DEFAULT 'EMAIL' 
AFTER leads_generated;
