package javaapplications.hw;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
}
