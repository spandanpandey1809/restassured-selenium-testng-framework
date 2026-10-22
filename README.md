# Rest Assured + Selenium + TestNG Automation Framework

![Tests](https://github.com/spandanpandey1809/restassured-selenium-testng-framework/actions/workflows/tests.yml/badge.svg)

A Java test automation framework for **REST API** and **web UI** testing, with Allure reporting and CI on GitHub Actions. Tests run on every push and nightly.

| Layer | Tools |
| --- | --- |
| API testing | Rest Assured, JSON Schema validation, POJOs with Jackson, service layer |
| UI testing | Selenium 4, Page Object Model, explicit waits, thread-safe WebDriver |
| Test runner | TestNG: groups, data providers, dependent tests, parallel execution |
| Reporting | Allure: steps, request/response logs, screenshots on failure |
| CI/CD | GitHub Actions: push, pull request, nightly schedule, manual suite choice |
| Containers | Docker test-runner image with cached dependencies |
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
├── ui/DriverFactory.java          # ThreadLocal WebDriver (Chrome / Firefox / Edge)
├── ui/BasePage.java               # common actions with explicit waits
├── ui/pages/                      # Page Objects: Login, Inventory, Cart, Checkout
└── utils/TestDataReader.java      # JSON test data -> POJOs

src/test/java/com/spandan/tests
├── api/                           # AuthTest, BookingCrudTest, BookingNegativeTest
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

In CI, the Allure report is uploaded as a build artifact, and failures are summarised as annotations on the run.

### Run in Docker

No local Java or Maven needed. Dependencies are cached in an image layer that only rebuilds when `pom.xml` changes.

```bash
docker build -t rst-tests .
docker run --rm rst-tests                                  # API suite (default)
docker run --rm -e SUITE=smoke-suite.xml rst-tests         # pick a suite
docker run --rm -v "$PWD/target:/tests/target" rst-tests   # keep Allure results on the host
```

CI runs the API suite in this container as a separate job on every push.

## Design decisions

- **Service layer for APIs:** tests call `BookingService` methods, never raw requests, so an endpoint change is fixed in one place.
- **One shared request spec:** headers, logging and Allure capture live in `ApiClient`.
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
- [ ] Run UI tests on Selenium Grid via docker-compose
- [ ] Run the booking API in a container and stub downstream failures with WireMock
- [ ] Publish the Allure report to GitHub Pages
- [ ] Mobile tests with Appium on the Sauce Labs demo Android app
