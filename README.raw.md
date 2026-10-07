# m5-pub

A small Java project containing a simplified Indian income-tax/GST calculator (`TaxCalculator`) and a thread-safe in-memory order book (`OrderBook`) with supporting `Order` and `OrderStatus` types.

## Build

Requires Java 17+ and `make`. Dependencies (JUnit 5, JaCoCo, PIT) are downloaded into `libs/` on first use.

```
make deps        # download JUnit
make build       # compile src/ and test/ into build/
make test        # run the JUnit 5 tests
make coverage    # JaCoCo report at coverage/index.html
make mutation    # PIT report at build/reports/pitest/index.html
make javadoc     # generate API documentation
make clean
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

TODO

## License

This project is licensed under the MIT License. See `LICENSE` for details.
