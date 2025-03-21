package events;

import databases.DatesDBManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class DeleteDateEvent extends ListenerAdapter {

    private final DatesDBManager dbManager = new DatesDBManager();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw().toLowerCase();
        String[] args = message.split(" ");

        if (args[0].equals("!deletedate") && args.length >= 2) {
            String description = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));

            dbManager.deleteDate(description);

            try {
                event.getChannel().sendTyping().queue();
                Thread.sleep(1500);
                event.getChannel().sendMessage("Okay, I have deleted the date for "
                        + description.substring(0, 1).toUpperCase()
                        + description.substring(1)
                        + ".").queue();
            } catch (InterruptedException ex) {
            }
        }
    }
}
