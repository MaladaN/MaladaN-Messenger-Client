package net.strangled.maladan.shared;

public class MessengerConversation {
    private String contactName;
    private String contactPhotoPath = "./MainInterfaceIcon.png";

    public MessengerConversation(String contactName, String contactPhotoPath) {
        this.contactName = contactName;
        this.contactPhotoPath = contactPhotoPath;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactPhotoPath() {
        return contactPhotoPath;
    }

    public void setContactPhotoPath(String contactPhotoPath) {
        this.contactPhotoPath = contactPhotoPath;
    }
}
