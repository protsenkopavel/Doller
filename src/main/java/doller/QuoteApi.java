package doller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;

@RestController
public class QuoteApi {

    private final QuotePoller poller;

    QuoteApi(QuotePoller poller) {
        this.poller = poller;
    }

    @GetMapping(path = "/quotes", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<Quote> stream() {
        return Flux.concat(
                Flux.fromIterable(poller.snapshot()),
                poller.updates()
                        .groupBy(Quote::exchange)
                        .flatMap(g -> g.sample(Duration.ofMillis(250)))
        );
    }
}
