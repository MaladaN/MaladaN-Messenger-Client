package net.MaladaN.Tor.thoughtcrime;


import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class SendInitData implements java.io.Serializable {
    private int registrationId;
    private List<PreKeyPublic> preKeys = new LinkedList<>();
    private int signedPreKeyId;
    private byte[] signedPreKeyPublic;
    private byte[] signedPreKeySignature;
    private byte[] identityKey;

    public SendInitData(int registrationId, List<PreKeyRecord> preKeys, SignedPreKeyRecord signedPreKey, IdentityKey identityKey) {
        if (registrationId != 0 && preKeys != null && signedPreKey != null && identityKey != null) {
            this.registrationId = registrationId;

            for (PreKeyRecord record : preKeys) {
                this.preKeys.add(new PreKeyPublic(record.getKeyPair().getPublicKey(), record.getId()));
            }

            this.signedPreKeyId = signedPreKey.getId();
            this.signedPreKeyPublic = signedPreKey.getKeyPair().getPublicKey().serialize();
            this.signedPreKeySignature = signedPreKey.getSignature();
            this.identityKey = identityKey.serialize();
        }
    }

    public ServerResponsePreKeyBundle getServerResponsePreKeyBundle() {
        int randomPreKeyIdPuller = ThreadLocalRandom.current().nextInt(0, preKeys.size());
        return new ServerResponsePreKeyBundle(registrationId, preKeys.get(randomPreKeyIdPuller), signedPreKeyId, signedPreKeyPublic, signedPreKeySignature, identityKey);

    }
}
