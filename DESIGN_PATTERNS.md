# Design Patterns Reference

This document explains the design patterns used in Member 1's code and how Members 2 & 3 should follow similar patterns.

---

## 1. Singleton Pattern (Creational)

### Used In: `DBUtil.java`

**Purpose:** Ensure only one shared ERP facade manager exists throughout the application.

**Implementation:**
```java
public class DBUtil {
    private static DBUtil instance;
    
    private DBUtil() { } // Private constructor
    
    public static synchronized DBUtil getInstance() {
        if (instance == null) {
            instance = new DBUtil();
        }
        return instance;
    }
}
```

**Usage:**
```java
// Anywhere in your code
Object marketingSubsystem = DBUtil.getInstance().getMarketingSubsystem();
```

**Key Points:**
- Thread-safe with `synchronized` keyword
- Private constructor prevents instantiation
- Always returns same instance
- Perfect for shared resources (DB connections, configuration)

**Members 2 & 3:** Follow this pattern for:
- Email service configuration (EmailServiceConfig)
- CRM connection manager (CRMConnectionManager)
- Logging/Analytics repository

---

## 2. Factory Method Pattern (Creational)

### Used In: `SegmentFactory.java`

**Purpose:** Create different types of segments without exposing creation logic to client code.

**Implementation:**
```java
public class SegmentFactory {
    public static Segment createSegment(String type, String criteria) 
            throws InvalidSegmentCriteriaException {
        switch(type.toUpperCase()) {
            case "CITY":
                return createCitySegment(criteria);
            case "AGE_GROUP":
                return createAgeGroupSegment(criteria);
            case "INTEREST":
                return createInterestSegment(criteria);
            default:
                throw new InvalidSegmentCriteriaException("Invalid type: " + type);
        }
    }
}
```

**Usage:**
```java
try {
    Segment segment = SegmentFactory.createSegment("CITY", "Bangalore");
} catch (InvalidSegmentCriteriaException e) {
    // Handle error
}
```

**Key Points:**
- Static factory method
- Validation inside factory
- Type-specific creation logic encapsulated
- Easy to add new types (OCP principle)

**Members 2 & 3 should create:**
- `LeadStateFactory` - Create different lead states (new, contacted, qualified, etc.)
- `ReportTypeFactory` - Create different report types (PDF, HTML, CSV)
- `EmailTemplateFactory` - Create email templates by category

---

## 3. Facade Pattern (Structural)

### Used In: `CampaignFacade.java` and `SegmentManager.java`

**Purpose:** Provide simplified unified interface to complex subsystem (JDBC operations).

**Implementation:**
```java
public class CampaignFacade {
    private DBUtil dbUtil;
    
    public CampaignFacade() {
        this.dbUtil = DBUtil.getInstance();
    }
    
    public boolean createCampaign(Campaign campaign) 
            throws CampaignCreationException {
        // Complex subsystem facade calls hidden here
        Object marketingSubsystem = dbUtil.getMarketingSubsystem();
        // create/read/update/delete calls go through shared facade
    }
}
```

**Usage:**
```java
CampaignFacade facade = new CampaignFacade();
facade.createCampaign(newCampaign); // Simple interface
```

**Key Points:**
- Hides complexity of shared facade operations
- One facade per major entity
- Try-with-resources for resource management
- Clear exception handling
- Supports GRASP Information Expert principle

**Members 2 & 3 should create:**
- `EmailServiceFacade` - Send emails, manage templates
- `CRMConnectorFacade` - Fetch customers, sync data
- `AnalyticsEngineFacade` - Calculate metrics, generate reports
- `ReportGeneratorFacade` - Build and export reports

---

## 4. Observer Pattern (Behavioral)

### For Member 3: `AnalyticsEngine.java`

**Purpose:** Automatically update analytics when campaign metrics change.

**Implementation Pattern:**
```java
// Subject
public class Campaign {
    private List<Observer> observers = new ArrayList<>();
    
    public void attach(Observer observer) {
        observers.add(observer);
    }
    
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update(this);
        }
    }
    
    public void setConversions(int conversions) {
        this.conversions = conversions;
        notifyObservers(); // Update all analytics
    }
}

// Observer
public interface Observer {
    void update(Campaign campaign);
}

public class AnalyticsObserver implements Observer {
    @Override
    public void update(Campaign campaign) {
        // Recalculate CTR, ROI, etc.
        double ctr = (double) campaign.getClicks() / campaign.getImpressions();
    }
}
```

**Usage:**
```java
Campaign campaign = facade.getCampaignById(1);
AnalyticsObserver observer = new AnalyticsObserver();
campaign.attach(observer);

// When metrics change
campaign.setConversions(150); // Observer automatically updates
```

**Key Points:**
- Loose coupling between campaign and analytics
- Multiple observers can listen
- Automatic updates when data changes
- Perfect for live dashboards

---

## 5. Template Method Pattern (Behavioral)

### For Member 2: `AbstractEmailService.java`

**Purpose:** Define skeleton of email sending process, letting subclasses override specific steps.

**Implementation Pattern:**
```java
public abstract class AbstractEmailService {
    // Template method - defines the algorithm
    public final void sendEmail(String to, String subject, String body) 
            throws EmailSendException {
        if (validateEmail(to)) {
            String renderedTemplate = renderTemplate(body);
            boolean sent = performSend(to, subject, renderedTemplate);
            if (sent) {
                logSuccess(to);
            }
        }
    }
    
    protected abstract boolean performSend(String to, String subject, String body);
    
    protected abstract void logSuccess(String to);
    
    protected String renderTemplate(String template) {
        // Default implementation
        return template;
    }
    
    protected boolean validateEmail(String email) {
        return email != null && email.contains("@");
    }
}

// Concrete implementation
public class SMTPEmailService extends AbstractEmailService {
    @Override
    protected boolean performSend(String to, String subject, String body) {
        // SMTP-specific logic
    }
    
    @Override
    protected void logSuccess(String to) {
        // SMTP-specific logging
    }
}
```

**Key Points:**
- Defines algorithm structure in superclass
- Subclasses implement specific steps
- Prevents algorithm modification
- Enforces consistent process

---

## 6. State Pattern (Behavioral)

### For Member 2: `LeadLifecycle.java`

**Purpose:** Allow lead to change behavior as its internal state changes (new → contacted → qualified → converted).

**Implementation Pattern:**
```java
// State interface
public interface LeadState {
    void contactLead(Lead lead);
    void qualifyLead(Lead lead) throws InvalidStateException;
    void convertLead(Lead lead) throws InvalidStateException;
    String getStateDescription();
}

// Concrete states
public class NewLeadState implements LeadState {
    @Override
    public void contactLead(Lead lead) {
        // Can transition from NEW to CONTACTED
        lead.setState(new ContactedLeadState());
    }
    
    @Override
    public void qualifyLead(Lead lead) throws InvalidStateException {
        throw new InvalidStateException("Cannot qualify new lead");
    }
}

public class ContactedLeadState implements LeadState {
    @Override
    public void qualifyLead(Lead lead) {
        // Can transition from CONTACTED to QUALIFIED
        lead.setState(new QualifiedLeadState());
    }
    
    @Override
    public void contactLead(Lead lead) {
        // Already contacted, do nothing
    }
}

// Context
public class Lead {
    private LeadState currentState;
    
    public Lead() {
        this.currentState = new NewLeadState();
    }
    
    public void setState(LeadState state) {
        this.currentState = state;
    }
    
    public void contact() throws InvalidStateException {
        currentState.contactLead(this);
    }
}
```

**Usage:**
```java
Lead lead = new Lead();
lead.contact();      // Transitions to CONTACTED
lead.qualify();      // Transitions to QUALIFIED
lead.qualify();      // Throws exception - already qualified
```

**Key Points:**
- Encapsulates state-specific behavior
- Clear state transitions
- Prevents invalid transitions
- Easy to add new states

---

## 7. Builder Pattern (Behavioral)

### For Member 3: `ReportBuilder.java`

**Purpose:** Construct complex reports step-by-step through a fluent interface.

**Implementation Pattern:**
```java
public class ReportBuilder {
    private Report report = new Report();
    
    public ReportBuilder withCampaignMetrics(Campaign campaign) {
        report.addSection(new CampaignMetricsSection(campaign));
        return this;
    }
    
    public ReportBuilder withLeadSummary(List<Lead> leads) {
        report.addSection(new LeadSummarySection(leads));
        return this;
    }
    
    public ReportBuilder withKPIs(Map<String, Double> kpis) {
        report.addSection(new KPISection(kpis));
        return this;
    }
    
    public Report build() {
        if (report.getSections().isEmpty()) {
            throw new IllegalStateException("Report must have at least one section");
        }
        return report;
    }
}
```

**Usage:**
```java
Report report = new ReportBuilder()
    .withCampaignMetrics(campaign)
    .withLeadSummary(leads)
    .withKPIs(kpiMap)
    .build();
```

**Key Points:**
- Fluent interface (method chaining)
- Step-by-step construction
- Immutable final object
- Clear and readable

---

## 8. Composite Pattern (Structural)

### For Member 3: `DashboardUI.java`

**Purpose:** Compose dashboard from tree of panels (panels can contain other panels).

**Implementation Pattern:**
```java
public interface DashboardComponent {
    void render();
    void add(DashboardComponent component);
    void remove(DashboardComponent component);
}

// Leaf component
public class KPIWidget implements DashboardComponent {
    @Override
    public void render() {
        System.out.println("Rendering KPI: " + getValue());
    }
    
    @Override
    public void add(DashboardComponent c) {
        // Leaves can't have children
    }
}

// Composite component
public class DashboardPanel implements DashboardComponent {
    private List<DashboardComponent> components = new ArrayList<>();
    
    @Override
    public void render() {
        for (DashboardComponent component : components) {
            component.render();
        }
    }
    
    @Override
    public void add(DashboardComponent component) {
        components.add(component);
    }
    
    @Override
    public void remove(DashboardComponent component) {
        components.remove(component);
    }
}
```

**Usage:**
```java
DashboardPanel mainDash = new DashboardPanel();
DashboardPanel metricsPanel = new DashboardPanel();

metricsPanel.add(new KPIWidget("CTR"));
metricsPanel.add(new KPIWidget("ROI"));

mainDash.add(metricsPanel);
mainDash.add(new ChartPanel());

mainDash.render(); // Renders entire tree
```

**Key Points:**
- Recursive composition
- Uniform treatment of leaf and composite
- Easy to add new widget types
- Flexible hierarchies

---

## SOLID Principles Applied

| Principle | Implementation | Example |
|-----------|-----------------|---------|
| **S**RP | Each class has one responsibility | CampaignFacade only manages campaigns |
| **O**CP | Open for extension, closed for modification | SegmentFactory easily adds new types |
| **L**SP | Subtypes substitutable for base types | All LeadState impls follow contract |
| **I**SP | Segregated interfaces | Observer interface with single method |
| **D**IP | Depend on abstractions, not concretions | CampaignFacade uses DBUtil abstraction |

---

## GRASP Principles Applied

| Principle | Implementation | Example |
|-----------|-----------------|---------|
| **Information Expert** | Assign responsibility to class with info | Campaign knows its own data |
| **Creator** | Who should create objects? | Facade creates entity objects |
| **Controller** | Who handles system events? | UI panels are controllers |
| **Low Coupling** | Minimize dependencies | DBUtil isolated behind interface |
| **High Cohesion** | Keep related things together | All campaign logic in CampaignFacade |
| **Polymorphism** | Use type-specific behavior | EmailService subclasses differ |
| **Pure Fabrication** | Create classes for responsibilities | DBUtil handles DB concerns |

---

## Exception Handling Pattern

All members should follow this pattern:

```java
public class YourFacade {
    public void yourMethod() throws YourCustomException {
        try {
            // Perform operation
        } catch (SQLException e) {
            throw new YourCustomException("Descriptive message", e);
        }
    }
}
```

**Usage:**
```java
try {
    facade.yourMethod();
} catch (YourCustomException e) {
    logger.error("Operation failed: " + e.getMessage());
    // Handle gracefully
}
```

---

## Summary

- **Member 1** provides foundation with Singleton, Factory, and Facade patterns
- **Member 2** adds Template Method, State, and Adapter patterns for email/CRM
- **Member 3** adds Observer, Builder, and Composite patterns for analytics/reporting

Each member maintains:
- ✅ Consistent exception handling
- ✅ SOLID and GRASP principles
- ✅ Design pattern documentation
- ✅ Clear separation of concerns
- ✅ Thread-safe operations
