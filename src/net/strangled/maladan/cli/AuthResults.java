package net.strangled.maladan.cli;


public class AuthResults {

    private String formattedResults;
    private boolean valid;

    public AuthResults(String formattedResults, boolean valid) {
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
