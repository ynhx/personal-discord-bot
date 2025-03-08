package events;

import essentials.Config;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ClearCartEvent extends ListenerAdapter {

    private final Config config = new Config();
    private final String CART_CHANNEL_ID = config.getCartChannelId();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw();
        String channelId = event.getChannel().getId();

        if (message.equalsIgnoreCase("!clearcart")) {
            if (channelId.equalsIgnoreCase(CART_CHANNEL_ID)) {
                event.getChannel().getHistory().retrievePast(100).queue(messages -> {
                    for (Message msg : messages) {
                        if (!msg.getContentRaw().equalsIgnoreCase("!clearcart")) {
                            msg.delete().queue();
                        }
                    }
                    try {
                        event.getChannel().sendTyping().queue();
                        Thread.sleep(1500);
                        event.getChannel().sendMessage("Cart cleared!").queue();
                    } catch (InterruptedException ex) {
                    }
                });
            } else {
                event.getChannel().sendTyping().queue();
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException ex) {
                }
                event.getChannel().sendMessage("There's no cart to clear here.").queue();
            }
        }
    }
}
