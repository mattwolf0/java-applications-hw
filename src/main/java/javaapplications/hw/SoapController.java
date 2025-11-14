package javaapplications.hw;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
public class SoapController {

    @GetMapping("/soap")
    public String showSoapPage(
            @RequestParam(name = "currency", required = false) String currency,
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate,
            Model model) {

        // legördülő listához devizák
        List<String> currencies = List.of("EUR", "USD", "CHF", "GBP");
        model.addAttribute("currencies", currencies);

        model.addAttribute("selectedCurrency", currency);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        List<ExchangeRatePoint> rates = new ArrayList<>();

        // ha az űrlapot már elküldték: egyelőre demo adatokkal
        if (currency != null && !currency.isBlank()
                && startDate != null && !startDate.isBlank()
                && endDate != null && !endDate.isBlank()) {

            LocalDate start = LocalDate.parse(startDate);

            // 10 napnyi demo adat – később itt lesz az MNB SOAP hívás
            for (int i = 0; i < 10; i++) {
                LocalDate d = start.plusDays(i);
                BigDecimal rate = BigDecimal.valueOf(390 + i * 1.5); // demo értékek
                rates.add(new ExchangeRatePoint(d, rate));
            }
        }

        model.addAttribute("rates", rates);

        return "soap"; // src/main/resources/templates/soap.html
    }
}
