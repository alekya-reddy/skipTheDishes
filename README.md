# Courier Shifts Automation

Java test automation project for the Courier Shifts application. The suite
keeps browser interactions, API access, configuration, and test data in
separate components and uses the public application interfaces.

## Technology stack

- Java 25 and Maven
- JUnit 5 for test discovery, lifecycle, and API/UI tags
- Selenium WebDriver for browser-based end-to-end testing
- REST Assured for REST API requests and response validation
- Jackson Java Time support and typed Java records for JSON payloads
- Allure JUnit 5 adapter for test reporting
- SLF4J and Logback for execution and HTTP request/response logging

## Prerequisites

- JDK 25 or newer
- Maven 3.9 or newer
- Docker Desktop / Docker Compose v2 to run the application
- Google Chrome (Selenium Manager resolves a compatible driver)

### Automated setup

On Windows, run the setup script from PowerShell:

```powershell
.\setup.ps1
```

To skip optional browser or Docker Desktop installation, use:

```powershell
.\setup.ps1 -SkipChrome -SkipDocker
```

The script uses `winget` to install JDK 25, Maven, Chrome, and Docker Desktop
when missing, then downloads the Maven dependencies. Restart PowerShell after
installation if the updated PATH is not detected.

For macOS or Linux, run:

```shell
./setup.sh
```

Clone or open this repository as the framework repository. The Courier Shifts
application must be started separately from its own repository. From the
application repository root, start its Docker Compose services:

```shell
docker compose up -d --build
```

Then return to this repository before running Maven.

### Windows: Maven command not found

If `mvn` is not recognized in a new terminal, configure JDK 25 and Maven using
PowerShell, then reopen the terminal:

```powershell
[Environment]::SetEnvironmentVariable("JAVA_HOME", "$HOME\AppData\Local\jdks\jdk-25.0.2", "User")
[Environment]::SetEnvironmentVariable("MAVEN_HOME", "$HOME\.maven\maven-3.9.15", "User")
$userPath = [Environment]::GetEnvironmentVariable("Path", "User")
$required = "$HOME\AppData\Local\jdks\jdk-25.0.2\bin;$HOME\.maven\maven-3.9.15\bin"
if ($userPath -notlike "*$required*") {
    [Environment]::SetEnvironmentVariable("Path", "$required;$userPath", "User")
}
```

This workspace also configures the paths for VS Code integrated terminals.

The test suite expects the UI at `http://localhost:3000` and API at
`http://localhost:8080`. Verify the app is ready at
`http://localhost:8080/actuator/health`.

## Run the tests

From the repository root, run all API and browser tests:

```shell
mvn clean test
```

For the full verification lifecycle, including Allure HTML report generation:

```shell
mvn clean verify
```

Run only the API tests (does not need Chrome):

```shell
mvn clean test -Dtest.groups=api
```

Run only the Selenium browser journey:

```shell
mvn clean test -Dtest.groups=ui
```

The browser runs headlessly by default. Set `-Dbrowser.headless=false` to show
the browser.

Override the application URLs using Maven properties or environment variables:

| Purpose | Maven property | Environment variable | Default |
|---|---|---|---|
| API | `-Dapi.baseUrl=...` | `API_BASE_URL` | `http://localhost:8080` |
| UI | `-Dui.baseUrl=...` | `UI_BASE_URL` | `http://localhost:3000` |
| Headless Chrome | `-Dbrowser.headless=true/false` | `BROWSER_HEADLESS` | `true` |

For example:

```shell
mvn clean test -Dapi.baseUrl=http://localhost:8080 -Dui.baseUrl=http://localhost:3000
```

## Reports and logs

Surefire XML and text results are written under `target/surefire-reports/`.
Test execution logs are written to `target/test-logs/courier-shifts-tests.log` as
well as the console.
The Allure report includes every REST request and response as an attachment.
Each UI test includes a final browser screenshot attachment.

Run `mvn clean verify` after the application and Chrome are available. Allure
result JSON files are written under `allure-results/`. The static HTML report is
written to `target/site/allure-maven-plugin/index.html`; open that file directly
in a browser or serve it locally with:

```shell
mvn allure:serve
```

Surefire XML and text output under `target/surefire-reports/` can be reviewed
when a test fails. The persistent execution log is at
`target/test-logs/courier-shifts-tests.log`, and REST Assured request/response
details are included in the Maven output and reports where configured.

## Framework structure

```text
src/test/java/com/delivery/shifts/
├── api/
│   ├── pages/    # ApiServicePage service abstraction
│   ├── tests/    # REST API journey and business-rule tests
│   └── utils/    # API status, error, and response helpers
├── config/       # Environment configuration, waits, and request/response specs
├── data/         # Unique test-data builders
├── model/        # Typed request and response records
└── ui/
    ├── pages/    # BasePage, application shell, and feature page objects
    └── tests/    # Selenium browser journeys
```

Reusable utility classes:

- `ApiServicePage` provides one service-level entry point for courier, shift, booking, retrieval, and cancellation operations.
- `ApiAssertions` and `ApiResponseUtils` centralize HTTP status, error message, field, and typed response assertions.
- `TestConfig` centralizes system properties, environment variables, browser timeout, and report paths.
- `TimeUtils` creates consistent future, past, and truncated timestamps for test data.
- `BrowserWaits` provides common explicit-wait operations.
- `TestData` builds unique courier, email, and shift data consistently.

## Scenarios

### API scenarios

- Create a courier and upcoming shift, book the shift, verify the response
  status, response time, payload fields, and persisted booking records.
- Enforce shift capacity and verify that cancellation frees a slot for reuse.
- Reject duplicate bookings and overlapping bookings for the same courier.
- Reject bookings by inactive couriers and bookings for unknown shifts.
- Reject duplicate courier email addresses.
- Validate shift capacity, shift time ranges, and past shift creation.

### UI scenarios

- Create a courier and shift, book the shift, verify confirmed status, cancel
  the booking, and verify cancelled status.
- Filter shifts by zone and verify the resulting rows.
- Submit a duplicate courier email and verify the UI validation response.
- Create a courier and verify that it appears in the couriers table.
- Fill shift capacity and verify that another booking is not accepted.
- Reset a zone filter and verify that all shifts are restored.

The application has no authentication, so tests exercise the supported
unauthenticated API contract rather than inventing an authentication-failure
case.

## Assumptions and limitations

- The application is started separately using Docker Compose; tests do not
  start or stop app services.
- Tests use unique data and do not depend on seed records. The API does not
  offer deletion of booking history, so test data persists until the database
  is reset with `docker compose down -v`.
- Selenium uses explicit waits and stable element IDs/data attributes exposed
  by the supplied UI.
- The API booking response-time limit is 3 seconds, intended as a generous
  local-stack threshold.
- The combined suite may leave browser sessions longer-running than individual
  suite runs, so a Chromium renderer crash can occur in a repeated or resource-
  constrained environment.
- With more time, I would add CI execution, browser failure screenshots,
  video/retry diagnostics, stable test isolation, and a disposable database
  per test run.
