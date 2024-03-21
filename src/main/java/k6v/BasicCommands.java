package k6v;

import java.util.ArrayList;
import java.util.Collection;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
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
        return App.mem.isOp(user.getIdLong());
    }


    public static void ping(MessageChannel channel)
    {
        JDA k6v = channel.getJDA();
        channel.sendMessage("pong !").queue();
        long pingRest = k6v.getRestPing().complete();
        long pingHeart = k6v.getGatewayPing();
        channel.sendMessageFormat("Rest action ping %d ms \n HeartBeat ping %d ms", pingRest, pingHeart).queue();
    }

    public static void ping(SlashCommandInteractionEvent event)
    {
        JDA k6v = event.getJDA();
        event.reply("pong !").queue();
        long pingRest = k6v.getRestPing().complete();
        long pingHeart = k6v.getGatewayPing();
        event.getHook().editOriginalFormat("pong !\nRest action ping %d ms \n HeartBeat ping %d ms", pingRest, pingHeart).queue();
    }

    public static void updateSlash(MessageChannel channel)
    {
        channel.getJDA().updateCommands().addCommands(getGlobalCommands())
            .queue(
                (v) -> channel.sendMessage("Les commands slash ont été mis à jour").queue(), 
                (v) -> channel.sendMessage("il y a eu une erreur dans la mise a jour des commands").queue()
            );
    }

    public static void updateSlash(SlashCommandInteractionEvent event)
    {
        event.deferReply(true);
        event.getJDA().updateCommands().addCommands(getGlobalCommands())
            .queue(
                (v) -> event.getHook().sendMessage("Les commands slash ont été mis à jour").queue(), 
                (v) -> event.getHook().sendMessage("il y a eu une erreur dans la mise a jour des commands").queue()
            );
    }

    public static void whoAmI(MessageChannel channel, User user, Guild guild)
    {
        channel.sendMessage(
            "you are " + user.getAsMention() + 
            ", your id is " + user.getId() + 
            "\nthe server is " + guild.getName() + 
            " (" + guild.getId() + ")"
        ).queue();
    }

    public static void whoAmI(SlashCommandInteractionEvent event, User user, Guild guild)
    {
        event.reply(
            "you are " + user.getAsMention() + 
            ", your id is " + user.getId() + 
            "\nthe server is " + guild.getName() + 
            " (" + guild.getId() + ")"
        ).queue();
    }

    public static void sendlink(MessageChannel channel)
    {
        channel.sendMessage(channel.getJDA().getInviteUrl(Permission.getPermissions(402182634816l))).queue();
    }

    public static void sendlink(SlashCommandInteractionEvent event)
    {
        event.reply(event.getJDA().getInviteUrl(Permission.getPermissions(402182634816l))).queue();
    }

    protected static Collection<CommandData> getGlobalCommands()
    {
        Collection<CommandData> result = new ArrayList<CommandData>();
        result.add(Commands.slash("joinvoice", "join the vocal you are in"));
        result.add(Commands.slash("ping", "get ping information"));
        result.add(Commands.slash("link", "get the link to invite the bot (need to be op)"));
        result.add(Commands.slash("whoami", "get information about your Id and the guild Id"));
        return result;
    }
}
