package net.strangled.maladan.serializables.Authentication;


public class User implements java.io.Serializable {
    //Used by the server to keep track of sessions.

    private boolean loggedIn;
    private String username;

    public User(boolean loggedIn, String username) {
        this.loggedIn = loggedIn;
        this.username = username;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public String getUsername() {
        return username;
    }
}
