package k6v;

import java.util.ArrayList;
import java.util.Collection;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class BasicModule extends ListenerAdapter 
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
            MessageChannel channel = event.getChannel();
            channel.sendMessage("Pong!").queue(); // Important to call .queue() on the RestAction returned by sendMessage(...)
        }
        if (content.equals("!updateSlash"))
        {
            event.getJDA().updateCommands().addCommands(getGlobalCommands()).queue();;
        }
        if (content.equals("!whoAmI"))
        {
            event.getChannel().sendMessage("you are " + event.getAuthor().getAsMention() + ", your id is " + event.getAuthor().getId() + "\nthe server is " + event.getGuild().getName() + " (" + event.getGuild().getId() + ")").queue();
        }
        if (content.equals("!link"))
        {
            if (! App.isUserOp(event.getAuthor())) return;
            event.getChannel().sendMessage("https://discord.com/api/oauth2/authorize?client_id=1200828506750332948&permissions=402182634816&scope=bot").queue();
        }
    }

    protected Collection<CommandData> getGlobalCommands()
    {
        Collection<CommandData> result = new ArrayList<CommandData>();
        result.add(Commands.slash("joinvoice", "join the vocal you are in"));
        return result;
    }
}
