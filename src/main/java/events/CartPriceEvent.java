package events;

import essentials.Config;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.entities.Message;

public class CartPriceEvent extends ListenerAdapter {

    private final Config config = new Config();
    private final String CURRENCY_EMOJI = config.getCurrencyEmojiId();
    private final String CURRENCY_TOTAL_EMOJI = config.getTotalCurrencyEmojiId();
    private final String CART_CHANNEL_ID = config.getCartChannelId();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        /*
        the purpose of this code is to calculate the total cost of everything she sends into the channel
        that was designed for her to send stuff she wishes to get
         */
        String message = event.getMessage().getContentRaw();

        if (message.equalsIgnoreCase("!cartprice")) {
            if (event.getChannel().getId().equals(CART_CHANNEL_ID)) {
                event.getChannel().asTextChannel().getHistory().retrievePast(100).queue(messages -> {
                    int totalPrice = 0;

                    for (Message msg : messages) {
                        String content = msg.getContentRaw();

                        if (content.contains(CURRENCY_EMOJI)) {
                            String[] parts = content.split(CURRENCY_EMOJI + "\\s*");
                            for (String part : parts) {
                                try {
                                    int price = Integer.parseInt(part.trim());
                                    totalPrice += price;
                                } catch (NumberFormatException ex) {
                                }
                            }
                        }
                    }
                    try {
                        event.getChannel().sendTyping().queue();
                        Thread.sleep(1500);
                        event.getChannel().sendMessage("Total Cart Price: " + CURRENCY_TOTAL_EMOJI + " " + totalPrice).queue();
                    } catch (InterruptedException ex) {
                    }
                });
            } else {
                try {
                    event.getChannel().sendTyping().queue();
                    Thread.sleep(1500);
                    event.getChannel().sendMessage("There is no cart to calculate here.").queue();
                } catch (InterruptedException ex) {
                }
            }
        }
    }
}
