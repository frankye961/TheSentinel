# TheSentinel

TheSentinel is a Spring Boot security assessment engine project for exploring Java, Spring, HTTP security checks, concurrency, memory, and JVM behavior. The planned application will assess one permitted HTTP target and return structured findings. This repository currently contains **only a build and package skeleton**. It has no application class, endpoint, assessment logic, or tests and cannot run as an application.

## Toolchain and build

- Java: **25** required; OpenJDK **25.0.4.1** used for verification. Maven compiler release is 25. No preview features.
- Maven: **3.9.12** used to verify this skeleton; the selected Spring Boot release supports Maven 3.6.3 or later.
- Spring Boot: **4.1.1**, the latest stable release verified on 2026-10-11. Boot manages Spring Framework and other dependency versions.
- Dependencies: `spring-boot-starter-webmvc`, `spring-boot-starter-validation`, `spring-boot-starter-restclient`, plus Boot's general, MVC, and RestClient test starters.

Run `mvn -version` to confirm Maven uses JDK 25, then `mvn validate` to check the Maven project model and `mvn verify` to resolve dependencies and complete the lifecycle. The empty skeleton can build. Its plain JAR is not executable: Spring Boot repackaging is deferred until an application main class exists. A successful build with no tests verifies neither application startup nor behavior. No test sources exist yet.

## Directory map

`src/main/java/com/sentinel/` is the planned base package. Package directories contain only `.gitkeep` until implementation is requested.

| Path | Intended responsibility |
| --- | --- |
| `api` | REST input validation, endpoint and response mapping, error responses |
| `assessment` | Sequential orchestration and report assembly |
| `checks` | Pure evaluation of HTTP observations |
| `http` | Small transport contract and Spring RestClient adapter |
| `model` | Immutable observations, findings, and reports |
| `config` | Typed settings and infrastructure bean wiring |
| `src/main/resources` | Future shared application resources |
| `src/test/java`, `src/test/resources` | Future tests and local fixtures |

## Planned architecture

REST request → assessment service → HTTP transport → security checks → JSON report.

The eventual endpoint is `POST /assessments`. A request supplies one lab-permitted HTTP target. The response will contain a structured report with findings from X-Content-Type-Options and HSTS checks, plus clear completion, failure, and applicability outcomes. The request, report, and failure policy have not yet been defined.

- Use constructor injection. Spring manages services, checks, transports, and infrastructure beans.
- The assessment service depends on small `HttpTransport` and `SecurityCheck` contracts. A new check must not change the orchestration algorithm.
- Checks evaluate observations; only the HTTP adapter makes network requests, using Spring RestClient.
- Controllers validate requests and map responses. Models are immutable data objects.
- Services and checks keep assessment-specific state local. Checks run sequentially within each assessment. Spring may serve several incoming requests concurrently.
- Custom concurrency engines, cancellation, admission limits, persistence, and deep JVM investigation are future work.
