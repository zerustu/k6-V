package k6v;

import java.util.ArrayList;
import java.util.Collection;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.Permission;

public class BasicCommands {

    /**
     * Check if a user is in the Op list defined in the option.json file
     * @param user user to test
     * @return true if the user is in the Op list, false otherwise
     */
    public static boolean isUserOp(User user)
    {
        for (String op : App.ops) {
            if (op.equals(user.getId()))
            {
                return true;
            }
        }
        return false;
    }


    public static void ping(MessageReceivedEvent event)
    {
        MessageChannel channel = event.getChannel();
        JDA k6v = event.getJDA();
        channel.sendMessage("pong !").queue();
        long pingRest = k6v.getRestPing().complete();
        long pingHeart = k6v.getGatewayPing();
        channel.sendMessageFormat("Rest action ping %d ms \n HeartBeat ping %d ms", pingRest, pingHeart).queue();
    }

    public static void updateSlash(MessageReceivedEvent event)
    {
        MessageChannel channel = event.getChannel();
        event.getJDA().updateCommands().addCommands(getGlobalCommands())
            .queue(
                (v) -> channel.sendMessage("Les commands slash ont été mis à jour").queue(), 
                (v) -> channel.sendMessage("il y a eu une erreur dans la mise a jour des commands").queue()
            );
    }

    public static void whoAmI(MessageReceivedEvent event)
    {
        event.getChannel().sendMessage(
            "you are " + event.getAuthor().getAsMention() + 
            ", your id is " + event.getAuthor().getId() + 
            "\nthe server is " + event.getGuild().getName() + 
            " (" + event.getGuild().getId() + ")"
        ).queue();
    }

    public static void sendlink(MessageReceivedEvent event)
    {
        MessageChannel channel = event.getChannel();
        if (! BasicCommands.isUserOp(event.getAuthor())) 
        {
            channel.sendMessage("Tu n'est pas authorisé à utilisé cette commande.").queue();
            return;
        }
        event.getChannel().sendMessage(event.getJDA().getInviteUrl(Permission.getPermissions(402182634816l))).queue();
    }

    protected static Collection<CommandData> getGlobalCommands()
    {
        Collection<CommandData> result = new ArrayList<CommandData>();
        result.add(Commands.slash("joinvoice", "join the vocal you are in"));
        return result;
    }
}
