package lilybot.essentials;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {

    private Properties properties = new Properties();

    public Config() {
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            properties.load(fis);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public String getBotToken() {
        return properties.getProperty("bot.token");
    }

    public String getMyUserId() {
        return properties.getProperty("my.user.id");
    }

    public String getHerUserId() {
        return properties.getProperty("her.user.id");
    }

    public String getGiftChannelId() {
        return properties.getProperty("gift.channel.id");
    }

    public String getDBUrl() {
        return properties.getProperty("db.url");
    }

    public String getDBUser() {
        return properties.getProperty("db.user");
    }

    public String getDBPassword() {
        return properties.getProperty("db.password");
    }
}
