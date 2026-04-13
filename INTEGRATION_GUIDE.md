# Integration Guide for Member 2 & 3

## For Member 2 (Email & CRM Integration)

### What You'll Use from Member 1

1. **Entity Classes** (directly import)
   ```java
   import com.marketing.entity.Campaign;
   import com.marketing.entity.Customer;
   import com.marketing.entity.Segment;
   ```

2. **Database Utility** (for your own DB operations)
   ```java
   import com.marketing.util.DBUtil;
   Connection conn = DBUtil.getInstance().getConnection();
   ```

3. **Available Facades** (reference implementation)
   - `CampaignFacade` - Shows pattern for CRUD + status management
   - `SegmentManager` - Shows pattern for entity management

4. **Exception Handling** - Follow the same pattern:
   ```java
   import com.marketing.exception.*;
   
   // Throw relevant exceptions in your code
   // Members 1, 2, 3 handle their own exceptions
   ```

### Database Tables You'll Extend

The following tables exist and you can add columns:
- **campaigns** - Add email_sent_count, last_email_date, etc.
- **customers** - Email already has email column, add unsubscribed flag, etc.
- **New tables**: emails, email_templates, crm_sync_log, leads

### Your Responsibility

Create new tables in a migration script:
```sql
-- Add to your own setup file
CREATE TABLE IF NOT EXISTS email_templates (...)
CREATE TABLE IF NOT EXISTS leads (...)
CREATE TABLE IF NOT EXISTS crm_connections (...)
```

Do NOT modify Member 1's schema.sql or existing table structures.

---

## For Member 3 (Analytics & Reporting)

### What You'll Use from Member 1 & Member 2

1. **Campaign Data Access**
   ```java
   import com.marketing.entity.Campaign;
   import com.marketing.util.DBUtil;
   import com.marketing.facade.CampaignFacade;
   
   CampaignFacade campaignFacade = new CampaignFacade();
   List<Campaign> campaigns = campaignFacade.getAllCampaigns();
   ```

2. **Campaign Metrics** (already available in Campaign entity)
   - `impressions` - Track views
   - `clicks` - Click-throughs
   - `conversions` - Sales/conversions
   - `budget` - Link to ROI calculations

3. **Customer & Segment Data**
   - Use `Customer` entity to get segmentation for analytics
   - Use `Segment` entity to group analytics by segment type

4. **Exception Handling**
   ```java
   try {
       Campaign campaign = campaignFacade.getCampaignById(campaignId);
   } catch (CampaignNotFoundException e) {
       // Handle gracefully
   }
   ```

### Your Responsibility

Create analytics tables:
```sql
CREATE TABLE campaign_analytics (...)
CREATE TABLE daily_metrics (...)
CREATE TABLE reports (...)
CREATE TABLE dashboards (...)
```

Implement Observer pattern for live updates when campaign metrics change.

---

## Key Integration Points

### Database Connection
All members use the same `DBUtil.getInstance()` singleton:
```java
// This is thread-safe and reuses connections
Connection conn = DBUtil.getInstance().getConnection();
```

### Entity Objects
Pass entity objects between members:
```java
// Member 1 creates
Campaign campaign = campaignFacade.getCampaignById(1);

// Member 2 uses it
lead.setAssociatedCampaign(campaign);

// Member 3 analyzes it
analytics.calculateROI(campaign.getBudget(), campaign.getConversions());
```

### Exception Flow
Each member handles their own exceptions. For cross-module calls:
```java
try {
    // Call to another member's facade
    Campaign campaign = campaignFacade.getCampaignById(campaignId);
} catch (CampaignNotFoundException e) {
    // Member 3 handles gracefully
    logger.warn("Campaign not found, showing aggregate stats instead");
}
```

---

## Testing Integration

### Manual Testing Workflow
1. Run Member 1's UI to create sample campaigns
2. Member 2 should be able to query campaigns and create leads
3. Member 3 should be able to generate reports on existing campaigns

### Sample Integration Code
```java
// Test code that uses all 3 members
public class IntegrationTest {
    public static void main(String[] args) throws Exception {
        // Member 1 - Get campaign
        CampaignFacade facade = new CampaignFacade();
        Campaign campaign = facade.getCampaignById(1);
        System.out.println("Campaign: " + campaign.getCampaignName());
        
        // Member 2 - Get leads for campaign
        // LeadTracker.getLeadsForCampaign(campaign.getId());
        
        // Member 3 - Generate analytics
        // AnalyticsEngine.calculateMetrics(campaign);
    }
}
```

---

## Common Mistakes to Avoid

❌ **DO NOT:**
- Modify Member 1's entity classes without discussion
- Add new columns to Member 1's tables directly
- Hardcode database credentials (use DBUtil)
- Create new driver jars (reuse mysql-connector-j-9.6.0.jar)
- Catch and ignore exceptions silently

✅ **DO:**
- Create your own migration/schema files
- Use interfaces for your components (IEmailService, etc.)
- Document your new exceptions clearly
- Keep exception messages consistent
- Test integration with Member 1's test data

---

## Questions?

If you need to extend Member 1's entities:
1. Add getter/setter in the entity class
2. Update the schema.sql (add column migration)
3. Update the facade's mapResultSetToEntity method
4. Add exception handling if needed

This ensures clean, non-breaking integration across all three members!
