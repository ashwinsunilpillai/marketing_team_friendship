# Readme For Other Teams

You will receive one folder named:

`IntegrationDB`

Use this module with the MySQL server running on your own machine.

Do not connect to the ERP schema directly with raw JDBC. Use this shared module only.

This module is intentionally configured to avoid missing-permission failures on local team machines.
If you need a table for your subsystem work, use the subsystem facade first.

## Files You Should Receive

Inside `IntegrationDB`, you should receive these files:

1. `local-database-module-1.0.0.jar`
2. `database-template.properties`
3. `README_FOR_OTHER_TEAMS.md`
4. `lib/mysql-connector-j-9.3.0.jar`
5. `lib/slf4j-api-2.0.17.jar`
6. `lib/slf4j-simple-2.0.17.jar`

## What You Need

You need:

1. Java 21
2. local MySQL 8+ running on your PC
3. a local MySQL user that can create/drop databases and tables during first bootstrap

## Folder Setup

Put the `IntegrationDB` folder inside your own subsystem project folder.

Example:

```text
YourProject/
  IntegrationDB/
  src/
  bin/
```

Open PowerShell in your project folder:

```powershell
cd "C:\Path\To\YourProject"
```

## Step 1. Create Your Config File

Copy `database-template.properties` to `database.properties`.

Run:

```powershell
cd "C:\Path\To\YourProject"
Copy-Item ".\IntegrationDB\database-template.properties" ".\database.properties" -Force
```

Then open `database.properties` and fill in your own local MySQL settings.

Example:

```properties
db.host=127.0.0.1
db.port=3306
db.name=erp_subsystem
db.username=root
db.password=your_local_mysql_password
db.pool.size=4
```

Important:

- `db.username` and `db.password` must be your real local MySQL credentials
- do not leave placeholder values unchanged
- keep `database.properties` in your project root, the same place where you run `java`

## Step 2. Add The JARs To Your Project

Add:

- `local-database-module-1.0.0.jar`
- `mysql-connector-j-9.3.0.jar`
- `slf4j-api-2.0.17.jar`
- `slf4j-simple-2.0.17.jar`

If your subsystem is a plain Java project with `src` and `bin` folders, use these commands:

```powershell
cd "C:\Path\To\YourProject"
$compileCp = ".;IntegrationDB\local-database-module-1.0.0.jar;IntegrationDB\lib\mysql-connector-j-9.3.0.jar;IntegrationDB\lib\slf4j-api-2.0.17.jar;IntegrationDB\lib\slf4j-simple-2.0.17.jar"
javac -cp $compileCp -d bin (Get-ChildItem -Recurse .\src\*.java | ForEach-Object { $_.FullName })

$runCp = ".;bin;IntegrationDB\local-database-module-1.0.0.jar;IntegrationDB\lib\mysql-connector-j-9.3.0.jar;IntegrationDB\lib\slf4j-api-2.0.17.jar;IntegrationDB\lib\slf4j-simple-2.0.17.jar"
java -cp $runCp your.main.Class
```

Replace `your.main.Class` with your real entry class, for example `com.team.crm.Main`.

If you are not compiling your own app yet and only want to test the database module itself first, run this directly:

```powershell
cd "C:\Path\To\YourProject"
java -cp ".;IntegrationDB\local-database-module-1.0.0.jar;IntegrationDB\lib\*" com.likeseca.erp.database.facade.DemoApplication
```



## Step 3. Create The Facade In Your Code

Example:

```java
import com.likeseca.erp.database.facade.ErpDatabaseFacade;

import java.util.Map;

public class TeamApp {
    public static void main(String[] args) {
        try (ErpDatabaseFacade facade = new ErpDatabaseFacade()) {
            System.out.println(facade.crmSubsystem().readAll("customers", Map.of()));
        }
    }
}
```

## What Happens Automatically

The first time your code creates `ErpDatabaseFacade`, the module will:

1. connect to your local MySQL server
2. create the database `erp_subsystem` if needed
3. load the full shared ERP schema
4. seed the integration registry and permission matrix
5. return the subsystem facades and adapters

You do not need to manually run SQL in normal usage.

The bundled permission setup is intentionally broad so teams do not get blocked by missing permission rows.

## Important Safety Note

If the module detects a broken or partial initialization, it may reset the local `erp_subsystem` schema and recreate it cleanly.

So:

- use `erp_subsystem` only for this project
- do not store unrelated data inside that schema

## Use Your Subsystem Facade First

Preferred subsystem facade entry points include:

- `facade.databaseIntegrationSubsystem()`
- `facade.uiSubsystem()`
- `facade.crmSubsystem()`
- `facade.marketingSubsystem()`
- `facade.salesManagementSubsystem()`
- `facade.orderProcessingSubsystem()`
- `facade.supplyChainSubsystem()`
- `facade.manufacturingSubsystem()`
- `facade.hrSubsystem()`
- `facade.projectManagementSubsystem()`
- `facade.reportingSubsystem()`
- `facade.dataAnalyticsSubsystem()`
- `facade.businessIntelligenceSubsystem()`
- `facade.automationSubsystem()`
- `facade.businessControlSubsystem()`
- `facade.financialManagementSubsystem()`
- `facade.accountingSubsystem()`

The older adapter-style methods such as `facade.crm()` still exist for compatibility, but the subsystem-facade methods are the cleaner API.

Examples:

- CRM team: `facade.crmSubsystem()`
- UI team: `facade.uiSubsystem()`
- Supply Chain team: `facade.supplyChainSubsystem()`
- HR team: `facade.hrSubsystem()`
- Reporting team: `facade.reportingSubsystem()`

## Allowed Operations

Every subsystem facade exposes these methods:

- `create(tableName, payload)`
- `readAll(tableName, filters)`
- `readById(tableName, idColumn, idValue)`
- `update(tableName, idColumn, idValue, payload)`
- `delete(tableName, idColumn, idValue)`
- `join(joinSpec)`

## Access Rules

All teams get the same local schema on their own machine.

For this handoff version, permissions are intentionally permissive so teams can work without later database-module rebuilds.

Use the shared facade and subsystem-facade methods instead of direct JDBC.

## Quick Verification

After first run, open MySQL Workbench and confirm:

- schema `erp_subsystem` exists
- the shared tables exist
- your program can read the tables it needs through the module

Useful MySQL check:

```sql
SHOW DATABASES;
USE erp_subsystem;
SHOW TABLES;
SELECT subsystem_name FROM integration_registry;
```

## If It Fails

Check these first:

1. is local MySQL running?
2. is `database.properties` correct?
3. does your MySQL user have enough privileges?
4. did you add all required JARs to classpath?
5. are you using the correct subsystem facade?

Common working test command:

```powershell
cd "C:\Path\To\YourProject"
java -cp ".;IntegrationDB\local-database-module-1.0.0.jar;IntegrationDB\lib\*" com.likeseca.erp.database.facade.DemoApplication
```

## Smallest Working Example

```java
import com.likeseca.erp.database.facade.ErpDatabaseFacade;
import java.util.Map;

public class TeamMain {
    public static void main(String[] args) {
        try (ErpDatabaseFacade facade = new ErpDatabaseFacade()) {
            System.out.println(facade.salesManagementSubsystem().readAll("customers", Map.of()));
        }
    }
}
```
