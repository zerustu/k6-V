package k6v;

import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.CombinedAudio;
import net.dv8tion.jda.api.audio.UserAudio;
import net.dv8tion.jda.api.entities.User;

import javax.sound.sampled.*;

public class ReceiverModule implements AudioReceiveHandler  {

    STTModule decoderModule;
    Boolean isFocusing;
    String userFocus;
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
        if (userFocus == null) decoderModule.addUserData(userAudio);
        else
        {
            if (user.getId().equals(userFocus))
            {
                decoderModule.recordUser(userAudio);
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
