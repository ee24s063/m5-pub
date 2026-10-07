# PROMPTS.md - Session 5B (Auto-documentation)

## Part A - JavaDoc for `OrderBook.create`

Method chosen: `create(String customerId, BigDecimal amount)`

### Prompt 1

```
Generate JavaDoc for the following method. Rules:
- One-line summary in imperative mood.
- Describe the contract, not the implementation.
- @param for each parameter with type and constraints.
- @return with what is returned.
- @throws for each declared or unchecked exception the
  caller should be aware of.
- If any behaviour is not evident from the code, write
  "TODO" and skip.

public Order create(String customerId, BigDecimal amount) {
    if (customerId == null || customerId.isBlank()) {
        throw new IllegalArgumentException("customerId required");
    }
    if (amount == null || amount.signum() <= 0) {
        throw new IllegalArgumentException("amount must be > 0");
    }
    Order o = new Order(UUID.randomUUID().toString(),
                        customerId, amount,
                        OrderStatus.NEW, Instant.now());
    store.put(o.id(), o);
    return o;
}
```

### AI reply (first draft)

```java
/**
 * Create a new order for a customer and add it to the order book.
 *
 * <p>The returned order has a newly generated unique id, status
 * {@link OrderStatus#NEW}, and the current time as its creation time.
 *
 * @param customerId the {@code String} id of the customer placing the order;
 *                   must not be {@code null} or empty
 * @param amount     the {@code BigDecimal} amount of the order; must not be
 *                   {@code null} and must be strictly greater than zero
 * @return the newly created {@link Order}, which can be looked up
 *         afterwards with {@link #find(String)}
 * @throws IllegalArgumentException if {@code customerId} is {@code null} or
 *         empty, or if {@code amount} is {@code null}, zero, or negative
 */
```

### Prompt 2 (verification)

```
Given the JavaDoc above and the method body below,
identify any statement in the JavaDoc that is inconsistent
with the code. Do not fix; just list.
```

### AI reply

1. `@param customerId` and `@throws` say the id must not be "null or empty". The code uses `isBlank()`, so a whitespace-only id like `"   "` is also rejected. The JavaDoc understates the constraint.

Everything else (status NEW, generated id, current timestamp, amount must be > 0, the order being findable afterwards) matches the code.

### Fix

Changed "null or empty" to "null, empty, or whitespace only" in `@param` and "null or blank" in `@throws`.

### Verification run 2

Same prompt on the updated JavaDoc -> "No inconsistencies found."

Final JavaDoc is in `src/OrderBook.java` above `create`.

---

## Part B - README

### Prompt

```
Draft a README.md for this repository with sections:
description, build, quick example, contributing, license.
Use MIT license placeholder.
Rules:
- Do NOT invent features not present in the code.
- If a section has no evidence in the code, write "TODO" and
  skip.
- The one-line description must be a factual summary of what
  the code does, not marketing copy.
```

(pasted file tree, Makefile, the old README and the files under src/)

Raw output saved in `README.raw.md` (copied at the end of this file).

### Invented features deleted

1. `make javadoc` - there is no javadoc target in the Makefile (grep `javadoc` Makefile -> nothing).
2. "thread-safe in-memory order book" - `OrderBook` uses a `ConcurrentHashMap`, but nothing in the code or docs claims thread safety, and `cancel` does a get followed by a put, which is not atomic. Removed the word.

Other edits: rewrote the description in my own words, filled in the Contributing section by hand (was TODO), added `curl` to the build requirements since the Makefile uses it to download jars, and changed the license line because there is no `LICENSE` file in the repo.

---

## Part C - API reference

### Prompt

```
Read the following Java library and generate an API reference
in Markdown covering:
- Every public method of OrderBook, with signature, one-line
  summary, parameters (type + constraints), return value, and
  exceptions thrown.
- Referenced value types (Order, OrderStatus), with each field
  briefly explained.
- A "Quick example" block of 5-8 lines of real Java showing a
  typical create / find / cancel / totalFor flow.
If any method's behaviour is unclear from the code alone, add a
TODO note in the Markdown at that location.
```

(pasted OrderBook.java, Order.java, OrderStatus.java)

### Output (after my edits - see notes below)

# OrderBook API reference

`OrderBook` is an in-memory store of `Order` objects keyed by order id. It has no external dependencies.

## `OrderBook`

### `public OrderBook()`

Creates an empty order book.

### `public List<Order> list()`

Return all orders currently in the book.

- **Parameters:** none
- **Returns:** `List<Order>` - an unmodifiable snapshot of all orders (both `NEW` and `CANCELLED`). Later changes to the book are not reflected in the returned list. Calling `add`/`remove` on it throws `UnsupportedOperationException`.
- **Throws:** nothing
- TODO: order of the elements is not specified (it comes from a hash map).

### `public Optional<Order> find(String id)`

Look up an order by its id.

- **Parameters:** `id` (`String`) - the order id; must not be `null`.
- **Returns:** `Optional<Order>` - the order if present, otherwise `Optional.empty()`. A cancelled order is still returned (with status `CANCELLED`).
- **Throws:** `NullPointerException` if `id` is `null`.

### `public Order create(String customerId, BigDecimal amount)`

Create a new order for a customer and add it to the book.

- **Parameters:**
  - `customerId` (`String`) - must not be `null`, empty or whitespace only.
  - `amount` (`BigDecimal`) - must not be `null` and must be > 0.
- **Returns:** `Order` - the new order with a random UUID id, status `NEW` and `createdAt` set to the current time.
- **Throws:**
  - `IllegalArgumentException("customerId required")` if `customerId` is `null` or blank.
  - `IllegalArgumentException("amount must be > 0")` if `amount` is `null`, zero or negative.

### `public boolean cancel(String id)`

Cancel the order with the given id.

- **Parameters:** `id` (`String`) - the order id; must not be `null`.
- **Returns:** `boolean` - `true` if the order existed and was `NEW` and is now `CANCELLED`; `false` if no order has this id or it was already cancelled. The order is kept in the book, only its status changes.
- **Throws:** `NullPointerException` if `id` is `null`.

### `public BigDecimal totalFor(String customerId)`

Sum the amounts of a customer's active (`NEW`) orders.

- **Parameters:** `customerId` (`String`) - must not be `null`.
- **Returns:** `BigDecimal` - total of `amount` over the customer's `NEW` orders. Cancelled orders are ignored. Returns `BigDecimal.ZERO` if the customer has no `NEW` orders (or is unknown).
- **Throws:** `NullPointerException` if `customerId` is `null`.

## Value types

### `Order` (record)

```java
public record Order(String id, String customerId, BigDecimal amount,
                    OrderStatus status, Instant createdAt) {}
```

| Field | Type | Meaning |
|---|---|---|
| `id` | `String` | Unique order id (random UUID string, set by `create`) |
| `customerId` | `String` | Id of the customer who placed the order |
| `amount` | `BigDecimal` | Order amount, always > 0 for orders made by `create` |
| `status` | `OrderStatus` | `NEW` or `CANCELLED` |
| `createdAt` | `Instant` | Time the order was created; unchanged by `cancel` |

Being a record, `Order` is immutable; `cancel` replaces the stored order with a new `Order` that has status `CANCELLED`.

### `OrderStatus` (enum)

| Constant | Meaning |
|---|---|
| `NEW` | Order is active and counts towards `totalFor` |
| `CANCELLED` | Order was cancelled with `cancel`; excluded from `totalFor` |

## Quick example

```java
OrderBook book = new OrderBook();
Order a = book.create("cust-42", new BigDecimal("199.99"));
Order b = book.create("cust-42", new BigDecimal("50.00"));
System.out.println(book.find(a.id()).isPresent());     // true
System.out.println(book.totalFor("cust-42"));          // 249.99
book.cancel(b.id());
System.out.println(book.totalFor("cust-42"));          // 199.99
```

### Verification notes

- All 5 public methods are there with the right names and signatures. Added the implicit no-arg constructor myself.
- `Order` (5 fields) and `OrderStatus` (2 constants) are documented.
- Quick example: put it in a scratch `main` (with `import java.math.BigDecimal;`), compiled with `javac --release 17` and ran it. Output was `true`, `249.99`, `199.99` as expected.
- `IllegalArgumentException` - grep shows only two throws, both in `create`. Both are listed.
- Missing exception docs I added by hand: the AI said `find(null)` and `cancel(null)` "return empty / false", but the store is a `ConcurrentHashMap`, which does not allow null keys, so both actually throw `NullPointerException` (checked by running it). It also left out the `NullPointerException` for `totalFor(null)` (`customerId.equals(...)`).
- Also added that `list()` returns an unmodifiable copy (the AI just said "returns a list of orders").

---

## Part D - Reflect

- **JavaDoc inconsistencies caught on the first self-check pass:** 1 (whitespace-only `customerId` is rejected by `isBlank()` but the JavaDoc said "null or empty"). Second pass found none.
- **Invented features in the README pass:** 2 (`make javadoc` target and the "thread-safe" claim).
- **Which artefact needed the most hand editing:** the API reference. It is the longest and makes the most specific claims, and the mistakes were in behaviour that isn't visible in `OrderBook` itself - the null handling depends on `ConcurrentHashMap` rejecting null keys, so the AI guessed "returns empty/false" instead of NPE. I only found it by actually running the calls. The JavaDoc was short and easy to check line by line, and the README mostly just needed two things deleted.

---

## README.raw.md

````markdown
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
````

## README.md

````markdown
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
````
