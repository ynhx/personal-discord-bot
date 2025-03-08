package commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class HelpCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("help")) {
            event.reply("What do you need help with?").addActionRow(
                    Button.primary("help_redeem", "How to redeem"),
                    Button.primary("help_change", "How to change monthly item"),
                    Button.primary("help_monthly_exp", "How monthly works"),
                    Button.primary("commands_list", "View available commands")).queue();
        }
    }
}
