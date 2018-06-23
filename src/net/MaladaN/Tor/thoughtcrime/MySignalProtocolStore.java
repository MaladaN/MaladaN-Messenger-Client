package net.MaladaN.Tor.thoughtcrime;


import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.SessionRecord;
import org.whispersystems.libsignal.state.SignalProtocolStore;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MySignalProtocolStore implements SignalProtocolStore {

    @Override
    public IdentityKeyPair getIdentityKeyPair() {
        Connection conn = GetSQLConnection.getConn();
        IdentityKeyPair keyPair = null;
        if (conn != null) {
            try {
                String sql = "SELECT identityKeyPair FROM localIdentityStorage";
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                rs.next();
                byte[] testKeyPair = rs.getBytes("identityKeyPair");
                keyPair = new IdentityKeyPair(testKeyPair);
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return keyPair;
        } else {
            return null;
        }
    }

    @Override
    public int getLocalRegistrationId() {
        Connection conn = GetSQLConnection.getConn();
        int localRegId = 0;
        if (conn != null) {
            try {
                String sql = "SELECT localRegistrationId FROM localIdentityStorage";
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                rs.next();
                localRegId = rs.getInt("localRegistrationId");
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return localRegId;
    }

    @Override
    public boolean saveIdentity(SignalProtocolAddress signalProtocolAddress, IdentityKey identityKey) {
        Connection conn = GetSQLConnection.getConn();
        boolean alreadyExists = isTrustedIdentity(signalProtocolAddress, identityKey, Direction.SENDING);
        if (conn != null && !alreadyExists) {
            try {
                String sql = "INSERT INTO identityKeyStorage (signalProtocolAddress, identityKey) VALUES (?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, signalProtocolAddress.toString());
                ps.setBytes(2, identityKey.serialize());
                ps.execute();
                conn.close();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean isTrustedIdentity(SignalProtocolAddress signalProtocolAddress, IdentityKey identityKey, Direction direction) {
        Connection conn = GetSQLConnection.getConn();
        boolean isTrusted = false;
        if (identityKey != null) {
            if (conn != null) {
                try {
                    String sql = "SELECT signalProtocolAddress, identityKey FROM identityKeyStorage WHERE identityKey=?";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setBytes(1, identityKey.serialize());
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        if (signalProtocolAddress.toString().equals(rs.getString("signalProtocolAddress"))) {
                            isTrusted = true;
                        }
                    }
                    conn.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (conn != null) {
                try {
                    String sql = "SELECT signalProtocolAddress FROM identityKeyStorage WHERE signalProtocolAddress=?";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1, signalProtocolAddress.toString());
                    ResultSet rs = ps.executeQuery();
                    boolean exists = rs.next();
                    conn.close();
                    return exists;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return isTrusted;
    }

    @Override
    public PreKeyRecord loadPreKey(int i) {
        Connection conn = GetSQLConnection.getConn();
        PreKeyRecord recoveredRecord = null;
        if (conn != null) {
            try {
                String sql = "SELECT preKeyRecord FROM preKeyStorage WHERE keyId =?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, i);
                ResultSet rs = ps.executeQuery();
                rs.next();
                recoveredRecord = new PreKeyRecord(rs.getBytes("preKeyRecord"));
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return recoveredRecord;
    }

    @Override
    public void storePreKey(int i, PreKeyRecord preKeyRecord) {
        Connection conn = GetSQLConnection.getConn();
        if (conn != null) {
            try {
                String sql = "INSERT INTO preKeyStorage (preKeyRecord, keyId) VALUES (?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setBytes(1, preKeyRecord.serialize());
                ps.setInt(2, i);
                ps.execute();
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean containsPreKey(int i) {
        boolean exists = false;
        try {
            if (loadPreKey(i) != null) {
                exists = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return exists;
    }

    @Override
    public void removePreKey(int i) {
        Connection conn = GetSQLConnection.getConn();
        if (conn != null) {
            try {
                String sql = "DELETE FROM preKeyStorage WHERE keyId=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, i);
                ps.execute();
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public SessionRecord loadSession(SignalProtocolAddress signalProtocolAddress) {
        Connection conn = GetSQLConnection.getConn();
        SessionRecord sr = null;
        if (conn != null) {
            try {
                String sql = "SELECT sessionRecord FROM sessionStoreStorage WHERE protocolAddress=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, signalProtocolAddress.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    sr = new SessionRecord(rs.getBytes("sessionRecord"));
                }
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (sr != null) {
            return sr;
        } else {
            return new SessionRecord();
        }
    }

    @Override
    public List<Integer> getSubDeviceSessions(String s) {
        Connection conn = GetSQLConnection.getConn();
        List<Integer> retrievedIds = new ArrayList<>();
        if (conn != null) {
            try {
                String sql = "SELECT * FROM sessionStoreStorage";
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String protoToString = rs.getString("protocolAddress");
                    if (protoToString.contains(s)) {
                        StringBuilder tempInteger = new StringBuilder();
                        boolean passedColon = false;
                        for (char c : protoToString.toCharArray()) {
                            if (Character.isDigit(c) && passedColon) {
                                tempInteger.append(c);
                            }
                            if (c == ':') {
                                passedColon = true;
                            }
                        }
                        if (!tempInteger.toString().equals("")) {
                            retrievedIds.add(Integer.valueOf(tempInteger.toString()));
                        }
                    }
                }
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (retrievedIds.isEmpty()) {
            return null;
        } else {
            return retrievedIds;
        }
    }

    @Override
    public void storeSession(SignalProtocolAddress signalProtocolAddress, SessionRecord sessionRecord) {
        Connection conn = GetSQLConnection.getConn();
        if (conn != null) {
            if (!containsSession(signalProtocolAddress)) {
                try {
                    String sql = "INSERT INTO sessionStoreStorage (protocolAddress, sessionRecord) VALUES (?, ?)";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1, signalProtocolAddress.toString());
                    ps.setBytes(2, sessionRecord.serialize());
                    ps.execute();
                    conn.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    String sql = "UPDATE sessionStoreStorage SET sessionRecord = ? WHERE protocolAddress = ?";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setBytes(1, sessionRecord.serialize());
                    ps.setString(2, signalProtocolAddress.toString());
                    ps.execute();
                    conn.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    @Override
    public boolean containsSession(SignalProtocolAddress signalProtocolAddress) {
        Connection conn = GetSQLConnection.getConn();
        if (conn != null) {
            try {
                String sql = "SELECT sessionRecord FROM sessionStoreStorage WHERE protocolAddress=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, signalProtocolAddress.toString());
                ResultSet rs = ps.executeQuery();
                boolean valid = rs.next();
                conn.close();
                return valid;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public void deleteSession(SignalProtocolAddress signalProtocolAddress) {
        Connection conn = GetSQLConnection.getConn();
        if (conn != null) {
            try {
                String sql = "DELETE FROM sessionStoreStorage WHERE protocolAddress=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, signalProtocolAddress.toString());
                ps.execute();
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void deleteAllSessions(String s) {
        Connection conn = GetSQLConnection.getConn();
        if (conn != null) {
            try {
                String sql = "DELETE FROM sessionStoreStorage WHERE protocolAddress like ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, ('%' + s + '%'));
                ps.execute();
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public SignedPreKeyRecord loadSignedPreKey(int i) {
        Connection conn = GetSQLConnection.getConn();
        SignedPreKeyRecord pkr = null;
        if (conn != null) {
            try {
                String sql = "SELECT signedPreKeyRecord FROM signedPreKeyStore WHERE keyId = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, i);
                ResultSet rs = ps.executeQuery();
                rs.next();
                pkr = new SignedPreKeyRecord(rs.getBytes("signedPreKeyRecord"));
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return pkr;
    }

    @Override
    public List<SignedPreKeyRecord> loadSignedPreKeys() {
        Connection conn = GetSQLConnection.getConn();
        List<SignedPreKeyRecord> pkr = new ArrayList<>();
        if (conn != null) {
            try {
                String sql = "SELECT signedPreKeyRecord FROM signedPreKeyStore";
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    pkr.add(new SignedPreKeyRecord(rs.getBytes("signedPreKeyRecord")));
                }
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (pkr.isEmpty()) {
            return null;
        } else {
            return pkr;
        }
    }

    @Override
    public void storeSignedPreKey(int i, SignedPreKeyRecord signedPreKeyRecord) {
        Connection conn = GetSQLConnection.getConn();
        if (conn != null) {
            try {
                String sql = "INSERT INTO signedPreKeyStore (signedPreKeyRecord, keyId) VALUES (?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setBytes(1, signedPreKeyRecord.serialize());
                ps.setInt(2, i);
                ps.execute();
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean containsSignedPreKey(int i) {
        Connection conn = GetSQLConnection.getConn();
        if (conn != null) {
            try {
                String sql = "SELECT signedPreKeyRecord FROM signedPreKeyStore WHERE keyId = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, i);
                ResultSet rs = ps.executeQuery();
                boolean contains = rs.next();
                conn.close();
                return contains;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public void removeSignedPreKey(int i) {
        Connection conn = GetSQLConnection.getConn();
        if (conn != null) {
            try {
                String sql = "DELETE FROM signedPreKeyStorage WHERE keyId = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, i);
                ps.execute();
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
