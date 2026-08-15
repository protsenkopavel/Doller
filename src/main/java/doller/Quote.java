package doller;

import java.math.BigDecimal;
import java.time.Instant;

public record Quote(String exchange, String symbol, BigDecimal price, Instant at) {
}
