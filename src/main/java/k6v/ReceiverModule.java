package k6v;

import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.CombinedAudio;
import net.dv8tion.jda.api.audio.UserAudio;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;

import javax.sound.sampled.*;

public class ReceiverModule implements AudioReceiveHandler  {

    Boolean isFocusing;
    public long userFocus;
    AudioChannelUnion myChannel;
    static final AudioFormat Format = OUTPUT_FORMAT;

    public ReceiverModule(AudioChannelUnion channel)
    {
        isFocusing = false;
        userFocus = 0;
        myChannel = channel;
    }

    @Override
    public boolean canReceiveUser() {
        return false;
    }

    @Override
    public boolean canReceiveCombined() {
        return false;
    }

    @Override
    public void handleCombinedAudio(CombinedAudio combinedAudio) {
    }

    
}
