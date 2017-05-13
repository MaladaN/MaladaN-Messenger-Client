package net.strangled.maladan;


public class LoginResponseState implements java.io.Serializable {
    //Sent to the client by the server, to tell the client the current authentication state.

    private boolean validLogin;

    public LoginResponseState(boolean validLogin) {
        this.validLogin = validLogin;
    }

    public boolean isValidLogin() {
        return validLogin;
    }

}
