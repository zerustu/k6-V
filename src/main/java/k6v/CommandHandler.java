package k6v;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandHandler extends ListenerAdapter 
{
    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        User user = event.getAuthor();
        if (user.isBot()) return;
        // We don't want to respond to other bot accounts, including ourself
        Message message = event.getMessage();
        String content = message.getContentRaw(); 
        MessageChannel channel = event.getChannel();
        // getContentRaw() is an atomic getter
        // getContentDisplay() is a lazy getter which modifies the content for e.g. console view (strip discord formatting)
        if (content.equals("!ping"))
        {
            BasicCommands.ping(channel);
        }
        if (content.equals("!updateSlash"))
        {
            if (! BasicCommands.isUserOp(user)) 
            {
                channel.sendMessage("Tu n'est pas authorisé à utilisé cette commande.").queue();
                return;
            }
            BasicCommands.updateSlash(channel);
        }
        if (content.equals("!whoAmI"))
        {
            BasicCommands.whoAmI(channel, user, event.getGuild());
        }
        if (content.equals("!link"))
        {
            if (! BasicCommands.isUserOp(user)) 
            {
                channel.sendMessage("Tu n'est pas authorisé à utilisé cette commande.").queue();
                return;
            }
            BasicCommands.sendlink(channel);
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        User user = event.getUser();
        if (user.isBot()) return;
        // We don't want to respond to other bot accounts, including ourself
        String name = event.getName();
        if (name.equals("ping"))
        {
            BasicCommands.ping(event);
        }
        if (name.equals("updateslash"))
        {
            if (! BasicCommands.isUserOp(user)) 
            {
                event.reply("Tu n'est pas authorisé à utilisé cette commande.").queue();
                return;
            }
            BasicCommands.updateSlash(event);
        }
        if (name.equals("whoami"))
        {
            BasicCommands.whoAmI(event, user, event.getGuild());
        }
        if (name.equals("link"))
        {
            if (! BasicCommands.isUserOp(user)) 
            {
                event.reply("Tu n'est pas authorisé à utilisé cette commande.").queue();
                return;
            }
            BasicCommands.sendlink(event);
        }
    }
}
