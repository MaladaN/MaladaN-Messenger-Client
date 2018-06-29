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
        if (registrationId != 0 && identityKey != null) {
            this.registrationId = registrationId;

            //add the temporary pre keys (deleted by the server on use)
            addPreKeys(preKeys);

            //add the signed pre key (changed on a weekly basis)
            updateSignedPreKey(signedPreKey);

            this.identityKey = identityKey.serialize();
        }
    }

    public void updateSignedPreKey(SignedPreKeyRecord signedPreKey) {
        if (signedPreKey != null) {
            this.signedPreKeyId = signedPreKey.getId();
            this.signedPreKeyPublic = signedPreKey.getKeyPair().getPublicKey().serialize();
            this.signedPreKeySignature = signedPreKey.getSignature();
        }
    }

    public void addPreKeys(List<PreKeyRecord> preKeys) {
        if (preKeys != null) {
            this.preKeys.clear();

            for (PreKeyRecord record : preKeys) {
                this.preKeys.add(new PreKeyPublic(record.getKeyPair().getPublicKey(), record.getId()));
            }
        }
    }

    public int getNumberOfPreKeys() {
        return this.preKeys.size();
    }

    public ServerResponsePreKeyBundle getServerResponsePreKeyBundle() {
        int randomPreKeyIdPuller = ThreadLocalRandom.current().nextInt(0, preKeys.size());

        try {
            PreKeyPublic recordToSend = preKeys.get(randomPreKeyIdPuller);
            preKeys.remove(randomPreKeyIdPuller);

            return new ServerResponsePreKeyBundle(registrationId, recordToSend, signedPreKeyId, signedPreKeyPublic, signedPreKeySignature, identityKey);

        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public IdentityKey getIdKey() throws Exception {
        return new IdentityKey(identityKey, 0);
    }
}
