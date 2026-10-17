# Sentinel

Sentinel is an incremental **concurrent web pentesting framework** for authorized targets and controlled laboratories. It is also a Java 25, Spring Boot, HTTP, concurrency, and JVM learning project. The current code is a passive HTTP assessment foundation: it can fetch one configured in-scope target through `HttpTransport`, evaluate X-Content-Type-Options and HSTS, and produce an `AssessmentReport` when called from Java. There is **no HTTP assessment endpoint yet**, so Milestone 1 is not complete. Crawling, active probes, identity-aware testing, evidence storage, AI enrichment, and a custom concurrent engine are planned work.

## Current toolchain and verification

- Java **25** (`maven.compiler.release=25`; no preview features), Maven **3.9.12** observed, Spring Boot **4.1.1** with Boot-managed dependency versions. Base package: `com.sentinel`.
- `mvn -version` verifies Maven's JDK. `mvn validate` checks the Maven model. `mvn verify` compiles and runs the current suite; the latest review passed **43 tests** on OpenJDK 25.0.4.1. Those tests do not establish a working HTTP API or real-network timeout/DNS safety.
- `application.yaml` sets Boot-managed client connect/read timeouts to **2s/5s**, disables automatic redirects, and lists local permitted origins and two header checks. The service checks origin before fetching. See [the M1 review](docs/milestone-1-review.md) for the remaining outbound safety work.

## Current request path

Programmatic `AssessmentService.execute(URI)` → scope check and `ValidTarget` → `HttpTransport` → Spring `RestClient` → `HttpResponseSnapshot` → sequential passive checks → `AssessmentReport`.

The code is one Maven module. `service` contains orchestration, transport adapter, and current combined check implementation; `http` and `checks` contain small contracts; `config` binds lab settings; `model` holds snapshots and results. The current `HttpResponseSnapshot` serves as the HTTP observation type. Its headers are copied defensively. There is no production `api` controller yet.

Use constructor injection and keep each assessment's mutable work local. The transport owns HTTP communication; passive checks evaluate existing observations and never fetch. The service coordinates the fetch and checks. Adding independent checks without changing the combined check class is an M2 task. The future execution engine must remain independent of testing technique, and every outbound request must respect scope and budgets. Active tests will be opt-in; AI will be optional.

## Project plan

- [Architecture and boundaries](docs/architecture.md)
- [Revised milestone roadmap](docs/milestones.md)
- [Detailed Kanban cards](docs/kanban.md)
- [Java/JVM/security learning path](docs/learning-path.md)
- [Verified Milestone 1 review](docs/milestone-1-review.md)

Work one unfinished card at a time. The immediate card is **M01-01: verify direct application launch and settle the M1 request/report contract**. The original [Milestone 1 tracker](MILESTONES.md) remains as a historical plan with a current-status pointer. Ask Codex to use `$milestone-review` for a read-only review; authorize tracker edits or Git delivery explicitly when wanted.
