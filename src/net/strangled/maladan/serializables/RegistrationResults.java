package net.strangled.maladan.serializables;


public class RegistrationResults {

    private String formattedResults;
    private boolean valid;

    public RegistrationResults(String formattedResults, boolean valid) {
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
