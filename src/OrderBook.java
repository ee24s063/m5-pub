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
