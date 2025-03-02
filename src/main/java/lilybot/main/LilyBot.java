package lilybot.main;

import java.util.HashMap;
import java.util.Map;
import lilybot.databases.DatabaseManager;
import lilybot.essentials.Config;
import lilybot.tasks.GiftCardScheduler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class LilyBot extends ListenerAdapter {

    public static final Config config = new Config();

    public final String MY_USER_ID = config.getMyUserId();
    private final Map<String, String> dmConversations = new HashMap<>();
    public final String HER_USER_ID = config.getHerUserId();
    public final String GIFT_CHANNEL_ID = config.getGiftChannelId();

    public static void main(String[] args) throws Exception {
        JDABuilder builder = JDABuilder.createDefault(config.getBotToken())
                .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .enableIntents(GatewayIntent.DIRECT_MESSAGES)
                .enableIntents(GatewayIntent.DIRECT_MESSAGE_TYPING);

        builder.addEventListeners(new LilyBot());

        JDA jda = builder.build();
        jda.awaitReady();

        new GiftCardScheduler(jda);

        jda.updateCommands().addCommands(Commands.slash("change", "Change your monthly item")
                .addOption(OptionType.STRING, "new_item", "What do you want to change to?", true)).queue();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        String channelName = event.getChannel().getName();

        if (event.getName().equals("change")) {
            String newItem = event.getOption("new_item").getAsString();
            if (event.isFromGuild()) {
                if (channelName.equalsIgnoreCase("item-change-🤍")) {
                    event.reply("You have changed your item for the next month to " + newItem).queue();

                    if (!event.getUser().getId().equals(MY_USER_ID)) {

                        String message = "Monthly item successfully configured to: " + newItem;
                        event.getJDA().retrieveUserById(MY_USER_ID).queue(user -> {
                            user.openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(message).queue());
                        });
                        dmConversations.put(event.getUser().getId(), "dm");
                    }
                } else {
                    event.reply("You can only change to the item you want in the #item-change-🤍 channel").queue();
                }
            } else {
                event.reply("You have changed your item for the next month to " + newItem).queue();

                if (!event.getUser().getId().equals(MY_USER_ID)) {

                    String message = "Monthly item successfully configured to: " + newItem;
                    event.getJDA().retrieveUserById(MY_USER_ID).queue(user -> {
                        user.openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(message).queue());
                    });
                    dmConversations.put(event.getUser().getId(), "dm");
                }
            }
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        // ignore bot messages
        if (event.getAuthor().isBot()) {
            return;
        }

        String channelName = event.getChannel().getName();
        String message = event.getMessage().getContentRaw().toLowerCase();
        String userId = event.getAuthor().getId();
        String[] args = message.split(" ");

        /*
        validate if the admin commands are from my account first
        then execute 
         */
        if (event.getAuthor().getId().equals(MY_USER_ID)) {

            if (args[0].equalsIgnoreCase("!dm")) {
                if (args.length > 2) {
                    String IdUser = args[1];
                    String content = message.substring(args[0].length() + args[1].length() + 2);
                    sendDM(event, IdUser, content);
                    return;
                }
            } else if (args[0].equalsIgnoreCase("!sendchannel")) {
                if (args.length > 2) {
                    String channelId = args[1];
                    String content = message.substring(args[0].length() + args[1].length() + 2);
                    sendToChannel(event, channelId, content);
                    return;
                }
            }

            if (args[0].equalsIgnoreCase("!store") && args.length == 3) {
                String month = args[1];
                String characters = args[2];

                DatabaseManager manager = new DatabaseManager();
                manager.storeGiftCard(month, characters);

                event.getChannel().sendMessage("The gift card for the month "
                        + "" + month.substring(0, 1).toUpperCase() + month.substring(1) + " "
                        + "has been stored.").queue();
                return;
            } else if (args[0].equalsIgnoreCase("!retrieve") && args.length == 2) {
                String month = args[1];

                DatabaseManager manager = new DatabaseManager();

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
                return;
            } else if (args[0].equalsIgnoreCase("!delete") && args.length == 2) {
                String month = args[1];

                DatabaseManager manager = new DatabaseManager();
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
                return;
            } else if (args[0].equalsIgnoreCase("!sendgift") && args.length == 2) {
                String month = args[1];

                DatabaseManager manager = new DatabaseManager();

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
                return;
            } else if (message.equalsIgnoreCase("!retrieveall")) {
                DatabaseManager manager = new DatabaseManager();

                String giftCards = manager.retrieveAll();

                if (!giftCards.isEmpty()) {
                    event.getChannel().sendMessage("Here are all the gift cards you have stored:"
                            + "\n\n" + giftCards).queue();
                } else {
                    event.getChannel().sendMessage("You haven't stored any gift cards.").queue();
                }
                return;
            }
        }
        try {
            if (message.equals("hey") || message.equals("hello") || message.equals("hi")) {
                if (event.isFromGuild()) {
                    return;
                } else {
                    event.getChannel().sendTyping().queue();
                    Thread.sleep(1500);

                    event.getChannel().sendMessage("Heyyy!").queue();
                }
            } else {
                if (event.isFromGuild()) {
                    return;
                } else {
                    event.getChannel().sendTyping().queue();
                    Thread.sleep(2500);

                    event.getChannel().sendMessage("Hmm... I don't understand that yet. But I'm currently working on"
                            + " cool stuff so I might understand that soon. Come back later! 😊").queue();
                }
            }
        } catch (InterruptedException ex) {
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    /*
    
    FOR FUTURE USE
    
    private void sendDirectMessage(JDA jda, String question, String answer) {
        // fetch the user to send the DM to
        jda.retrieveUserById(MY_USER_ID).queue(user -> {
            user.openPrivateChannel().queue(privateChannel -> {
                privateChannel.sendMessage("**Question: **" + question + "\n**Answer: **" + answer).queue(
                        success -> System.out.println("Message sent to DMs successfully!"),
                        failure -> System.out.println("Failed to send message to DMs: " + failure.getMessage())
                );
            }, throwable -> {
                System.out.println("Failed to open private channel: " + throwable.getMessage());
            });
        }, throwable -> {
            System.out.println("Failed to retrieve user: " + throwable.getMessage());
        });
    }
     */
    private void sendDM(MessageReceivedEvent event, String userId, String message) {
        /*
        this sends a direct message to a user
        takes in the user ID of that user
         */
        event.getJDA().retrieveUserById(userId).queue(user -> {
            if (user != null) {
                user.openPrivateChannel().queue(privateChannel -> {
                    privateChannel.sendMessage(message).queue();
                    event.getChannel().sendMessage("Message sent to <@" + userId + ">!").queue();
                });
            } else {
                event.getChannel().sendMessage("User not found!").queue();
            }
        }, throwable -> {
            event.getChannel().sendMessage("Failed to retrieve user.").queue();
        });
    }

    private void sendToChannel(MessageReceivedEvent event, String channelId, String chanMessage) {
        /*
        this sends a message to a server channel
        takes in the channel ID of that server in order to send
         */
        if (event.getJDA().getTextChannelById(channelId) != null) {
            event.getJDA().getTextChannelById(channelId).sendMessage(chanMessage).queue(success
                    -> event.getChannel().sendMessage("Message sent to the channel.").queue(),
                    failure
                    -> event.getChannel().sendMessage("Failed to send message.").queue());
        } else {
            event.getChannel().sendMessage("Invalid channel ID.").queue();
        }
    }
}
