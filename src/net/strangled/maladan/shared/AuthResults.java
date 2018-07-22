package net.strangled.maladan.shared;


public class AuthResults {

    private String formattedResults;
    private boolean valid;

    AuthResults(String formattedResults, boolean valid) {
        this.formattedResults = formattedResults;
        this.valid = valid;
    }

    public String getFormattedResults() {
        return formattedResults;
    }

    public boolean isValid() {
        return valid;
    }
}
