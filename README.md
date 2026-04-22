# Marketing ERP Subsystem - Member 1 (Foundation Layer)

## Overview
This is Member 1's implementation of the Marketing ERP subsystem for a group project. This layer serves as the foundation that other team members depend on. It includes:

- **DB Utility & Core Entities** - Database connection management and core model classes
- **Campaign Manager** - CRUD operations for marketing campaigns
- **Customer Segmentation** - Factory-based segment creation and management

## Architecture & Design Patterns

### Creational Patterns
- **Singleton (DBUtil)** - Single shared database connection source
- **Factory Method (SegmentFactory)** - Creates different segment types (CITY, AGE_GROUP, INTEREST)

### Structural Patterns
- **Facade (CampaignFacade, SegmentManager)** - Simplifies subsystem data operations into unified interfaces

### GRASP Principles Applied
- **Information Expert** - Entity classes and Facade classes know their own data
- **Creator** - Facade classes create entity objects
- **Controller** - UI panels control business logic
- **Low Coupling** - Clear separation between DB layer, business logic, and UI

### SOLID Principles Applied
- **SRP (Single Responsibility)** - Each class has one reason to change
- **OCP (Open/Closed)** - SegmentFactory can add new segment types without modifying existing code
- **Interface Segregation** - Separate concerns into distinct classes

## Project Structure

```
src/
├── com/marketing/
│   ├── util/
│   │   └── DBUtil.java                 # Singleton DB connection manager
│   ├── entity/
│   │   ├── Campaign.java               # Campaign data model
│   │   ├── Customer.java               # Customer data model
│   │   └── Segment.java                # Segment data model
│   ├── exception/
│   │   ├── CampaignCreationException.java
│   │   ├── CampaignNotFoundException.java
│   │   ├── CampaignStateException.java
│   │   ├── InvalidSegmentCriteriaException.java
│   │   ├── SegmentNotFoundException.java
│   │   └── EmptySegmentException.java
│   ├── facade/
│   │   ├── CampaignFacade.java         # Campaign CRUD operations
│   │   └── SegmentManager.java         # Segment CRUD operations
│   ├── factory/
│   │   └── SegmentFactory.java         # Factory for creating segments
│   └── ui/
│       ├── MarketingERP.java           # Main application frame
│       └── CampaignManagerPanel.java   # Campaign management UI
schema.sql                              # Database schema initialization
```

## Setup Instructions

This project now follows the IntegrationDB handoff workflow in `IntegrationDB/README_FOR_OTHER_TEAMS.md`.

### 1. Configure Database Properties
1. Ensure local MySQL 8+ is running.
2. Keep `database.properties` in the project root.
3. Set local credentials and keep `db.name=erp_subsystem`.

### 2. Compile the Project (PowerShell)
```powershell
$compileCp = ".;IntegrationDB\local-database-module-1.0.0.jar;IntegrationDB\lib\mysql-connector-j-9.3.0.jar;IntegrationDB\lib\slf4j-api-2.0.17.jar;IntegrationDB\lib\slf4j-simple-2.0.17.jar"
$files = Get-ChildItem -Path .\src -Recurse -Filter "*.java"
javac -cp $compileCp -d bin $files.FullName
```

### 3. Run the Application (PowerShell)
```powershell
$runCp = ".;bin;IntegrationDB\local-database-module-1.0.0.jar;IntegrationDB\lib\mysql-connector-j-9.3.0.jar;IntegrationDB\lib\slf4j-api-2.0.17.jar;IntegrationDB\lib\slf4j-simple-2.0.17.jar"
java -cp $runCp com.marketing.ui.MarketingERP
```

## Key Classes

### DBUtil (Singleton)
- **Purpose**: Provides access to shared ERP facade and subsystem facade entry points
- **Methods**:
  - `getInstance()` - Get singleton instance
   - `getMarketingSubsystem()` - Get shared marketing subsystem facade
  - `closeConnection()` - Close connection

### Entity Classes
- **Campaign**: Represents a marketing campaign
- **Customer**: Represents a customer
- **Segment**: Represents a customer segment

### CampaignFacade
Provides unified interface for campaign operations:
- `createCampaign(Campaign)`
- `getCampaignById(int)`
- `getAllCampaigns()`
- `updateCampaign(Campaign)`
- `deleteCampaign(int)`
- `changeCampaignStatus(int, String)`

### SegmentFactory
Creates different segment types:
- Creates CITY-based segments
- Creates AGE_GROUP-based segments (18-25, 25-35, 35-50, 50+)
- Creates INTEREST-based segments

### SegmentManager
Provides CRUD operations for segments:
- `createSegment(Segment)`
- `getSegmentById(int)`
- `getAllSegments()`
- `updateSegment(Segment)`
- `deleteSegment(int)`

### CampaignManagerPanel
Swing UI for managing campaigns with features:
- View all campaigns in a table
- Create new campaigns
- Update existing campaigns
- Delete campaigns
- Auto-populate fields when selecting a campaign

## Exception Handling

The following custom exceptions are implemented:

1. **CampaignCreationException** - When campaign creation fails
2. **CampaignNotFoundException** - When a campaign is not found
3. **CampaignStateException** - When invalid state transition is attempted
4. **InvalidSegmentCriteriaException** - When segment criteria is invalid
5. **SegmentNotFoundException** - When a segment is not found
6. **EmptySegmentException** - When a segment has no customers

## How Other Members Will Use This

### Member 2 (Email & CRM Integration)
- Will import `Campaign`, `Customer`, `Segment` entity classes
- Will use `DBUtil` for database access
- Will extend the campaign and customer tables with email/CRM data

### Member 3 (Analytics & Reporting)
- Will use campaign and customer data from Member 1 and Member 2
- Will create analytics tables based on the campaign structure
- Will generate reports using campaign metrics (impressions, clicks, conversions)

## Sample Database Queries

```sql
-- Get all active campaigns
SELECT * FROM campaigns WHERE status = 'ACTIVE';

-- Get customers by city
SELECT * FROM customers WHERE city = 'Bangalore';

-- Get segments by type
SELECT * FROM segments WHERE segment_type = 'CITY';

-- Join campaigns with segments
SELECT c.*, s.segment_name 
FROM campaigns c 
JOIN segments s ON c.segment_id = s.segment_id;
```

## Testing

To test the application:
1. Run the application
2. The Campaign Manager Panel should display existing campaigns
3. Try creating a new campaign with segment ID 1
4. Try updating and deleting campaigns
5. Watch the database updates in real-time

## Future Enhancements

- Add validation for date ranges (start date < end date)
- Implement budget validation
- Add search/filter functionality
- Export campaign data to CSV
- Add analytics dashboard
- Implement audit logging

## Notes

- All database operations use try-with-resources for proper connection handling
- The table automatically refreshes after CRUD operations
- Date parsing uses Java 8 LocalDate
- Exception handling is comprehensive with detailed error messages
- UI uses MVC pattern with clear separation of concerns

## Troubleshooting

**Issue**: "No database connection"
- **Solution**: Ensure MySQL is running and credentials in DBUtil.java are correct

**Issue**: "Table not found"
- **Solution**: Run `schema.sql` to create tables

**Issue**: "JDBC driver not found"
- **Solution**: Ensure `mysql-connector-j-9.6.0.jar` is in the classpath

---

Created as part of OOAD Group Project - Marketing ERP Subsystem
