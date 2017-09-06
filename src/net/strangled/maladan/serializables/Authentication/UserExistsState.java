package net.strangled.maladan.serializables.Authentication;


public class UserExistsState implements java.io.Serializable {

    private boolean userExists;

    public UserExistsState(boolean userExists) {
        this.userExists = userExists;
    }

    public boolean doesUserExists() {
        return userExists;
    }
}
