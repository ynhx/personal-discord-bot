package databases;

import java.sql.*;
import essentials.Config;

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

        try (Connection connection = connect();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, month);
            ps.setString(2, characters);
            ps.setString(3, characters);

            int rows = ps.executeUpdate();
            System.out.println("Added " + rows + " gift card.");
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public String retrieveGiftCard(String month) {
        String sql = "SELECT characters FROM gift_card WHERE month = ?";
        String result = "";

        try (Connection connection = connect();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, month);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String characters = rs.getString("characters");

                result = characters;
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return result;
    }

    public int deleteGiftCard(String month) {
        String sql = "DELETE FROM gift_card WHERE month = ?";
        int rows = 0;

        try (Connection connection = connect();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, month);

            rows = ps.executeUpdate();
            System.out.println("Deleted " + rows + " gift card.");
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return rows;
    }

    public String retrieveAll() {
        String sql = "SELECT month, characters FROM gift_card";
        StringBuilder giftCards = new StringBuilder();
        int rows = 0;

        try (Connection connection = connect();
                PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();) {

            while (rs.next()) {
                String month = rs.getString("month");
                String characters = rs.getString("characters");

                giftCards.append("**").append(month.substring(0, 1).toUpperCase()).append(month.substring(1))
                        .append(": **")
                        .append(characters.toUpperCase())
                        .append("\n");

                rows++;
            }
            System.out.println("Retrieved " + rows + " monthly gift cards.");
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return giftCards.toString();
    }

    public void storeRegularGiftCard(String code) {
        String sql = "INSERT INTO regular_gift_cards (card_code) VALUES (?)";

        try (Connection connection = connect();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, code);

            int rows = ps.executeUpdate();
            System.out.println("Added " + rows + " gift card.");
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public String getRegularGiftCard() {
        String sqlSelect = "SELECT card_code FROM regular_gift_cards LIMIT 1";
        String sqlDelete = "DELETE FROM regular_gift_cards WHERE card_code = ?";
        String giftCard;

        try (Connection connection = connect()) {
            connection.setAutoCommit(false);

            try (PreparedStatement selectPs = connection.prepareStatement(sqlSelect);
                    ResultSet rs = selectPs.executeQuery()) {

                if (rs.next()) {
                    giftCard = rs.getString("card_code");

                    // delete the gift card from the database after being fetched
                    try (PreparedStatement deletePs = connection.prepareStatement(sqlDelete)) {
                        deletePs.setString(1, giftCard);
                        deletePs.executeUpdate();
                    }

                    connection.commit();
                    return giftCard;
                }
            } catch (SQLException ex) {
                connection.rollback();
                System.out.println(ex.getMessage());
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return "";
    }

    public String retrieveAllRegular() {
        String sql = "SELECT card_code FROM regular_gift_cards";
        StringBuilder giftCards = new StringBuilder();
        int count = 1;

        try (Connection connection = connect();
                PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            int rows = 0;

            while (rs.next()) {
                String code = rs.getString("card_code");

                giftCards.append(count).append(". ").append(code.toUpperCase()).append("\n");

                count++;
                rows++;
            }
            System.out.println("Retrieved " + rows + " regular gift cards.");
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return giftCards.toString();
    }
}
