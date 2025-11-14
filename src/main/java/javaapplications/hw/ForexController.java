package javaapplications.hw;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/forex")
public class ForexController {

    private final OandaClient oandaClient;

    public ForexController(OandaClient oandaClient) {
        this.oandaClient = oandaClient;
    }

    @GetMapping("/account")
    public String showAccount(Model model) {
        try {
            OandaAccountSummary account = oandaClient.getAccountSummary();
            model.addAttribute("account", account);
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "forex-account";
    }

    @GetMapping("/akt-ar")
    public String showCurrentPrice(
            @RequestParam(name = "instrument", required = false) String instrument,
            Model model) {

        List<String> instruments = List.of(
                "EUR_USD",
                "EUR_HUF",
                "USD_HUF",
                "GBP_USD"
        );

        model.addAttribute("instruments", instruments);
        model.addAttribute("selectedInstrument", instrument);

        ForexPrice price = null;
        String errorMessage = null;

        if (instrument != null && !instrument.isBlank()) {
            try {
                price = oandaClient.getCurrentPrice(instrument);
            } catch (RuntimeException e) {
                errorMessage = e.getMessage();
            }
        }

        model.addAttribute("price", price);
        model.addAttribute("errorMessage", errorMessage);

        return "forex-akt-ar";
    }

    @GetMapping("/hist-ar")
    public String showHistoricalPrices(
            @RequestParam(name = "instrument", required = false) String instrument,
            @RequestParam(name = "granularity", required = false, defaultValue = "D") String granularity,
            Model model) {

        List<String> instruments = List.of(
                "EUR_USD",
                "EUR_HUF",
                "USD_HUF",
                "GBP_USD"
        );

        List<String> granularities = List.of(
                "D",
                "H4",
                "H1"
        );

        model.addAttribute("instruments", instruments);
        model.addAttribute("granularities", granularities);
        model.addAttribute("selectedInstrument", instrument);
        model.addAttribute("selectedGranularity", granularity);

        List<ForexHistoricalPoint> history = List.of();
        String errorMessage = null;

        if (instrument != null && !instrument.isBlank()) {
            try {
                history = oandaClient.getHistoricalPrices(instrument, granularity, 10);
            } catch (RuntimeException e) {
                errorMessage = e.getMessage();
            }
        }

        model.addAttribute("history", history);
        model.addAttribute("errorMessage", errorMessage);

        return "forex-hist-ar";
    }
}
