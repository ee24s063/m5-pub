# m5-pub

Plain Java 17 starter with a simplified Indian income-tax/GST calculator (`TaxCalculator`) and a small in-memory order book (`OrderBook`, `Order`, `OrderStatus`), plus JUnit 5 tests and Makefile targets for coverage and mutation testing.

## Build

Needs a JDK (17 or newer), `make` and `curl`. The JUnit, JaCoCo and PIT jars are downloaded into `libs/` the first time they are needed.

```
make deps        # download JUnit
make build       # compile src/ and test/ into build/
make test        # run the JUnit 5 tests
make coverage    # JaCoCo report at coverage/index.html
make mutation    # PIT report at build/reports/pitest/index.html
make clean       # remove build/, libs/, coverage/ and jacoco.exec
```

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

MIT License. Copyright (c) <year> <copyright holder>.
