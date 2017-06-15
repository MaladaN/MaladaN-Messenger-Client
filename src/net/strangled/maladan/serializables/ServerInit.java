package net.strangled.maladan.serializables;


import net.MaladaN.Tor.thoughtcrime.InitData;
import net.MaladaN.Tor.thoughtcrime.MySignalProtocolStore;
import net.MaladaN.Tor.thoughtcrime.SendInitData;
import org.whispersystems.libsignal.InvalidKeyIdException;
import org.whispersystems.libsignal.state.SignalProtocolStore;

public class ServerInit implements java.io.Serializable {
    //Used to initiate an account with the server, by the client.

    private byte[] username;
    private SendInitData initData;
    private boolean isNewUser;

    //only used for initial registration
    private String uniqueId;

    public ServerInit(byte[] username, String uniqueId, InitData data) throws InvalidKeyIdException {
        SignalProtocolStore store = new MySignalProtocolStore();
        this.username = username;
        this.uniqueId = uniqueId;
        if (isNewUser = (!(data == null))) {
            this.initData = new SendInitData(store.getLocalRegistrationId(), data.getPreKeyRecords(), store.loadSignedPreKey(data.getSignedPreKeyId()), store.getIdentityKeyPair().getPublicKey());
        }
        isNewUser = (uniqueId != null);
    }

    public byte[] getUsername() {
        return username;
    }

    public SendInitData getInitData() {
        return initData;
    }

    public boolean isNewUser() {
        return isNewUser;
    }

    public String getUniqueId() {
        return uniqueId;
    }

}
