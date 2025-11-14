package javaapplications.hw;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class SoapController {

    private final MnbSoapClient mnbSoapClient;

    public SoapController(MnbSoapClient mnbSoapClient) {
        this.mnbSoapClient = mnbSoapClient;
    }

    @GetMapping("/soap")
    public String showSoapPage(
            @RequestParam(name = "currency", required = false) String currency,
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate,
            Model model) {

        List<String> currencies = List.of("EUR", "USD", "CHF", "GBP");
        model.addAttribute("currencies", currencies);

        model.addAttribute("selectedCurrency", currency);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        List<ExchangeRatePoint> rates = List.of();
        String errorMessage = null;

        if (currency != null && !currency.isBlank()
                && startDate != null && !startDate.isBlank()
                && endDate != null && !endDate.isBlank()) {

            try {
                rates = mnbSoapClient.getExchangeRates(startDate, endDate, currency);
                if (rates.isEmpty()) {
                    errorMessage = "Nem érkezett árfolyam adat a megadott intervallumra.";
                }
            } catch (Exception e) {
                errorMessage = e.getMessage();
            }
        }

        model.addAttribute("rates", rates);
        model.addAttribute("errorMessage", errorMessage);

        return "soap";
    }
}
