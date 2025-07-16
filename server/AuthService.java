package server;

import java.security.MessageDigest;
import java.sql.*;

public class AuthService {

    public static boolean userExists(String username) throws Exception {
        Connection conn = DBUtil.getConnection();
        PreparedStatement stmt = conn.prepareStatement("select id from users where username = ?");
        stmt.setString(1, username);
        ResultSet rs = stmt.executeQuery();
        return rs.next();
    }

    public static boolean login(String username, String password) throws Exception {

        Connection conn = DBUtil.getConnection();
        String sql = "select password_hash from users where username = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, username);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            String storedHash = rs.getString("password_hash");
            return storedHash.equals(hash(password));
        }
        return false;
    }

    public static boolean signup(String username, String password) throws Exception {

        if (userExists(username))
            return false;

        Connection conn = DBUtil.getConnection();
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, username);
        stmt.setString(2, hash(password));
        stmt.executeUpdate();
        return true;
    }

    public static String hash(String password) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(password.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
