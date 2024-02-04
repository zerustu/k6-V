package k6v;

import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;

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
    static @Nullable SendModule mysendModule;

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if (event.getAuthor().isBot()) return;
        // We don't want to respond to other bot accounts, including ourself
        Message message = event.getMessage();
        String content = message.getContentRaw(); 

        
        if (content.startsWith("!say "))
        {
            if (audioManager == null)
            {
                event.getChannel().sendMessage("The bot is not connected. try again later!").queue();
                return;
            }
            Thread saying = new Thread(() -> mysendModule.respond(content.substring(5)));
            saying.start();
        }

        if (content.equals("!bonjour")) {
            if (audioManager == null)
            {
                event.getChannel().sendMessage("The bot is not connected. try again later!").queue();
                return;
            }
            mysendModule.respond("bonjour " + event.getAuthor().getName() + ".");
        }


        if (!BasicCommands.isUserOp(event.getAuthor())) return;
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

            GuildVoiceState GVS = user.getVoiceState();

            if (GVS == null)
            {
                event.getChannel().sendMessage("you are not in a voice channel right now").queue();
                return;
            }
            AudioChannelUnion myChannel = GVS.getChannel();

            if (myChannel == null)
            {
                event.getChannel().sendMessage("you are not in a voice channel right now").queue();
                return;
            }

            Guild guild = event.getGuild();
            audioManager = guild.getAudioManager();
            ReceiverModule myAudioModule = new ReceiverModule(App.sttDecoder, myChannel);
            mysendModule = new SendModule(App.sttDecoder);
            audioManager.setReceivingHandler(myAudioModule);
            audioManager.setSendingHandler(mysendModule);
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
        if (!BasicCommands.isUserOp(event.getMember().getUser())) 
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
                return;
            }

            Guild guild = event.getGuild();
            AudioManager audioManager = guild.getAudioManager();
            ReceiverModule myAudioModule = new ReceiverModule(App.sttDecoder, myChannel);
                audioManager.setReceivingHandler(myAudioModule);
                audioManager.openAudioConnection(myChannel);
        }
    }
}
