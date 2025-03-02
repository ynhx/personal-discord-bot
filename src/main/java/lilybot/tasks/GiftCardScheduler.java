package lilybot.tasks;

import java.time.*;
import java.util.concurrent.*;
import lilybot.databases.DatabaseManager;
import lilybot.essentials.Config;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class GiftCardScheduler {

    private final Config config = new Config();

    private final JDA jda;
    private final String userId = config.getHerUserId();
    private final String channelId = config.getGiftChannelId();
    private final DatabaseManager databaseManager;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public GiftCardScheduler(JDA jda) {
        this.jda = jda;
        this.databaseManager = new DatabaseManager();
        scheduleGiftCardSending();
    }

    private void scheduleGiftCardSending() {
        long initialDelay = getInitialDelay();
        long period = Duration.ofDays(30).toMillis();

        scheduler.scheduleAtFixedRate(this::sendGiftCard, initialDelay, period, TimeUnit.MILLISECONDS);
    }

    private long getInitialDelay() {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime nextRun = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);

        if (now.isAfter(nextRun)) {
            nextRun = nextRun.plusMonths(1);
        }
        return Duration.between(now, nextRun).toMillis();
    }

    private void sendGiftCard() {
        try {
            LocalDate currentDate = LocalDate.now(ZoneOffset.UTC);
            String month = currentDate.getMonth().name().toLowerCase();

            String giftCard = databaseManager.retrieveGiftCard(month);
            if (giftCard != null && !giftCard.isEmpty()) {
                String message = "Heyyy <@" + userId + ">\n\n"
                        + "Here's your gift card for " + month.substring(0, 1).toUpperCase() + month.substring(1) + ": **" + giftCard.toUpperCase() + "**.";

                TextChannel channel = jda.getTextChannelById(channelId);
                if (channel != null) {
                    channel.sendMessage(message).queue();
                }
            } else {
                System.out.println("There's an issue with the gift card.");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}
