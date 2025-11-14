package javaapplications.hw;

public class AccountSummaryResponse {

    private OandaAccountSummary account;
    private String lastTransactionID;

    public OandaAccountSummary getAccount() {
        return account;
    }

    public void setAccount(OandaAccountSummary account) {
        this.account = account;
    }

    public String getLastTransactionID() {
        return lastTransactionID;
    }

    public void setLastTransactionID(String lastTransactionID) {
        this.lastTransactionID = lastTransactionID;
    }
}
