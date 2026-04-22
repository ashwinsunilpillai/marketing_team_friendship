# Setup Guide (Shared IntegrationDB Mode)

This project is configured to follow the shared IntegrationDB flow.

Use this file together with IntegrationDB/README_FOR_OTHER_TEAMS.md.

## Required Runtime

- Java 21
- Local MySQL 8+
- IntegrationDB folder with all required jars

## Required Files

Keep this structure under project root:

```text
marketing_team_friendship/
  IntegrationDB/
    local-database-module-1.0.0.jar
    database-template.properties
    README_FOR_OTHER_TEAMS.md
    lib/
      mysql-connector-j-9.3.0.jar
      slf4j-api-2.0.17.jar
      slf4j-simple-2.0.17.jar
  database.properties
  src/
  bin/
```

## Step 1. Configure Local Database Credentials

From project root, copy template if needed:

```powershell
Copy-Item ".\IntegrationDB\database-template.properties" ".\database.properties" -Force
```

Then update database.properties with your local MySQL credentials.

Important:

- Keep db.name as erp_subsystem for shared module compatibility.
- Do not hardcode credentials in source files.

## Step 2. Compile

PowerShell from project root:

```powershell
$compileCp = ".;IntegrationDB\local-database-module-1.0.0.jar;IntegrationDB\lib\mysql-connector-j-9.3.0.jar;IntegrationDB\lib\slf4j-api-2.0.17.jar;IntegrationDB\lib\slf4j-simple-2.0.17.jar"
$files = Get-ChildItem -Path .\src -Recurse -Filter "*.java"
javac -cp $compileCp -d bin $files.FullName
```

## Step 3. Run

```powershell
$runCp = ".;bin;IntegrationDB\local-database-module-1.0.0.jar;IntegrationDB\lib\mysql-connector-j-9.3.0.jar;IntegrationDB\lib\slf4j-api-2.0.17.jar;IntegrationDB\lib\slf4j-simple-2.0.17.jar"
java -cp $runCp com.marketing.ui.MarketingERP
```

## Notes

- Do not use raw JDBC connection code for subsystem operations.
- Do not manually bootstrap the shared ERP schema with ad-hoc SQL for normal usage.
- Use DBUtil -> marketingSubsystem facade operations for create/read/update/delete.
