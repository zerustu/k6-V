package k6v;

import org.jetbrains.annotations.Nullable;

import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.CombinedAudio;
import net.dv8tion.jda.api.audio.UserAudio;
import net.dv8tion.jda.api.entities.User;

import javax.sound.sampled.*;

public class ReceiverModule implements AudioReceiveHandler  {

    STTModule decoderModule;
    Boolean isFocusing;
    @Nullable User userFocus;
    static final AudioFormat Format = OUTPUT_FORMAT;

    public ReceiverModule(STTModule decoder)
    {
        isFocusing = false;
        userFocus = null;
        decoderModule = decoder;
    }

    @Override
    public void handleUserAudio(UserAudio userAudio) {
        User user = userAudio.getUser();
        if (user.isBot() || user.isSystem())
        {
            return;
        }
        //decoderModule.addData(userAudio.getAudioData(1));
        decoderModule.addUserData(userAudio);
        //decoderModule.addData(userAudio.getAudioData(1));
    }

    @Override
    public boolean canReceiveUser() {
        return true;
    }

    @Override
    public boolean canReceiveCombined() {
        return true;
    }

    @Override
    public void handleCombinedAudio(CombinedAudio combinedAudio) {
        decoderModule.addData(combinedAudio.getAudioData(1));
    }

    
}
