package doller.poller;

import doller.config.DollerProperties;
import doller.domain.Quote;
import doller.store.QuoteStore;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuotePoller {

    private final WebClient quoteWebClient;
    private final DollerProperties properties;
    private final QuoteStore store;

    @PostConstruct
    void start() {
        properties.exchanges().forEach(this::poll);
    }

    private void poll(DollerProperties.Exchange exchange) {
        Flux.interval(Duration.ZERO, exchange.interval())
                .onBackpressureDrop()
                .concatMap(tick -> fetch(exchange))
                .filter(store::changed)
                .subscribe(store::publish);
    }

    private Mono<Quote> fetch(DollerProperties.Exchange exchange) {
        DollerProperties.Http http = properties.http();
        return quoteWebClient.get()
                .uri(exchange.url())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(body -> toQuote(exchange, body))
                .timeout(http.timeout())
                .retryWhen(Retry.backoff(http.retries(), http.retryBackoff()))
                .onErrorResume(error -> {
                    log.warn("{}: опрос не удался — {}", exchange.name(), error.toString());
                    return Mono.empty();
                });
    }

    private Quote toQuote(DollerProperties.Exchange exchange, JsonNode body) {
        String price = body.at(exchange.pricePointer()).asString();
        return new Quote(exchange.name(), exchange.symbol(), new BigDecimal(price), Instant.now());
    }
}
