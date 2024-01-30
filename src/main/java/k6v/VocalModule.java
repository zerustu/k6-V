package k6v;

import org.jetbrains.annotations.Nullable;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

public class VocalModule extends ListenerAdapter
{

    static @Nullable AudioManager audioManager;

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if (!App.isUserOp(event.getAuthor())) return;
        if (event.getAuthor().isBot()) return;
        // We don't want to respond to other bot accounts, including ourself
        Message message = event.getMessage();
        String content = message.getContentRaw(); 
        // getContentRaw() is an atomic getter
        // getContentDisplay() is a lazy getter which modifies the content for e.g. console view (strip discord formatting)
        if (content.equals("!joinvoice"))
        {
            if (audioManager != null)
            {
                event.getChannel().sendMessage("The bot is already connected somewhere. try again later!").queue();
                return;
            }
            Member user = event.getMember();

            AudioChannelUnion myChannel = user.getVoiceState().getChannel();

            if (myChannel == null)
            {
                event.getChannel().sendMessage("you are not in a voice channel right now").queue();
            }

            Guild guild = event.getGuild();
            audioManager = guild.getAudioManager();
            SendAudioModule myAudioModule = new SendAudioModule(App.sttDecoder);
            audioManager.setReceivingHandler(myAudioModule);
            audioManager.openAudioConnection(myChannel);
        }
        if (content.equals("!leavevoice"))
        {
            if (audioManager == null)
            {
                event.getChannel().sendMessage("The bot is not connected. try again later!").queue();
                return;
            }

            audioManager.closeAudioConnection();
            audioManager = null;
        }
        if (content.equals("!close"))
        {
            App.sttDecoder.run = false;
            event.getJDA().shutdown();
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!App.isUserOp(event.getMember().getUser())) 
        {
            event.reply("you are note allowed to do that!").queue();
            return;
        }
        if (!event.isFromGuild()) return;
        if (event.getName().equals("joinvoice"))
        {
            if (audioManager != null)
            {
                event.getChannel().sendMessage("The bot is already connected somewhere. try again later!").queue();
                return;
            }
            event.deferReply().queue();

            Member user = event.getMember();

            AudioChannelUnion myChannel = user.getVoiceState().getChannel();

            if (myChannel == null)
            {
                event.getHook().sendMessage("you are not in a voice channel right now").queue();
            }

            Guild guild = event.getGuild();
            AudioManager audioManager = guild.getAudioManager();
                SendAudioModule myAudioModule = new SendAudioModule(App.sttDecoder);
                audioManager.setReceivingHandler(myAudioModule);
                audioManager.openAudioConnection(myChannel);
        }
    }
}
