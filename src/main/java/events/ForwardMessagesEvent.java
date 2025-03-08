package events;

import essentials.Config;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ForwardMessagesEvent extends ListenerAdapter {

    private final Config config = new Config();
    private final String MY_USER_ID = config.getMyUserId();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        String message = event.getMessage().getContentRaw().toLowerCase();
        String[] args = message.split(" ");

        if (event.getAuthor().getId().equals(MY_USER_ID)) {
            if (args[0].equalsIgnoreCase("!dm")) {

                if (args.length > 2) {
                    String IdUser = args[1];
                    String content = message.substring(args[0].length() + args[1].length() + 2);
                    sendDM(event, IdUser, content);
                }
            } else if (args[0].equalsIgnoreCase("!sendchannel")) {

                if (args.length > 2) {
                    String channelId = args[1];
                    String content = message.substring(args[0].length() + args[1].length() + 2);
                    sendToChannel(event, channelId, content);
                }
            }
        }
    }

    private void sendDM(MessageReceivedEvent event, String userId, String message) {
        /*
        this sends a direct message to a user
        takes in the user ID of that user
         */
        event.getJDA().retrieveUserById(userId).queue(user
                -> user.openPrivateChannel().queue(privateChannel -> {
                    privateChannel.sendMessage(message).queue();
                    event.getChannel().sendMessage("Message sent to <@" + userId + ">!").queue();
                }, throwable
                        -> event.getChannel().sendMessage("Failed to retrieve user.").queue()
                ), throwable -> event.getChannel().sendMessage("Failed to retrieve user.").queue());
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
