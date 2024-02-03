package k6v;

import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.CombinedAudio;
import net.dv8tion.jda.api.audio.UserAudio;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;

import javax.sound.sampled.*;

public class ReceiverModule implements AudioReceiveHandler  {

    STTModule decoderModule;
    Boolean isFocusing;
    String userFocus;
    AudioChannelUnion myChannel;
    static final AudioFormat Format = OUTPUT_FORMAT;

    public ReceiverModule(STTModule decoder, AudioChannelUnion channel)
    {
        isFocusing = false;
        userFocus = null;
        decoderModule = decoder;
        decoder.receiver = this;
        myChannel = channel;
    }

    @Override
    public void handleUserAudio(UserAudio userAudio) {
        User user = userAudio.getUser();
        if (user.isBot() || user.isSystem())
        {
            return;
        }
        if (userFocus == null) decoderModule.addUserData(userAudio);
        else
        {
            if (user.getId().equals(userFocus))
            {
                decoderModule.addData(userAudio.getAudioData(1));
            }
        }
    }

    @Override
    public boolean canReceiveUser() {
        return true;
    }

    @Override
    public boolean canReceiveCombined() {
        return (userFocus == null);
    }

    @Override
    public void handleCombinedAudio(CombinedAudio combinedAudio) {
        decoderModule.addData(combinedAudio.getAudioData(1));
    }

    
}
