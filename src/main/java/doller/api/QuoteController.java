package doller.api;

import doller.config.DollerProperties;
import doller.domain.Quote;
import doller.store.QuoteStore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.BufferOverflowStrategy;
import reactor.core.publisher.Flux;

import java.util.Collection;

@RestController
@RequestMapping("/api/quotes")
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteStore store;
    private final DollerProperties properties;

    @GetMapping(path = "/snapshot", produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<Quote> snapshot() {
        return store.snapshot();
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Quote> stream() {
        DollerProperties.Stream stream = properties.stream();
        return Flux.concat(
                        Flux.fromIterable(store.snapshot()),
                        store.updates()
                                .groupBy(Quote::exchange)
                                .flatMap(group -> group.sample(stream.sample())))
                .onBackpressureBuffer(stream.bufferSize(), BufferOverflowStrategy.DROP_OLDEST);
    }
}
