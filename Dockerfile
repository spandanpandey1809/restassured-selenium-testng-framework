# Test runner image: Maven + Java 17 with project dependencies pre-downloaded.
#   docker build -t rst-tests .
#   docker run --rm rst-tests                              # API suite (default)
#   docker run --rm -e SUITE=smoke-suite.xml rst-tests     # any suite in src/test/resources/suites
#   docker run --rm -v "$PWD/target:/tests/target" rst-tests   # keep Allure results on the host
FROM maven:3.9.9-eclipse-temurin-17

WORKDIR /tests

# Copy only the pom first so the dependency layer is cached until pom.xml changes.
COPY pom.xml .
# Warm the Maven cache; anything this misses (e.g. test-provider plugins) downloads at test time.
RUN mvn -B -q dependency:go-offline || true

COPY src ./src

# UI suites need a browser; inside a container they run against Selenium Grid (see docker-compose).
ENV SUITE=api-suite.xml

ENTRYPOINT ["sh", "-c", "mvn -B test -Dsuite=${SUITE} -Dheadless=true"]
