package k6v;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandHandler extends ListenerAdapter 
{
    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if (event.getAuthor().isBot()) return;
        // We don't want to respond to other bot accounts, including ourself
        Message message = event.getMessage();
        String content = message.getContentRaw(); 
        // getContentRaw() is an atomic getter
        // getContentDisplay() is a lazy getter which modifies the content for e.g. console view (strip discord formatting)
        if (content.equals("!ping"))
        {
            BasicCommands.ping(event);
        }
        if (content.equals("!updateSlash"))
        {
            BasicCommands.updateSlash(event);
        }
        if (content.equals("!whoAmI"))
        {
            BasicCommands.whoAmI(event);
        }
        if (content.equals("!link"))
        {
            BasicCommands.sendlink(event);
        }
    }
}
