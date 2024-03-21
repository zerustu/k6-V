package k6v.sound;

import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;

public abstract class AudioProcessor {
    protected ArrayList<AudioProcessorListener> eventListener;

    protected AudioFormat inputFormat;
    protected boolean ready;

    public boolean isReady(){
        return ready;
    }

    public AudioProcessor(AudioFormat format)
    {
        inputFormat = format;
        eventListener = new ArrayList<>();
    }

    protected void Activate(long tag)
    {
        for (AudioProcessorListener audioProcessorListener : eventListener) {
            audioProcessorListener.Activated(tag);
        }
    }

    protected void AddAudioProcessorListener(AudioProcessorListener proc)
    {
        eventListener.add(proc);
    }

    protected void RemmoveProcessorListener(AudioProcessorListener proc)
    {
        eventListener.remove(proc);
    }

    public abstract void Process(byte[] data, long tag);

    public abstract void Flush(long tag);

    public abstract void Reset();

    public void Close()
    {
        ready = false;
    }
}
