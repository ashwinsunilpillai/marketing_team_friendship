# Setup Guide for Team Members

## Quick Start (For Members 2 & 3)

### Prerequisites
- Java 8 or higher
- MySQL Server 5.7+ (with mysql-connector-j-9.6.0.jar available)
- VS Code or any Java IDE

### Step 1: Clone & Navigate
```bash
git clone <repo-url>
cd marketing_project
```

### Step 2: Database Setup
```bash
# Option A: Using MySQL command line
mysql -u root -p < schema.sql

# Option B: Using MySQL Workbench
# Open Workbench → File → Open SQL Script → schema.sql → Execute
```

### Step 3: Configure DBUtil (if needed)
Edit `src/com/marketing/util/DBUtil.java` if your MySQL is not on localhost:3306 or you have different credentials:

```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/marketing_erp";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "Ashwinpillai4$";
```

### Step 4: Compile
```powershell
# PowerShell (Windows)
$files = Get-ChildItem -Path src -Recurse -Filter "*.java"
javac -d bin -cp ".:mysql-connector-j-9.6.0.jar" $files.FullName
```

```bash
# Bash/Linux/Mac
javac -d bin -cp ".:mysql-connector-j-9.6.0.jar" $(find src -name "*.java")
```

### Step 5: Run (Member 1's UI)
```powershell
# Windows
java -cp "bin;mysql-connector-j-9.6.0.jar" com.marketing.ui.MarketingERP
```

```bash
# Linux/Mac
java -cp "bin:mysql-connector-j-9.6.0.jar" com.marketing.ui.MarketingERP
```

---

## Member 2 Specific Setup

After cloning Member 1's code:

1. **Stay in the same project structure**
   ```
   src/com/marketing/
   ├── email/              ← Add your package
   │   ├── service/
   │   ├── template/
   │   └── exception/
   ├── crm/                ← Add your package
   │   ├── connector/
   │   └── exception/
   └── lead/               ← Add your package
       ├── tracker/
       ├── state/
       └── ui/
   ```

2. **Create your own schema migration file**
   - Create `schema_member2.sql`
   - Add your tables (email_templates, leads, etc.)
   - Do NOT modify schema.sql

3. **Import Member 1's classes**
   ```java
   import com.marketing.util.DBUtil;
   import com.marketing.entity.Campaign;
   import com.marketing.facade.CampaignFacade;
   import com.marketing.exception.*;
   ```

4. **Follow the same pattern as Member 1**
   - Use Facade pattern for complexity
   - Create custom exceptions
   - Use proper logging
   - Test before pushing

---

## Member 3 Specific Setup

After cloning Member 1 & 2's code:

1. **Add analytics package**
   ```
   src/com/marketing/
   ├── analytics/
   │   ├── engine/
   │   ├── calculator/
   │   ├── observer/
   │   └── ui/
   └── reporting/
       ├── generator/
       ├── builder/
       └── ui/
   ```

2. **Create analytics schema migration**
   - Create `schema_member3.sql`
   - Add analytics tables (campaign_analytics, daily_metrics, reports, dashboards)
   - Reference campaign and lead data

3. **Import from Members 1 & 2**
   ```java
   // Member 1
   import com.marketing.util.DBUtil;
   import com.marketing.entity.Campaign;
   import com.marketing.facade.CampaignFacade;
   
   // Member 2
   import com.marketing.lead.tracker.LeadTracker;
   import com.marketing.email.service.EmailService;
   ```

4. **Implement Observer pattern**
   - Subscribe to campaign metric changes
   - Auto-calculate CTR, ROI, etc.
   - Publish updates to dashboard

---

## Development Workflow

### For Compilation with Multiple Members

When all members' code is merged:
```powershell
# Compile everything at once
$files = Get-ChildItem -Path src -Recurse -Filter "*.java"
javac -d bin -cp ".:mysql-connector-j-9.6.0.jar" $files.FullName
```

### For Testing Individual Modules

```bash
# Compile only a package
javac -d bin -cp ".:mysql-connector-j-9.6.0.jar" src/com/marketing/facade/*.java

# Run a specific class
java -cp "bin;mysql-connector-j-9.6.0.jar" com.marketing.ui.MarketingERP
```

---

## Database Management

### Check Current Schema
```sql
USE marketing_erp;
SHOW TABLES;
DESCRIBE campaigns;
```

### Add Your Tables
```sql
USE marketing_erp;
-- Add your member-specific tables here
ALTER TABLE campaigns ADD COLUMN email_sent_count INT DEFAULT 0;
```

### Backup Before Major Changes
```bash
mysqldump -u root -p marketing_erp > backup_$(date +%Y%m%d).sql
```

---

## Common Issues & Solutions

| Issue | Solution |
|-------|----------|
| "Database connection failed" | Check MySQL running, verify credentials in DBUtil.java |
| "JDBC driver not found" | Ensure mysql-connector-j-9.6.0.jar is in project root |
| "Table not found" | Run schema.sql: `mysql -u root -p < schema.sql` |
| "Port 3306 already in use" | Change MySQL port or stop other MySQL instances |
| "Access denied for user" | Update DBUtil.java with correct username/password |
| PowerShell glob pattern error | Use `Get-ChildItem -Recurse` for file expansion (not `**/*.java`) |

---

## For Git Workflow

### Before Each Push
```bash
# Compile your code
$files = Get-ChildItem -Path src -Recurse -Filter "*.java"
javac -d bin -cp ".:mysql-connector-j-9.6.0.jar" $files.FullName

# Run basic test
java -cp "bin;mysql-connector-j-9.6.0.jar" com.marketing.ui.MarketingERP

# Then commit
git add src/ README.md INTEGRATION_GUIDE.md
git commit -m "Feature: Add email service with template pattern"
git push
```

### Branch Strategy
```bash
# Create feature branch
git checkout -b feature/email-service

# Do your work, test locally
# ...

# Merge after testing
git checkout main
git merge feature/email-service
git push
```

---

## Questions During Integration?

1. **Can I modify Member 1's code?** 
   - Only bugfixes or agreed enhancements. Ask first!

2. **Where should my exceptions go?**
   - Create `src/com/marketing/<yourmodule>/exception/` folder

3. **How do I test without UI?**
   - Create a demo/test class with main() method

4. **What if I need new columns in campaigns table?**
   - Discuss with Member 1, add getter/setter to Campaign.java, update schema migration

5. **How do I handle database transactions?**
   - Use try-with-resources for Connection/Statement
   - Let DBUtil manage the connection lifecycle

---

Happy coding! 🚀
