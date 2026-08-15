package doller;

import tools.jackson.databind.JsonNode;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

public record Exchange(String name,
                       String symbol,
                       String url,
                       Duration interval,
                       Function<JsonNode, String> price) {

    public static final List<Exchange> ALL = List.of(
            new Exchange("binance", "BTC/USDT",
                    "https://api.binance.com/api/v3/ticker/price?symbol=BTCUSDT",
                    Duration.ofSeconds(1),
                    j -> j.get("price").asString()),

            new Exchange("bybit", "BTC/USDT",
                    "https://api.bybit.com/v5/market/tickers?category=spot&symbol=BTCUSDT",
                    Duration.ofSeconds(2),
                    j -> j.get("result").get("list").get(0).get("lastPrice").asString()),

            new Exchange("okx", "BTC/USDT",
                    "https://www.okx.com/api/v5/market/ticker?instId=BTC-USDT",
                    Duration.ofSeconds(2),
                    j -> j.get("data").get(0).get("last").asString()),

            new Exchange("kraken", "BTC/USD",
                    "https://api.kraken.com/0/public/Ticker?pair=XBTUSD",
                    Duration.ofSeconds(3),
                    j -> j.get("result").iterator().next().get("c").get(0).asString()),

            new Exchange("coinbase", "BTC/USD",
                    "https://api.coinbase.com/v2/prices/BTC-USD/spot",
                    Duration.ofSeconds(3),
                    j -> j.get("data").get("amount").asString()),

            new Exchange("bitstamp", "BTC/USD",
                    "https://www.bitstamp.net/api/v2/ticker/btcusd/",
                    Duration.ofSeconds(5),
                    j -> j.get("last").asString())
    );
}
