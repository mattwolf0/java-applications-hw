package javaapplications.hw;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/forex")
public class ForexController {

    private final OandaClient oandaClient;
    private final ForexTradeService tradeService;

    public ForexController(OandaClient oandaClient, ForexTradeService tradeService) {
        this.oandaClient = oandaClient;
        this.tradeService = tradeService;
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

    @GetMapping("/nyit")
    public String openPosition(
            @RequestParam(name = "instrument", required = false) String instrument,
            @RequestParam(name = "units", required = false) BigDecimal units,
            Model model) {

        List<String> instruments = List.of(
                "EUR_USD",
                "EUR_HUF",
                "USD_HUF",
                "GBP_USD"
        );

        model.addAttribute("instruments", instruments);
        model.addAttribute("selectedInstrument", instrument);
        model.addAttribute("units", units);

        String errorMessage = null;
        ForexTrade openedTrade = null;

        if (instrument != null && !instrument.isBlank()
                && units != null && units.compareTo(BigDecimal.ZERO) != 0) {
            try {
                ForexPrice price = oandaClient.getCurrentPrice(instrument);
                openedTrade = tradeService.openTrade(instrument, units, price.getMid());
            } catch (RuntimeException e) {
                errorMessage = e.getMessage();
            }
        }

        model.addAttribute("openedTrade", openedTrade);
        model.addAttribute("errorMessage", errorMessage);

        return "forex-nyit";
    }

    @GetMapping("/poz")
    public String showOpenPositions(Model model) {
        List<ForexTrade> trades = tradeService.getOpenTrades();
        model.addAttribute("trades", trades);
        return "forex-poz";
    }

    @GetMapping("/zar")
    public String closePosition(
            @RequestParam(name = "tradeId", required = false) Long tradeId,
            Model model) {

        List<ForexTrade> trades = tradeService.getOpenTrades();
        model.addAttribute("trades", trades);
        model.addAttribute("tradeId", tradeId);

        ForexTrade closedTrade = null;
        String errorMessage = null;

        if (tradeId != null) {
            try {
                ForexTrade trade = tradeService.getTradeById(tradeId);
                if (trade == null) {
                    throw new IllegalArgumentException("Nincs ilyen trade azonosító.");
                }
                ForexPrice price = oandaClient.getCurrentPrice(trade.getInstrument());
                closedTrade = tradeService.closeTrade(tradeId, price.getMid());
            } catch (RuntimeException e) {
                errorMessage = e.getMessage();
            }
        }

        model.addAttribute("closedTrade", closedTrade);
        model.addAttribute("errorMessage", errorMessage);

        return "forex-zar";
    }
}
