package events;

import essentials.Config;
import handlers.MessagesHandler;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ForwardMessagesEvent extends ListenerAdapter {

    private final MessagesHandler messagesHandler = new MessagesHandler();
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
                    messagesHandler.sendDM(event, IdUser, content);
                }
            } else if (args[0].equalsIgnoreCase("!sendchannel")) {

                if (args.length > 2) {
                    String channelId = args[1];
                    String content = message.substring(args[0].length() + args[1].length() + 2);
                    messagesHandler.sendToChannel(event, channelId, content);
                }
            }
        }
    }
}
