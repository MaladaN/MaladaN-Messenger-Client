package net.MaladaN.Tor.thoughtcrime;

import org.whispersystems.libsignal.*;
import org.whispersystems.libsignal.protocol.CiphertextMessage;
import org.whispersystems.libsignal.protocol.PreKeySignalMessage;
import org.whispersystems.libsignal.protocol.SignalMessage;
import org.whispersystems.libsignal.state.PreKeyBundle;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.SignalProtocolStore;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;
import org.whispersystems.libsignal.util.KeyHelper;

import java.util.ArrayList;
import java.util.List;

public class SignalCrypto {

    public static byte[] encryptByteMessage(byte[] message, SignalProtocolAddress address, PreKeyBundle bundle) {
        SignalProtocolStore protocolStore = new MySignalProtocolStore();

        if (message != null) {

            if (!protocolStore.containsSession(address)) {

                if (bundle != null) {
                    boolean valid = trustIdentity(address, bundle.getIdentityKey());
                    if (valid) {
                        return encrypt(message, bundle, address);
                    }
                }
            } else {
                return encrypt(message, address);
            }
        }
        return null;
    }

    private static boolean trustIdentity(SignalProtocolAddress address, IdentityKey key) {
        SignalProtocolStore protocolStore = new MySignalProtocolStore();
        boolean contactedBefore = protocolStore.isTrustedIdentity(address, null);
        boolean isAlreadyTrusted = protocolStore.isTrustedIdentity(address, key);

        //can be expanded at a later time if necessary
        if (!contactedBefore && !isAlreadyTrusted) {
            protocolStore.saveIdentity(address, key);
            return true;
        } else {
            return isAlreadyTrusted;
        }
    }

    public static byte[] encryptStringMessage(String message, SignalProtocolAddress address, PreKeyBundle bundle) {
        //encrypt message
        byte[] byteString = null;
        try {
            byteString = message.getBytes("UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptByteMessage(byteString, address, bundle);
    }

    public static String decryptStringMessage(byte[] message, SignalProtocolAddress address) {
        try {
            byte[] cleartext = decryptMessage(message, address);
            if (cleartext != null) {
                return new String(cleartext, "UTF-8");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] decryptMessage(byte[] message, SignalProtocolAddress address) {
        //decrypt message

        try {
            byte[] returned = decrypt(message, address);

            if (returned != null) {
                return returned;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] encrypt(byte[] message, PreKeyBundle preKeyBundle, SignalProtocolAddress signalProtocolAddress) {
        if (preKeyBundle != null) {
            getSession(signalProtocolAddress, preKeyBundle);
        }
        SessionCipher sessionCipher = new SessionCipher(new MySignalProtocolStore(), signalProtocolAddress);

        try {
            CiphertextMessage message1 = sessionCipher.encrypt(message);
            return message1.serialize();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] encrypt(byte[] message, SignalProtocolAddress address) {
        return encrypt(message, null, address);
    }

    private static byte[] decrypt(byte[] message, SignalProtocolAddress senderAddress) {
        SessionCipher sessionCipher = new SessionCipher(new MySignalProtocolStore(), senderAddress);
        PreKeySignalMessage pks;
        SignalMessage signalMessage;

        if (message != null) {
            try {
                pks = new PreKeySignalMessage(message);
                boolean valid = trustIdentity(senderAddress, pks.getIdentityKey());
                if (valid) {
                    return sessionCipher.decrypt(pks);
                }
            } catch (Exception e) {
                try {
                    signalMessage = new SignalMessage(message);
                    return sessionCipher.decrypt(signalMessage);
                } catch (Exception f) {
                    e.printStackTrace();
                    f.printStackTrace();
                }
            }
        }
        return null;
    }

    private static InitData init() {

        //create data
        IdentityKeyPair identityKeyPair = KeyHelper.generateIdentityKeyPair();
        int registrationId = KeyHelper.generateRegistrationId(true);
        List<PreKeyRecord> preKeys = KeyHelper.generatePreKeys(2, 1000);
        SignalProtocolStore signalProtocolStore = new MySignalProtocolStore();

        ArrayList<PreKeyRecord> records = new ArrayList<>();
        int id = 0;

        //store signed PreKey
        try {
            SignedPreKeyRecord signedPreKey = KeyHelper.generateSignedPreKey(identityKeyPair, 5);
            signalProtocolStore.storeSignedPreKey(signedPreKey.getId(), signedPreKey);
            id = signedPreKey.getId();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //store required data as outlined in the documentation
        InitStore.storeIdentityKeyPairAndRegistrationId(identityKeyPair, registrationId);
        for (PreKeyRecord ps : preKeys) {
            signalProtocolStore.storePreKey(ps.getId(), ps);
            records.add(ps);
        }

        return new InitData(id, records);

        // Store identityKeyPair somewhere durable and safe.
        // Store registrationId somewhere durable and safe.

        // Store preKeys in PreKeyStore.
        // Store signed preKey in SignedPreKeyStore.
    }

    private static void getSession(SignalProtocolAddress address, PreKeyBundle preKeyBundle) {
        SignalProtocolStore signalProtocolStore = new MySignalProtocolStore();

        // Instantiate a SessionBuilder for a remote recipientId + deviceId tuple.
        SessionBuilder sessionBuilder = new SessionBuilder(signalProtocolStore,
                address);

        // Build a session with a PreKey retrieved from the server.
        try {
            sessionBuilder.process(preKeyBundle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //MUST BE CALLED BEFORE TRYING TO DO ANYTHING
    public static InitData initStore() {
        if (!InitStore.isInstalled()) {
            InitData data = init();
            InitStore.setInstalledFlagTrue();
            return data;
        }
        return null;
    }
}
