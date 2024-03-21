package k6v.sound;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.json.simple.JSONObject;

import k6v.ReceiverModule;
import k6v.SendModule;
import k6v.UserBuffer;
import net.dv8tion.jda.api.audio.UserAudio;

public class AudioModule implements AudioProcessorListener {

    protected int capacity;

    ReceiverModule receiver;
    SendModule sender;

    protected AudioProcessor audioProc;
    
    protected String target;
    protected long tag;

    BlockingQueue<byte[]> fifoQueue;
    HashMap<Long, UserBuffer> usersProba;

    protected Boolean run;
    protected boolean ready;

    private ByteArrayOutputStream byteArrayOutputStream;
    boolean is_focused;
    int userTimeOut;

    Thread processingThread;

    public void addData(byte[] dataIn)
    {
        if (fifoQueue.remainingCapacity() > 0)
        {
            fifoQueue.add(dataIn);
        }
        else
        {
            System.err.println("Fifo for voice control OverFLow, droping package");
        }
    }

    public void addUserData(UserAudio userAudio)
    {
        if (!ready) return;
        if (userAudio.getUser().isBot() || userAudio.getUser().isSystem()) return;
        long key = userAudio.getUser().getIdLong();
        if (usersProba.containsKey(key)) {
            usersProba.get(key).addData(userAudio.getAudioData(1));
        }
        else 
        {
            UserBuffer newBuffer = new UserBuffer(20);
            newBuffer.addData(userAudio.getAudioData(1));
            Thread loopi = new Thread(() -> {newBuffer.register();});
            loopi.start();
            usersProba.put(key, newBuffer);
        }
    }

    public void recordUser(byte[] dataIn)
    {
        if (is_focused)
        {
            userTimeOut = 200;
            byteArrayOutputStream.write(dataIn, 0, dataIn.length);
        }
    }

    protected void ProcessData()
    {
        int TimeOutTimer = -600;

        System.out.println("Process data is alive");
        while (run) {
            if (target.equals("wav"))
            {
                if (userTimeOut > 500)
                {
                    receiver.userFocus = 0;
                    target = "activation";
                    try {
                        AudioInputStream audioInputStream = new AudioInputStream(
                            new ByteArrayInputStream(byteArrayOutputStream.toByteArray()),
                            ReceiverModule.OUTPUT_FORMAT,
                            byteArrayOutputStream.size() / ReceiverModule.OUTPUT_FORMAT.getFrameSize());

                        AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, new File("./userVoice.wav"));

                        System.out.println("timed out, stop recording");;

                        // Clean up resources
                        byteArrayOutputStream.reset();

                        audioInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    userTimeOut += 1;
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                    }
                }
            }
            else
            {
                if (!fifoQueue.isEmpty()) {
                    TimeOutTimer = 0;
                    audioProc.Process(fifoQueue.poll(), 0);
                    //System.out.println("processing new data");
                }
                else if (TimeOutTimer > 100)
                {
                    System.out.println("TimeOuted, let's flush the data");
                    audioProc.Flush(0);
                    TimeOutTimer = -1;
                }
                else if (TimeOutTimer >= 0)
                {
                    TimeOutTimer++;
                }
                else if (TimeOutTimer == -500)
                {
                    System.out.println("timeout 2 : going back to sleeping");
                    receiver.userFocus = 0;
                    audioProc.Reset();
                    target = "activation";
                    try {
                        sender.load("H:\\k6v\\main\\ok.wav");
                    } catch (IllegalStateException | UnsupportedAudioFileException | IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    TimeOutTimer--;
                }
                else if (TimeOutTimer > -500)
                {
                    TimeOutTimer--;
                    audioProc.Flush(0);
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        System.out.println("ProcessData is now dead");
    }

    public void reset()
    {
        run = false;
        tag = 0;
        ready = false;
        try {
            processingThread.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        audioProc.Reset();
        target = "activation";
        byteArrayOutputStream.reset();
        is_focused = false;
        userTimeOut = 0;

        fifoQueue = new ArrayBlockingQueue<byte[]>(capacity);
        usersProba = new HashMap<>();
    }

    public boolean start(ReceiverModule receiver, SendModule sender)
    {
        if (run) return false;
        this.receiver = receiver;
        this. sender = sender;

        run = true;
        ready = true;

        this.processingThread = new Thread(() -> {ProcessData();});
        processingThread.start();
        return true;
    }

    public AudioModule(String moduleType, JSONObject configuration, int capacity)
    {
        // TODO add audioProc creation
        this.capacity = capacity;
        ready = false;
        run = false;
        tag = 0;
        is_focused = false;
        userTimeOut = 0;
        byteArrayOutputStream = new ByteArrayOutputStream();

        fifoQueue = new ArrayBlockingQueue<byte[]>(capacity);
        usersProba = new HashMap<>();
    }

    @Override
    public void Activated(long tag) {
        if (tag == 0)
        {
            ready = false;
            System.out.println("K6-V was detected !");
            for (Entry<Long, UserBuffer> entry : usersProba.entrySet()) {
                audioProc.Reset();
                List<byte[]> datas = entry.getValue().getData(); 
                for (byte[] s : datas) {
                    audioProc.Process(s, entry.getKey());
                }
                entry.getValue().run = false;
            }
            audioProc.Reset();
            ready = true;
        }
        else
        {
            is_focused = true;
            target = "wav";
            receiver.userFocus = tag;
            userTimeOut = 0;
            ready = true;

        }
    }

    public void stop()
    {
        this.reset();
        audioProc.Close();
    }

}
