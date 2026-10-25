# Rest Assured + Selenium + TestNG Automation Framework

![Tests](https://github.com/spandanpandey1809/restassured-selenium-testng-framework/actions/workflows/tests.yml/badge.svg)

A Java test automation framework for **REST API** and **web UI** testing, with Allure reporting and CI on GitHub Actions. Tests run on every push and nightly.

| Layer | Tools |
| --- | --- |
| API testing | Rest Assured, JSON Schema validation, POJOs with Jackson, service layer |
| Service virtualization | WireMock stubs for slow, failing and contract-breaking dependencies |
| UI testing | Selenium 4, Page Object Model, explicit waits, thread-safe WebDriver |
| Test runner | TestNG: groups, data providers, dependent tests, parallel execution |
| Reporting | Allure: steps, request/response logs, screenshots on failure, published to GitHub Pages |
| CI/CD | GitHub Actions: push, pull request, nightly schedule, manual suite choice |
| Containers | Docker test-runner image, docker-compose with Selenium Grid (RemoteWebDriver) |
| Build | Maven, Java 17 |

## Applications under test

- **API:** [Restful Booker](https://restful-booker.herokuapp.com/apidoc/index.html), a public practice booking API (auth, CRUD, filtering).
- **UI:** [Sauce Demo](https://www.saucedemo.com), a public practice e-commerce site (login, products, cart, checkout).

## Project structure

```
src/main/java/com/spandan/framework
├── config/ConfigManager.java      # env-based config, overridable with -Dkey=value
├── api/ApiClient.java             # shared request spec: base URL, headers, logging, Allure filter
├── api/BookingService.java        # service layer: one method per endpoint
├── api/models/                    # Booking POJOs with a fluent builder
├── ui/DriverFactory.java          # ThreadLocal WebDriver, local or Selenium Grid (Chrome / Firefox / Edge)
├── ui/BasePage.java               # common actions with explicit waits
├── ui/pages/                      # Page Objects: Login, Inventory, Cart, Checkout
└── utils/TestDataReader.java      # JSON test data -> POJOs

src/test/java/com/spandan/tests
├── api/                           # AuthTest, BookingCrudTest, BookingNegativeTest, ApiResilienceTest
└── ui/                            # BaseUiTest, LoginTest, CartAndCheckoutTest

src/test/resources
├── config/qa.properties           # URLs, public demo users, browser settings
├── suites/                        # testng.xml (full), smoke, api and ui suites
├── testdata/booking.json
└── schemas/booking-schema.json
```

## Test coverage

| Area | Scenarios |
| --- | --- |
| Auth API | Valid token; invalid credentials (data-driven) |
| Booking API | Create, get with schema validation, filter by name, PUT, PATCH, DELETE, list |
| API negative | No token / invalid token (403), missing booking (404), invalid payload |
| API resilience (WireMock) | Timeout on a slow service, 503 outage with Retry-After, schema catches a contract break, client sends the headers the API accepts |
| Login UI | Valid login; locked user, wrong password, empty fields (data-driven) |
| Cart and checkout UI | Add to cart, sort by price, full checkout, form validation |

## How to run

Requires Java 17+ and Maven. Chrome is the default browser; Selenium Manager downloads the driver automatically.

```bash
mvn test                                      # full regression (API + UI, parallel)
mvn test -Dsuite=smoke-suite.xml              # critical tests only (TestNG "smoke" group)
mvn test -Dsuite=api-suite.xml                # API only
mvn test -Dsuite=ui-suite.xml                 # UI only
mvn test -Dbrowser=firefox -Dheadless=false   # watch UI tests in Firefox

mvn allure:serve                              # open the Allure report in a browser
```

In CI, the Allure report is published to GitHub Pages after every main-branch run: **[live test report](https://spandanpandey1809.github.io/restassured-selenium-testng-framework/)**. It is also kept as a build artifact, and failures are summarised as annotations on the run.

### Run in Docker

No local Java or Maven needed. Dependencies are cached in an image layer that only rebuilds when `pom.xml` changes.

```bash
docker build -t rst-tests .
docker run --rm rst-tests                                  # API suite (default)
docker run --rm -e SUITE=smoke-suite.xml rst-tests         # pick a suite
docker run --rm -v "$PWD/target:/tests/target" rst-tests   # keep Allure results on the host
```

### Run everything in containers with Selenium Grid

`docker-compose.yml` starts a Selenium Grid Chrome node and the test runner. UI tests switch to `RemoteWebDriver` when `grid.url` is set, using the same browser options as a local run.

```bash
docker compose up --build --exit-code-from tests   # full suite: API + UI on the Grid
docker compose down
```

While it runs, the Grid console is at http://localhost:4444 and you can watch the browser at http://localhost:7900 (password `secret`). Locally without Docker, point at any Grid with `mvn test -Dgrid.url=http://localhost:4444`.

CI runs the full suite this way as a separate job on every push.

## Design decisions

- **Service layer for APIs:** tests call `BookingService` methods, never raw requests, so an endpoint change is fixed in one place.
- **One shared request spec:** headers, timeouts, logging and Allure capture live in `ApiClient`.
- **Timeouts on every API call:** a slow dependency fails fast with a clear error instead of hanging CI.
- **WireMock for what the real service can't do on demand:** `BookingService` accepts any request spec, so the same service layer runs against a stub to test slowness, outages and contract breaks.
- **Dependent CRUD tests:** if creating a booking fails, the later steps are skipped instead of failing with misleading errors.
- **Fresh browser per test, one per thread:** tests stay independent and can run in parallel.
- **Screenshots in `@AfterMethod`:** captured before the browser quits, and never allowed to hide the real failure.
- **Chrome password manager disabled:** the demo site's public passwords trigger Chrome's breach-warning pop-up, which blocks clicks.

## Defects found in the API under test

- `POST /booking` returns **418 I'm a Teapot** when the Accept header lists several media types (Rest Assured's `ContentType.JSON` does this), and **415** for any charset other than UTF-8. The framework sends explicit `application/json` headers.
- Invalid login returns **200 OK** with `"reason": "Bad credentials"` instead of **401**.
- A successful DELETE returns **201 Created** instead of **200** or **204**.
- Creating a booking with missing required fields returns **500** instead of **400**.

## Roadmap

- [x] Run the suite in a Docker container, locally and in CI
- [x] Run UI tests on Selenium Grid via docker-compose
- [x] Stub dependency failures with WireMock (timeouts, outages, contract breaks)
- [x] Publish the Allure report to GitHub Pages
- [ ] Mobile tests with Appium on the Sauce Labs demo Android app
