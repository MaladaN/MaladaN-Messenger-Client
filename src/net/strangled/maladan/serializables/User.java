package net.strangled.maladan.serializables;


public class User implements java.io.Serializable {
    //Used by the server to keep track of sessions.

    private boolean loggedIn;

    public User(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

}
