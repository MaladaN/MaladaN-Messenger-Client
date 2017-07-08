package net.strangled.maladan.shared;

import java.util.LinkedList;

public class Contact {

    private String username;
    private LinkedList<String> messages;

    public Contact(String username, LinkedList<String> messages) {
        this.username = username;
        this.messages = messages;
    }

    public String getUsername() {
        return username;
    }

    public LinkedList<String> getMessages() {
        return messages;
    }
}
