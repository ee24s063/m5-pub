# M5 -- LLM-assisted testing and documentation

Starter for the tax-calculator + order-API exercises.

Run:

    make deps && make test
    make coverage       # JaCoCo HTML at coverage/index.html
    make mutation       # PIT HTML at build/reports/pitest/index.html

Session 5A: use an LLM to raise `TaxCalculator` line/branch coverage
above the ~45%/~30% baseline the three seed tests give you.
Session 5B: add JavaDoc, a README, and a Markdown API reference for
the plain-Java `OrderBook` library class (no Spring, no DTOs -- just
`OrderBook`, `Order` as a record, `OrderStatus` as an enum).

The `mutation` target uses PIT 1.17.4 with the JUnit 5 plugin; both
are auto-downloaded into `libs/` on first use.  Baseline mutation
score with the seed test is around 36% (15 of 42 mutants killed) --
Part D of the handout asks students to kill at least one surviving
mutant.
