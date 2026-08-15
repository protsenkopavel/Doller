package doller;

import tools.jackson.databind.JsonNode;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class QuotePoller {

    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    private final WebClient http = WebClient.create();

    private final Map<String, Quote> latest = new ConcurrentHashMap<>();

    private final Sinks.Many<Quote> sink = Sinks.many().multicast().onBackpressureBuffer(256, false);

    @PostConstruct
    void start() {
        Exchange.ALL.forEach(ex ->
                Flux.interval(Duration.ZERO, ex.interval())
                        .onBackpressureDrop()
                        .concatMap(tick -> fetch(ex))
                        .filter(this::changed)
                        .subscribe(this::publish));
    }

    public Collection<Quote> snapshot() {
        return latest.values();
    }

    public Flux<Quote> updates() {
        return sink.asFlux();
    }

    private Mono<Quote> fetch(Exchange ex) {
        return http.get()
                .uri(ex.url())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(json -> new Quote(ex.name(), ex.symbol(),
                        new BigDecimal(ex.price().apply(json)), Instant.now()))
                .timeout(TIMEOUT)
                .retryWhen(Retry.backoff(2, Duration.ofMillis(200)))
                .onErrorResume(e -> {
                    log.warn("{}: опрос не удался — {}", ex.name(), e.toString());
                    return Mono.empty();
                });
    }

    private boolean changed(Quote q) {
        Quote prev = latest.get(q.exchange());
        return prev == null || prev.price().compareTo(q.price()) != 0;
    }

    private void publish(Quote q) {
        latest.put(q.exchange(), q);
        sink.emitNext(q, Sinks.EmitFailureHandler.busyLooping(Duration.ofMillis(100)));
    }
}
