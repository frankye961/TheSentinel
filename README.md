# TheSentinel

TheSentinel is a learning project for a Spring Boot concurrent security assessment engine. The first milestone will assess one permitted HTTP target and return structured security findings. Later milestones will explore concurrency, cancellation, resource limits, memory, and JVM behavior. This repository currently contains **only a build and package skeleton**. It has no application class, endpoint, assessment logic, or tests and cannot run as an application.

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

## Milestone 1 design target

REST request → assessment service → HTTP transport → security checks → JSON report.

The eventual endpoint is `POST /assessments`. A request supplies one lab-permitted HTTP target. The response will contain a structured report with findings from X-Content-Type-Options and HSTS checks, plus clear completion, failure, and applicability outcomes. The exact request, report, and failure policy are to be specified in M1-02.

- Use constructor injection. Spring manages services, checks, transports, and infrastructure beans.
- The assessment service depends on small `HttpTransport` and `SecurityCheck` contracts. A new check must not change the orchestration algorithm.
- Checks evaluate observations; only the HTTP adapter makes network requests, using Spring RestClient.
- Controllers validate requests and map responses. Models are immutable data objects.
- Services and checks keep assessment-specific state local. Checks run sequentially within each assessment. Spring may serve several incoming requests concurrently.
- Custom concurrency engines, cancellation, admission limits, persistence, and deep JVM investigation belong to later milestones.

## Milestone 1 Kanban

States: **Backlog → Ready → In progress → Review → Done**. At most one implementation card may be **In progress**, and at most one card may be in **Review**. Reading and implementation belong to the same card. Status reflects repository evidence, not intent. No card is Done; no implementation or test source exists. M1-01 is Ready because its build scaffold exists, while its startup and smoke test are unstarted. M1-02 has partial documentation in this README but its acceptance criteria are incomplete.

| ID | Task | Status | Dependencies | Acceptance criteria | Learning topics | Verification evidence and review notes |
| --- | --- | --- | --- | --- | --- | --- |
| M1-01 | Bootstrap Spring Boot application | Ready | Skeleton | Application starts and context smoke test passes | Boot versus Framework; starters; auto-configuration; application context; Maven lifecycle | Scaffold present. `mvn validate` passed; `mvn -B -ntp verify` passed after package rename on JDK 25. No main class or test; startup unverified. |
| M1-02 | Define assessment scope and package boundaries | Backlog | M1-01 | Inputs, outputs, two checks, failure policy, and package responsibilities documented | Cohesion; coupling; component scanning; contracts; separation of responsibilities | Flow and package intentions documented here; exact input/output and failure policy remain open. Partially complete. |
| M1-03 | Add typed configuration | Backlog | M1-02 | Timeouts and permitted lab targets bind correctly; invalid configuration fails at startup | Configuration properties; registration; validation; `Duration`; defaults; property precedence | Unstarted; no configuration or tests. |
| M1-04 | Validate assessment input | Backlog | M1-02, M1-03 | Invalid and out-of-scope targets rejected before fetching | Request DTOs; records; `URI`; Bean Validation; syntax versus domain validation | Unstarted. |
| M1-05 | Model HTTP observations | Backlog | M1-02 | Immutable observations represent target, status, and headers | Records; generics; multivalued headers; case-insensitive lookup; defensive copying | Unstarted. |
| M1-06 | Model findings and reports | Backlog | M1-02 | Findings contain evidence and recommendations; reports distinguish completion, failure, and applicability | Value objects; enums; collections; equality; serialization; outcomes | Unstarted. |
| M1-07 | Define component contracts | Backlog | M1-05, M1-06 | Transport and check contracts use project models | Interfaces; polymorphism; dependency inversion; composition; exception contracts | Unstarted. |
| M1-08 | Wire components through Spring | Backlog | M1-01, M1-07 | Spring assembles components and tests can replace transport | Service/component annotations; configuration beans; constructor and collection injection; bean scopes | Unstarted. |
| M1-09 | Implement sequential assessment | Backlog | M1-07, M1-08 | Fetch once, evaluate registered checks, assemble report | Orchestration; stateless services; iteration; aggregation; ordering; test doubles | Unstarted. |
| M1-10 | Check X-Content-Type-Options | Backlog | M1-05, M1-07 | Missing, expected, and unexpected values covered | MIME sniffing; header semantics; parsing; parameterized tests | Unstarted. |
| M1-11 | Check HSTS | Backlog | M1-05, M1-07 | Relevant HTTPS cases covered; HTTP is not applicable | HTTPS/TLS basics; HSTS directives; max-age parsing; applicability | Unstarted. |
| M1-12 | Implement RestClient adapter | Backlog | M1-03, M1-05, M1-07 | Configured fetching produces observations without retaining response bodies | Builders; request factories; exchange; status handling; timeouts; redirects; resource cleanup | Dependency declared; adapter unstarted. |
| M1-13 | Handle failures | Backlog | M1-04, M1-06, M1-12 | Invalid input, timeouts, and transport failures remain distinguishable | Exception translation; cause preservation; controller advice; `ProblemDetail`; API status codes | Unstarted. |
| M1-14 | Expose `POST /assessments` | Backlog | M1-04, M1-06, M1-09, M1-13 | Validated request invokes assessment service and returns report | REST controllers; request bodies; validation; DTO mapping; JSON | Unstarted. |
| M1-15 | Verify Spring integration | Backlog | M1-08 through M1-14 | Validation, error mapping, wiring, adapter behavior, and a complete local assessment pass | MVC/client test slices; MockMvc; mock HTTP servers; full-context tests; local fixtures | Test dependencies declared; no tests. |
| M1-16 | Review resources and finish documentation | Backlog | M1-12, M1-15 | Ownership and cleanup documented; README provides verified local example | Bean lifecycle; client ownership; connection reuse; shutdown; reachability; heap retention; basic GC | Unstarted. |

### Review workflow

Invoke the repository skill by asking Codex to **use `$milestone-review` to review M1-01** (or the current card). A review-only request stays read-only. Explicitly authorize tracker edits or commit/push when wanted; the skill records actual verification and stages only task-related changes.

## Focused official reading

- [Spring Boot 4.1.1 system requirements](https://docs.spring.io/spring-boot/system-requirements.html) and [build systems and starters](https://docs.spring.io/spring-boot/reference/using/build-systems.html)
- [Spring Boot auto-configuration](https://docs.spring.io/spring-boot/reference/using/auto-configuration.html) and [externalized configuration](https://docs.spring.io/spring-boot/reference/features/external-config.html)
- [Spring Boot testing](https://docs.spring.io/spring-boot/reference/testing/index.html) and [testing Spring Boot applications](https://docs.spring.io/spring-boot/reference/testing/spring-boot-applications.html)
- [Spring MVC annotated controllers](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller.html) and [Spring Framework REST clients](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html)
- [Java 25 concurrency APIs](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/package-summary.html) and [on-heap and off-heap memory](https://docs.oracle.com/en/java/javase/25/core/heap-and-heap-memory.html) for later milestones

**Next unfinished task:** M1-01 — create the application entry point and context smoke test when explicitly requested, then verify startup. Read Boot versus Framework, starters, auto-configuration, application context, and Maven lifecycle while working on that card.
