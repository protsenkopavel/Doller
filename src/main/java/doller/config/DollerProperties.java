package doller.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;

@Validated
@ConfigurationProperties(prefix = "doller")
public record DollerProperties(@NotNull @Valid Http http,
                               @NotNull @Valid Stream stream,
                               @NotEmpty List<@Valid Exchange> exchanges) {

    public record Http(@NotNull Duration timeout,
                       @PositiveOrZero long retries,
                       @NotNull Duration retryBackoff) {
    }

    public record Stream(@NotNull Duration sample,
                         @Positive int bufferSize) {
    }

    public record Exchange(@NotBlank String name,
                           @NotBlank String symbol,
                           @NotBlank String url,
                           @NotNull Duration interval,
                           @NotBlank String pricePointer) {
    }
}
