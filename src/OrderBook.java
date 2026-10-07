import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Intentionally undocumented -- Session 5B asks students to add
// JavaDoc describing the contract of each public method.
public class OrderBook {

    private final Map<String, Order> store = new ConcurrentHashMap<>();

    public List<Order> list() {
        return List.copyOf(store.values());
    }

    public Optional<Order> find(String id) {
        return Optional.ofNullable(store.get(id));
    }

    /**
     * Create a new order for the given customer and amount, and store it.
     * <p>
     * The new order gets a randomly generated UUID string as its identifier,
     * the status {@link OrderStatus#NEW}, and the current time as its creation
     * timestamp. It is stored before it is returned, so it can be looked up
     * with {@link #find(String)}. The customer is not checked against any
     * existing records.
     *
     * @param customerId the {@code String} ID of the customer placing the order;
     *                   must not be {@code null} or blank (empty or only
     *                   whitespace, as defined by {@link String#isBlank()})
     * @param amount     the {@code BigDecimal} order amount; must not be
     *                   {@code null} and must be greater than zero. Any scale
     *                   is accepted.
     * @return the newly created {@link Order}, never {@code null}, with the
     *         given customer ID and amount, status {@code NEW}, and the
     *         creation time
     * @throws IllegalArgumentException if {@code customerId} is {@code null} or
     *         blank, or if {@code amount} is {@code null}, zero or negative
     */
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

    public boolean cancel(String id) {
        Order existing = store.get(id);
        if (existing == null || existing.status() == OrderStatus.CANCELLED) {
            return false;
        }
        store.put(id, new Order(existing.id(), existing.customerId(),
                                existing.amount(), OrderStatus.CANCELLED,
                                existing.createdAt()));
        return true;
    }

    public BigDecimal totalFor(String customerId) {
        return store.values().stream()
                .filter(o -> customerId.equals(o.customerId())
                          && o.status() == OrderStatus.NEW)
                .map(Order::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
