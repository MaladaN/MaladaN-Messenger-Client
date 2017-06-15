package net.strangled.maladan.serializables;

public class EncryptedMessage {

    //Encrypted with information of the person message is being sent to
    //Object: String, Base64
    private byte[] senderUsername;
    //Encrypted with the information of the server, the server will use this
    //to send the message to the correct end user
    //Object: String, Base64
    private byte[] recipientUserame;
    //Encrypted with recipient information, Is the actual message that is being
    //sent to the end user
    //Object: Object, could be a file, text message, or something else
    private byte[] message;

    public EncryptedMessage(byte[] senderUsername, byte[] recipientUserame, byte[] message) {
        this.senderUsername = senderUsername;
        this.recipientUserame = recipientUserame;
        this.message = message;
    }

    public byte[] getSenderUsername() {
        return senderUsername;
    }

    public byte[] getRecipientUserame() {
        return recipientUserame;
    }

    public byte[] getMessage() {
        return message;
    }
}

