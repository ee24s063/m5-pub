# M5 -- LLM-assisted testing and documentation

A plain-Java (Java 17) exercise repository: a simplified Indian income-tax
and GST calculator (`TaxCalculator`) and an in-memory order store
(`OrderBook`). It also has JUnit 5 tests and Make targets for JaCoCo
coverage and PIT mutation testing.

## Description

Source files (`src/`):

| File | Contents |
|------|----------|
| `TaxCalculator.java` | Simplified Indian income-tax + GST calculator. Public methods: `computeIncomeTax(BigDecimal)`, `computeVAT(BigDecimal amount, int gstRatePercent)`, `applyExemption(BigDecimal, BigDecimal)`, `roundToPaise(BigDecimal)`, `isEligibleForReturn(BigDecimal, int)`. |
| `OrderBook.java` | In-memory order store backed by a private `ConcurrentHashMap<String, Order>`. Public methods: `list()`, `find(String id)`, `create(String customerId, BigDecimal amount)`, `cancel(String id)`, `totalFor(String customerId)`. |
| `Order.java` | `record Order(String id, String customerId, BigDecimal amount, OrderStatus status, Instant createdAt)` |
| `OrderStatus.java` | `enum OrderStatus { NEW, CANCELLED }` |

Tests (`test/`): `TaxCalculatorTest.java` (JUnit 5).

Course context: in Session 5A you raise `TaxCalculator` coverage above
the seed-test baseline (about 45% line / 30% branch). In Session 5B you
document `OrderBook`. Part D asks you to kill at least one surviving PIT
mutant (baseline: about 36%, 15 of 42 mutants killed).

## Build

### Requirements

- JDK 17 or newer (`javac --release 17`)
- `make`, `curl`, and a POSIX shell with `find` (on Windows, use Git Bash or WSL)
- Network access the first time you run it, to download the tool JARs into `libs/`

### Targets

| Command | What it does |
|---------|--------------|
| `make deps` | Downloads JUnit Platform Console Standalone 1.10.0 to `libs/junit.jar` |
| `make build` | Compiles `src/` and `test/` into `build/` |
| `make test` | Runs all tests with the JUnit console launcher |
| `make coverage` | Runs the tests under the JaCoCo 0.8.11 agent and writes `coverage/index.html` and `coverage/report.xml` |
| `make mutation` | Runs PIT 1.17.4 (JUnit 5 plugin 1.2.2) against `TaxCalculator` and writes `build/reports/pitest/index.html` |
| `make clean` | Removes `build/`, `libs/`, `coverage/` and `jacoco.exec` |

## Quick example

    make deps && make test
    make coverage       # JaCoCo HTML at coverage/index.html
    make mutation       # PIT HTML at build/reports/pitest/index.html

TODO: Java usage example for `OrderBook` / `TaxCalculator`. The snippets
I have don't show return types, or whether the `TaxCalculator` methods
are static.

## Contributing

TODO

## License

MIT License -- Copyright (c) <YEAR> <COPYRIGHT HOLDER>.
TODO: add a `LICENSE` file with the full MIT text.
