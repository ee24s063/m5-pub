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
     * Create a new order for a customer and add it to the order book.
     *
     * <p>The returned order has a newly generated unique id, status
     * {@link OrderStatus#NEW}, and the current time as its creation time.
     *
     * @param customerId the {@code String} id of the customer placing the order;
     *                   must not be {@code null}, empty, or whitespace only
     * @param amount     the {@code BigDecimal} amount of the order; must not be
     *                   {@code null} and must be strictly greater than zero
     * @return the newly created {@link Order}, which can be looked up
     *         afterwards with {@link #find(String)}
     * @throws IllegalArgumentException if {@code customerId} is {@code null} or
     *         blank, or if {@code amount} is {@code null}, zero, or negative
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
