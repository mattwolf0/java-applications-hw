package javaapplications.hw;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OandaClient {

    private final RestTemplate restTemplate = new RestTemplate();

    private final String apiUrl;
    private final String accountId;
    private final String apiKey;

    public OandaClient(
            @Value("${oanda.api-url:}") String apiUrl,
            @Value("${oanda.account-id:}") String accountId,
            @Value("${oanda.api-key:}") String apiKey) {
        this.apiUrl = apiUrl;
        this.accountId = accountId;
        this.apiKey = apiKey;
    }

    public OandaAccountSummary getAccountSummary() {
        if (accountId == null || accountId.isBlank()
                || apiKey == null || apiKey.isBlank()) {
            return createMockAccount();
        }

        String effectiveApiUrl = (apiUrl == null || apiUrl.isBlank())
                ? "https://api-fxpractice.oanda.com/v3"
                : apiUrl;

        String url = effectiveApiUrl + "/accounts/" + accountId + "/summary";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<AccountSummaryResponse> response =
                    restTemplate.exchange(url, HttpMethod.GET, entity, AccountSummaryResponse.class);
            AccountSummaryResponse body = response.getBody();
            if (body == null || body.getAccount() == null) {
                throw new IllegalStateException("A Forex számla adatai nem érhetők el.");
            }
            return body.getAccount();
        } catch (HttpStatusCodeException e) {
            String message = "A Forex szolgáltató HTTP hibát adott vissza. Státusz: "
                    + e.getStatusCode().value();
            throw new IllegalStateException(message, e);
        } catch (RestClientException e) {
            throw new IllegalStateException("A Forex szolgáltató elérése nem sikerült.", e);
        }
    }

    public ForexPrice getCurrentPrice(String instrument) {
        if (instrument == null || instrument.isBlank()) {
            throw new IllegalArgumentException("Instrument megadása kötelező.");
        }

        if (accountId == null || accountId.isBlank()
                || apiKey == null || apiKey.isBlank()) {
            ForexPrice mock = new ForexPrice();
            mock.setInstrument(instrument);
            mock.setBid(new BigDecimal("1.1000"));
            mock.setAsk(new BigDecimal("1.1010"));
            mock.setMid(new BigDecimal("1.1005"));
            mock.setTime(OffsetDateTime.now());
            return mock;
        }

        String effectiveApiUrl = (apiUrl == null || apiUrl.isBlank())
                ? "https://api-fxpractice.oanda.com/v3"
                : apiUrl;

        String url = effectiveApiUrl + "/accounts/" + accountId + "/pricing?instruments=" + instrument;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            String body = response.getBody();
            if (body == null || body.isBlank()) {
                throw new IllegalStateException("A Forex árfolyam adat nem érhető el.");
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(body);

            JsonNode prices = root.path("prices");
            if (!prices.isArray() || prices.isEmpty()) {
                throw new IllegalStateException("A Forex árfolyam adat nem érhető el.");
            }

            JsonNode p = prices.get(0);

            BigDecimal bid = new BigDecimal(
                    p.path("bids").get(0).path("price").asText());
            BigDecimal ask = new BigDecimal(
                    p.path("asks").get(0).path("price").asText());
            BigDecimal mid = bid.add(ask)
                    .divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP);

            String timeStr = p.path("time").asText();
            OffsetDateTime time = OffsetDateTime.parse(timeStr);

            ForexPrice result = new ForexPrice();
            result.setInstrument(instrument);
            result.setBid(bid);
            result.setAsk(ask);
            result.setMid(mid);
            result.setTime(time);
            return result;

        } catch (HttpStatusCodeException e) {
            String message = "A Forex szolgáltató HTTP hibát adott vissza. Státusz: "
                    + e.getStatusCode().value();
            throw new IllegalStateException(message, e);
        } catch (Exception e) {
            throw new IllegalStateException("A Forex árfolyam adat lekérdezése nem sikerült.", e);
        }
    }

    public List<ForexHistoricalPoint> getHistoricalPrices(String instrument,
                                                          String granularity,
                                                          int count) {
        if (instrument == null || instrument.isBlank()) {
            throw new IllegalArgumentException("Instrument megadása kötelező.");
        }
        if (granularity == null || granularity.isBlank()) {
            granularity = "D";
        }
        if (count <= 0) {
            count = 10;
        }

        if (accountId == null || accountId.isBlank()
                || apiKey == null || apiKey.isBlank()) {

            List<ForexHistoricalPoint> mock = new ArrayList<>();
            BigDecimal base = new BigDecimal("1.1000");
            OffsetDateTime baseTime = OffsetDateTime.now();

            for (int i = 0; i < count; i++) {
                int step = count - i;
                OffsetDateTime t;
                if ("H1".equals(granularity)) {
                    t = baseTime.minusHours(step);
                } else if ("H4".equals(granularity)) {
                    t = baseTime.minusHours(4L * step);
                } else {
                    t = baseTime.minusDays(step);
                }

                ForexHistoricalPoint p = new ForexHistoricalPoint();
                p.setTime(t);
                p.setClose(base.add(BigDecimal.valueOf(i).multiply(new BigDecimal("0.0010"))));
                mock.add(p);
            }
            return mock;
        }

        String effectiveApiUrl = (apiUrl == null || apiUrl.isBlank())
                ? "https://api-fxpractice.oanda.com/v3"
                : apiUrl;

        String url = effectiveApiUrl + "/instruments/" + instrument
                + "/candles?granularity=" + granularity + "&count=" + count + "&price=M";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            String body = response.getBody();
            if (body == null || body.isBlank()) {
                throw new IllegalStateException("A historikus árfolyam adat nem érhető el.");
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(body);

            JsonNode candles = root.path("candles");
            if (!candles.isArray() || candles.isEmpty()) {
                throw new IllegalStateException("A historikus árfolyam adat nem érhető el.");
            }

            List<ForexHistoricalPoint> result = new ArrayList<>();
            for (JsonNode c : candles) {
                String timeStr = c.path("time").asText();
                OffsetDateTime time = OffsetDateTime.parse(timeStr);
                String closeStr = c.path("mid").path("c").asText();
                BigDecimal close = new BigDecimal(closeStr);

                ForexHistoricalPoint p = new ForexHistoricalPoint();
                p.setTime(time);
                p.setClose(close);
                result.add(p);
            }

            return result;

        } catch (HttpStatusCodeException e) {
            String message = "A Forex szolgáltató HTTP hibát adott vissza. Státusz: "
                    + e.getStatusCode().value();
            throw new IllegalStateException(message, e);
        } catch (Exception e) {
            throw new IllegalStateException("A historikus árfolyam adat lekérdezése nem sikerült.", e);
        }
    }

    private OandaAccountSummary createMockAccount() {
        OandaAccountSummary mock = new OandaAccountSummary();
        mock.setId("DEMO-ACCOUNT");
        mock.setAlias("Bemutató");
        mock.setCurrency("EUR");
        mock.setNav("10000.00");
        mock.setMarginAvailable("9500.00");
        mock.setMarginRate("0.05");
        mock.setOpenTradeCount(1);
        mock.setOpenPositionCount(1);
        mock.setPendingOrderCount(0);
        return mock;
    }
}
