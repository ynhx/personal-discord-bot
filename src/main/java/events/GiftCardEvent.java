package events;

import databases.DatabaseManager;
import essentials.Config;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GiftCardEvent extends ListenerAdapter {

    private final Config config = new Config();
    private final String MY_USER_ID = config.getMyUserId();
    private final String HER_USER_ID = config.getHerUserId();
    private final String GIFT_CHANNEL_ID = config.getGiftChannelId();
    private final DatabaseManager manager = new DatabaseManager();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw().toLowerCase();
        String[] args = message.split(" ");

        if (event.getAuthor().isBot()) {
            return;
        }

        // only execute when it's from my account
        if (event.getAuthor().getId().equals(MY_USER_ID)) {
            if (args[0].equals("!store") && args.length == 2) {
                String code = args[1];

                manager.storeRegularGiftCard(code);

                event.getChannel().sendMessage("You have successfully stored a regular gift card.").queue();

            } else if (args[0].equalsIgnoreCase("!sendgift") && args.length == 2) {
                String month = args[1];
                String giftCard = manager.retrieveGiftCard(month);

                if (!giftCard.isEmpty()) {
                    String gift = "Heyyy <@" + HER_USER_ID + ">"
                            + "\n\n"
                            + "Here's your gift card for "
                            + month.substring(0, 1).toUpperCase() + month.substring(1)
                            + ": **" + giftCard.toUpperCase() + "**.";

                    sendToChannel(event, GIFT_CHANNEL_ID, gift);
                } else {
                    event.getChannel().sendMessage("You haven't stored any gift card for "
                            + month.substring(0, 1).toUpperCase() + month.substring(1)).queue();
                }

            } else if (args[0].equalsIgnoreCase("!store") && args.length == 3) {
                String month = args[1];
                String characters = args[2];

                manager.storeGiftCard(month, characters);

                event.getChannel().sendMessage("The gift card for the month "
                        + "" + month.substring(0, 1).toUpperCase() + month.substring(1) + " "
                        + "has been stored.").queue();

            } else if (args[0].equalsIgnoreCase("!retrieve") && args.length == 2) {
                String month = args[1];
                String characters = manager.retrieveGiftCard(month);

                if (!characters.isEmpty()) {
                    event.getChannel().sendMessage("Month: "
                            + month.substring(0, 1).toUpperCase() + month.substring(1)
                            + "\n\n"
                            + "Gift card: " + characters).queue();
                } else {
                    event.getChannel().sendMessage("You haven't stored any gift card for "
                            + month.substring(0, 1).toUpperCase() + month.substring(1)).queue();
                }

            } else if (args[0].equalsIgnoreCase("!delete") && args.length == 2) {
                String month = args[1];
                int rows = manager.deleteGiftCard(month);

                if (rows == 1) {
                    event.getChannel().sendMessage("You have deleted a gift card for month: **"
                            + month.substring(0, 1).toUpperCase() + month.substring(1) + "**.").queue();
                } else if (rows >= 1) {
                    event.getChannel().sendMessage("You have deleted " + rows + " gift cards.").queue();
                } else {
                    event.getChannel().sendMessage("You haven't stored any **"
                            + month.substring(0, 1).toUpperCase() + month.substring(1) + "** gift card.").queue();
                }

            } else if (message.equalsIgnoreCase("!retrieveall")) {
                String giftCards = manager.retrieveAll();

                if (!giftCards.isEmpty()) {
                    event.getChannel().sendMessage("Here are all the gift cards you have stored:"
                            + "\n\n" + giftCards).queue();
                } else {
                    event.getChannel().sendMessage("You haven't stored any gift cards.").queue();
                }
                
            } else if (message.equalsIgnoreCase("!retrieveallreg")) {
                String giftCards = manager.retrieveAllRegular();

                if (!giftCards.isEmpty()) {
                    event.getChannel().sendMessage("Here are all the regular gift cards you have stored:"
                            + "\n\n" + giftCards).queue();
                } else {
                    event.getChannel().sendMessage("You haven't stored any regular gift cards.").queue();
                }
            }
        }
    }

    private void sendToChannel(MessageReceivedEvent event, String channelId, String chanMessage) {
        /*
        this sends a message to a server channel
        takes in the channel ID of that server in order to send
         */
        TextChannel channel = event.getJDA().getTextChannelById(channelId);

        if (channel != null) {
            channel.sendMessage(chanMessage).queue(success
                    -> event.getChannel().sendMessage("Message sent to the channel.").queue(),
                    failure
                    -> event.getChannel().sendMessage("Failed to send message.").queue());
        } else {
            event.getChannel().sendMessage("Invalid channel ID.").queue();
        }
    }
}
