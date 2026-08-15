package doller.store;

import doller.domain.Quote;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class QuoteStore {

    private static final Duration EMIT_BUSY_LOOP = Duration.ofMillis(100);

    private final Map<String, Quote> latest = new ConcurrentHashMap<>();
    private final Sinks.Many<Quote> sink = Sinks.many().multicast().directBestEffort();

    public boolean changed(Quote quote) {
        Quote previous = latest.get(quote.exchange());
        return previous == null || previous.price().compareTo(quote.price()) != 0;
    }

    public void publish(Quote quote) {
        latest.put(quote.exchange(), quote);
        sink.emitNext(quote, Sinks.EmitFailureHandler.busyLooping(EMIT_BUSY_LOOP));
    }

    public Collection<Quote> snapshot() {
        return List.copyOf(latest.values());
    }

    public Flux<Quote> updates() {
        return sink.asFlux();
    }
}
