# m5-pub

Plain Java 17 code for a simplified Indian income-tax/GST calculator (`TaxCalculator`) and an in-memory order book (`OrderBook`), with JUnit 5 tests and Make targets for coverage and mutation testing.

## Description

Source files (`src/`):

| File | Contents |
|------|----------|
| `TaxCalculator.java` | Simplified Indian income-tax + GST calculator. Public methods: `computeIncomeTax(BigDecimal)`, `computeVAT(BigDecimal amount, int gstRatePercent)`, `applyExemption(BigDecimal, BigDecimal)`, `roundToPaise(BigDecimal)`, `isEligibleForReturn(BigDecimal, int)`. |
| `OrderBook.java` | In-memory order store. Public methods: `list()`, `find(String id)`, `create(String customerId, BigDecimal amount)`, `cancel(String id)`, `totalFor(String customerId)`. |
| `Order.java` | `record Order(String id, String customerId, BigDecimal amount, OrderStatus status, Instant createdAt)` |
| `OrderStatus.java` | `enum OrderStatus { NEW, CANCELLED }` |

Tests (`test/`): `TaxCalculatorTest.java` (JUnit 5).

## Build

### Requirements

- JDK 17 or newer (`javac --release 17`)
- `make`, `curl`, and a shell with `find` (on Windows, use Git Bash or WSL)
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

```java
OrderBook book = new OrderBook();
Order order = book.create("cust-1", new BigDecimal("100.00"));
book.find(order.id());          // Optional[Order[...]]
book.totalFor("cust-1");        // 100.00
book.cancel(order.id());        // true

TaxCalculator calc = new TaxCalculator();
calc.computeIncomeTax(new BigDecimal("700000"));   // 52500.00
calc.computeVAT(new BigDecimal("1000"), 18);       // 180.00
```

## Contributing

1. Put source files in `src/` and tests in `test/` (test classes are named `*Test`).
2. Run `make test` before committing; for changes to `TaxCalculator` also check `make coverage` and `make mutation`.
3. Open a pull request with a short description of the change.

## License

MIT License. Copyright (c) <YEAR> <COPYRIGHT HOLDER>.
