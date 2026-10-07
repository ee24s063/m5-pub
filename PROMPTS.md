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

### AI reply

```java
/**
 * Create a new order for the given customer and amount, and store it.
 * <p>
 * The new order gets a randomly generated unique identifier (a UUID string),
 * the status {@link OrderStatus#NEW}, and the current time as its creation
 * timestamp. It is stored under its identifier before it is returned.
 * <p>
 * Thread-safety: TODO
 * <p>
 * Persistence/durability of the stored order: TODO
 *
 * @param customerId the {@code String} ID of the customer placing the order;
 *                   must not be {@code null}, empty, or whitespace only.
 *                   Whether the customer must already exist: TODO
 * @param amount     the {@code BigDecimal} order amount; must not be
 *                   {@code null} and must be greater than zero.
 *                   Limits on scale, precision or currency: TODO
 * @return the newly created {@link Order}, never {@code null}, with a new ID,
 *         the given customer ID and amount, status {@code NEW}, and the
 *         creation time
 * @throws IllegalArgumentException if {@code customerId} is {@code null},
 *         empty or whitespace only, or if {@code amount} is {@code null},
 *         zero or negative
 * @throws RuntimeException any exception thrown by the underlying store
 *         when saving the order: TODO
 */
```

Notes it added:
- `isBlank()` also rejects whitespace-only strings, so the contract says that explicitly.
- The TODOs are there because the snippet doesn't show what `store` is, so thread-safety, durability, store exceptions and whether the customer must exist can't be documented from it.
- No TODO for duplicate IDs, since a random UUID collision is very unlikely.

### Prompt 2 (verification)

```
Given the JavaDoc above and the method body below,
identify any statement in the JavaDoc that is inconsistent
with the code. Do not fix; just list.
```

(pasted the JavaDoc, the method body and the `store` field declaration)

### AI reply (first pass)

No statement directly contradicts the code. ID, status, timestamp, store-before-return, both argument checks and the `@return` all match.

Statements it flagged as loose or misleading:

1. "unique identifier" - nothing checks the ID against the store; uniqueness only comes from UUIDs rarely colliding.
2. "whitespace only" - `isBlank()` uses `Character.isWhitespace`, so a string of only non-breaking spaces (U+00A0) is not blank and is accepted.
3. `@throws RuntimeException ... thrown by the underlying store` - the store is an in-memory `ConcurrentHashMap`; `put` only throws NPE for null key/value, which can't happen here. The clause describes a failure the code doesn't have.
4. The TODOs don't contradict the code, but the code answers most of them: it's in memory only (no persistence), the customer is not checked, no scale/precision limits.

### Fixes

- "unique identifier" -> "randomly generated UUID string as its identifier".
- "empty, or whitespace only" -> "blank (empty or only whitespace, as defined by `String#isBlank()`)".
- Removed `@throws RuntimeException`.
- Replaced the TODOs with what the code shows: customer not checked, any scale accepted. Dropped the thread-safety and persistence lines (those are about the class, not this method's contract).
- Added that the order can be looked up with `find(String)` after it is created.

### Verification run 2

Same prompt on the fixed JavaDoc. Reply: no statements in the JavaDoc conflict with the code (it went through every claim in a table). Its one caveat was that the `Order` constructor wasn't shown, so it couldn't confirm the argument order. I checked `Order.java`: the record is `(id, customerId, amount, status, createdAt)`, which matches.

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

(pasted the file tree, `.gitignore`, the Makefile (truncated), the old README, `Order.java`, `OrderStatus.java` and truncated `OrderBook.java` / `TaxCalculator.java`)

Raw output saved in `README.raw.md` (copied at the end of this file).

### Invented features deleted

None. I grepped for each thing it claimed: every make target (`deps`, `build`, `test`, `coverage`, `mutation`, `clean`), the versions (JUnit console 1.10.0, JaCoCo 0.8.11, PIT 1.17.4, PIT JUnit 5 plugin 1.2.2), `coverage/report.xml`, and all the method names in the table. All of them are in the Makefile or source.

### Other edits

- Wrote the one-line description in my own words.
- Removed the "Course context" paragraph. It was copied from the old README, it's about the assignment and not the code, and the coverage/mutation numbers are only the starter baseline.
- Removed "backed by a private `ConcurrentHashMap`" from the table (implementation detail).
- Quick example was TODO (the AI said it couldn't see return types). Wrote a Java example by hand, then compiled and ran it: `true`, `100.00`, `true`, `52500.00`, `180.00`.
- Contributing was TODO. Wrote it by hand from how the repo is laid out.
- License: kept the MIT placeholder, dropped the TODO line.

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

(pasted full `OrderBook.java`, `Order.java`, `OrderStatus.java`)

### AI output

#### OrderBook API Reference

`OrderBook` is an in-memory store of `Order` records, kept in a `ConcurrentHashMap` keyed by order ID. Orders never change in place. Cancelling an order replaces it with a new `Order` whose status is `CANCELLED`.

##### Class `OrderBook`

###### Constructor

```java
public OrderBook()
```
Creates an empty order book. This is the implicit default constructor.

###### `list`

```java
public List<Order> list()
```
Returns a snapshot of every order in the book, including cancelled ones.

- **Parameters:** none
- **Returns:** `List<Order>`. This is an unmodifiable copy made with `List.copyOf`. Later changes to the book do not show up in it. It never returns `null`, and returns an empty list when there are no orders.
- **Throws:** nothing
- **Order of results:** no fixed order. It follows `ConcurrentHashMap` iteration order, not creation time.

> **TODO:** Say whether callers can rely on any ordering, such as by `createdAt`. Right now none is guaranteed. Also say whether cancelled orders are meant to be included.

###### `find`

```java
public Optional<Order> find(String id)
```
Looks up one order by its ID.

- **Parameters:**
  - `id` (`String`): the order ID. Must not be `null`.
- **Returns:** `Optional<Order>`. It contains the current version of the order, which may be `CANCELLED`, or is `Optional.empty()` if no order has that ID.
- **Throws:**
  - `NullPointerException` if `id` is `null`, because `ConcurrentHashMap` does not allow null keys.

> **TODO:** Decide whether a `null` `id` should return `Optional.empty()` instead of throwing.

###### `create`

```java
public Order create(String customerId, BigDecimal amount)
```
Creates a new order with status `NEW`, stores it, and returns it.

- **Parameters:**
  - `customerId` (`String`): required. Must not be `null`, empty, or whitespace only. It is stored exactly as given, without trimming.
  - `amount` (`BigDecimal`): required. Must not be `null` and must be greater than zero. It is stored exactly as given, with no change to its scale or rounding.
- **Returns:** `Order`, the newly stored order. It gets a random UUID string as its `id`, status `OrderStatus.NEW`, and `createdAt = Instant.now()`.
- **Throws:**
  - `IllegalArgumentException("customerId required")` if `customerId` is `null` or blank.
  - `IllegalArgumentException("amount must be > 0")` if `amount` is `null`, zero, or negative.

> **TODO:** Say whether `amount` should have a fixed scale or currency precision, such as 2 decimal places. Any scale is accepted today.

###### `cancel`

```java
public boolean cancel(String id)
```
Marks an existing order as `CANCELLED`.

- **Parameters:**
  - `id` (`String`): the order ID. Must not be `null`.
- **Returns:** `boolean`
  - `true` if the order existed and was `NEW`, and is now `CANCELLED`.
  - `false` if no order has that ID, or if it was already `CANCELLED`.
- **Throws:**
  - `NullPointerException` if `id` is `null`, from `ConcurrentHashMap.get`.
- **Side effects:** replaces the stored `Order` with a copy that has `status = CANCELLED`. The `id`, `customerId`, `amount`, and `createdAt` stay the same. The order stays in the book; it is not removed.

> **TODO (thread safety):** The method reads the order, checks it, then writes it back as separate steps. These steps are not atomic. If two threads cancel the same order at the same time, both can return `true`. Should this use `store.computeIfPresent(...)` or `replace(key, old, new)`?

###### `totalFor`

```java
public BigDecimal totalFor(String customerId)
```
Adds up the `amount` of every order for a customer that is still `NEW`. Cancelled orders are left out.

- **Parameters:**
  - `customerId` (`String`): the customer to total. The match is exact and case-sensitive. Must not be `null`.
- **Returns:** `BigDecimal`, the sum of matching amounts. Returns `BigDecimal.ZERO` if the customer has no `NEW` orders or is unknown. The scale of the result follows normal `BigDecimal.add` rules, which means the largest scale among the amounts.
- **Throws:**
  - `NullPointerException` if `customerId` is `null` **and** the book has at least one order. If the book is empty, a `null` `customerId` returns `ZERO` without an error.

> **TODO:** Make `null` handling consistent: either always reject it or always return `ZERO`. Also confirm the name `totalFor`, since it covers only open (`NEW`) orders, not all orders.

##### Value Types

###### `record Order`

```java
public record Order(
        String id,
        String customerId,
        BigDecimal amount,
        OrderStatus status,
        Instant createdAt) {}
```
An immutable snapshot of one order. Records give you the accessor methods (`id()`, `customerId()`, and so on) plus `equals`, `hashCode`, and `toString` automatically.

| Field        | Type          | Description |
|--------------|---------------|-------------|
| `id`         | `String`      | Unique order ID. `OrderBook.create` sets it to a random UUID string. |
| `customerId` | `String`      | ID of the customer who placed the order. Not blank when created through `OrderBook`. |
| `amount`     | `BigDecimal`  | Order value. Greater than zero when created through `OrderBook`. No currency is stored. |
| `status`     | `OrderStatus` | Current lifecycle state (`NEW` or `CANCELLED`). |
| `createdAt`  | `Instant`     | When the order was created (UTC instant). Stays the same after cancellation. |

> **TODO:** `Order` does not validate anything itself, so calling `new Order(...)` directly can create orders with null or invalid fields. Consider adding a compact constructor that validates, or document that orders should only be created through `OrderBook.create`.

###### `enum OrderStatus`

```java
public enum OrderStatus { NEW, CANCELLED }
```

| Constant    | Meaning |
|-------------|---------|
| `NEW`       | The order is active. It counts toward `totalFor` and can be cancelled. |
| `CANCELLED` | The order was cancelled through `cancel`. It is still stored and returned by `list`/`find`, but `totalFor` leaves it out. This is a final state. |

##### Quick example

```java
OrderBook book = new OrderBook();
Order a = book.create("cust-42", new BigDecimal("19.99"));
Order b = book.create("cust-42", new BigDecimal("5.01"));
System.out.println(book.find(a.id()).map(Order::status).orElseThrow()); // NEW
System.out.println(book.totalFor("cust-42"));                           // 25.00
System.out.println(book.cancel(b.id()));                                // true
System.out.println(book.cancel(b.id()));                                // false (already cancelled)
System.out.println(book.totalFor("cust-42"));                           // 19.99
```

### Verification

- All 5 public methods (`list`, `find`, `create`, `cancel`, `totalFor`) are there with the correct names and signatures, plus the implicit constructor.
- `Order` (all 5 fields) and `OrderStatus` (both constants) are documented.
- Quick example: pasted into a scratch `main`, added `import java.math.BigDecimal;`, compiled with `javac --release 17` and ran. Output: `NEW`, `25.00`, `true`, `false`, `19.99` - matches the comments.
- `IllegalArgumentException`: grep finds only two `throw new IllegalArgumentException` in `OrderBook`, both in `create`. Both are listed with the right messages.
- Checked the `NullPointerException` claims by running them: `find(null)` and `cancel(null)` throw NPE, and `new OrderBook().totalFor(null)` returns `0` while it throws once the book has an order. All correct.
- No missing exception documentation, so nothing to add by hand. I left the AI's TODO notes in, since they are open design questions and not errors.

---

## Part D - Reflect

- **How many JavaDoc inconsistencies did the AI's self-check catch on the first pass?** 0 direct contradictions, but it flagged 3 misleading statements ("unique" identifier, "whitespace only" vs. `isBlank()`, and a `@throws RuntimeException` that can't happen) and pointed out that most of the TODOs could be answered from the code. After fixing those, the second pass found nothing.
- **How many invented features did the README pass produce?** 0. Every target, version and method it mentioned is in the Makefile or source.
- **Which docs artefact needed the most hand editing, and why?** The README. The AI only got a truncated view of the source, so it left the Java quick example and Contributing as TODO, and it padded the description with assignment notes copied from the old README. I had to write the example (and run it), write Contributing, and cut the description down. The API reference needed no real edits because it had the full source; it even caught the `NullPointerException` cases from `ConcurrentHashMap`. The JavaDoc only needed small wording fixes.

---

## README.raw.md

````markdown
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
````

## README.md

````markdown
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
````
