package net.strangled.maladan.gui;

import net.MaladaN.Tor.thoughtcrime.GetSQLConnection;
import net.strangled.maladan.serializables.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LocalLoginDataStore {

    static User getLocalUser() throws Exception {
        Connection conn = GetSQLConnection.getConn();

        if (conn != null && dataSaved()) {
            String sql = "SELECT user FROM username";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            rs.next();
            User user = (User) Main.reconstructSerializedObject(rs.getBytes("user"));
            conn.close();
            return user;

        }
        return null;
    }

    static void saveLocaluser(User user) throws Exception {
        Connection conn = GetSQLConnection.getConn();

        if (conn != null && !dataSaved()) {
            String sql = "INSERT INTO username (user) VALUES (?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setBytes(1, Main.serializeObject(user));
            ps.execute();
            conn.close();
        }
    }

    static boolean dataSaved() throws Exception {
        Connection conn = GetSQLConnection.getConn();

        if (conn != null) {
            String sql = "SELECT user FROM username";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            boolean exists = rs.next();
            conn.close();
            return exists;

        }
        return false;
    }
}
