package lilybot.databases;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import lilybot.essentials.Config;

public class DatabaseManager {

    private static final Config config = new Config();

    private static final String URL = config.getDBUrl();
    private static final String USERNAME = config.getDBUser();
    private static final String PASSWORD = config.getDBPassword();

    public static Connection connect() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return connection;
    }

    public void storeGiftCard(String month, String characters) {
        String sql = "INSERT INTO gift_card(month, characters) VALUES(?, ?)"
                + " ON DUPLICATE KEY UPDATE characters = ?";

        try {
            Connection connection = connect();

            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, month);
            ps.setString(2, characters);
            ps.setString(3, characters);
            int rows = ps.executeUpdate();
            System.out.println("Added " + rows + " gift card.");
            ps.close();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public String retrieveGiftCard(String month) {
        String sql = "SELECT characters FROM gift_card WHERE month = ?";
        String result = "";

        try {
            Connection connection = connect();

            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, month);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String characters = rs.getString("characters");

                result = characters;
            }
            rs.close();
            ps.close();

        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return result;
    }

    public int deleteGiftCard(String month) {
        String sql = "DELETE FROM gift_card WHERE month = ?";
        int rows = 0;

        try {
            Connection connection = connect();

            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, month);
            rows = ps.executeUpdate();
            System.out.println("Deleted " + rows + " gift card.");
            ps.close();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return rows;
    }

    public String retrieveAll() {
        String sql = "SELECT month, characters FROM gift_card";
        String giftCards = "";

        try {
            Connection connection = connect();

            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            System.out.println("Retrieved " + rs.getFetchSize() + " gift cards.");
            while (rs.next()) {
                String month = rs.getString("month");
                String characters = rs.getString("characters");

                giftCards += "**" + month.substring(0, 1).toUpperCase() + month.substring(1) + ": **"
                        + characters.toUpperCase()
                        + "\n\n";
            }
            rs.close();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return giftCards;
    }
}
