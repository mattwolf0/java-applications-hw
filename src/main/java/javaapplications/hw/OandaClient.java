package javaapplications.hw;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

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
