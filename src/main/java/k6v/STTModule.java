package k6v;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.sound.sampled.*;

import org.jetbrains.annotations.Nullable;

import ai.picovoice.cheetah.*;
import ai.picovoice.porcupine.*;
import ai.picovoice.rhino.*;
import net.dv8tion.jda.api.audio.UserAudio;
import net.dv8tion.jda.api.entities.Member;

public class STTModule {

    static final Float limite = (float) (0);

    ReceiverModule receiver;
    SendModule sender;

    BlockingQueue<byte[]> fifoQueue;
    HashMap<String, UserBuffer> usersProba;

    String target;
    Cheetah localcheetah;
    Porcupine localporc;
    Rhino localrhino;

    String transcript;
    String LastTranscript;

    Boolean run;
    boolean ready;

    private ByteArrayOutputStream byteArrayOutputStream;
    boolean is_focused;
    int userTimeOut;

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
        String key = userAudio.getUser().getId();
        if (usersProba.containsKey(key)) {
            usersProba.get(key).addData(userAudio.getAudioData(1));
        }
        else 
        {
            UserBuffer newBuffer = new UserBuffer(localporc.getFrameLength(), 50, 10);
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

    public static short formatConvert(byte left1, byte left2, byte right1, byte right2)
    {
        short leftSample = (short) ((left1 << 8) | (left2 & 0xFF));
        short rightSample = (short) ((right1 << 8) | (right2 & 0xFF));
        return (short) ((leftSample + rightSample)/2);
    }

    protected void feedSTT(short[] dataIn) 
    {
        if (target.equals("porcupine")) feedSTTporcupine(dataIn);
        // if (target.equals("wav")) recordUser(dataIn);
        if (target.equals("rhino")) feedSTTrhino(dataIn);
    }

    protected void feedSTTporcupine(short[] dataIn)
    {
        int keywordsIndex;
        try {
            keywordsIndex = localporc.process(dataIn);
            if (keywordsIndex == 0) {
                ready = false;
                String result = null;
                System.out.println("K6-V was detected !");
                for (Entry<String, UserBuffer> entry : usersProba.entrySet()) {
                    List<short[]> datas = entry.getValue().getData(); 
                    for (short[] s : datas) {
                        keywordsIndex = localporc.process(s);
                        if (keywordsIndex == 0)
                        {
                            result = entry.getKey();
                        }
                    }
                    entry.getValue().run = false;
                }
                usersProba.clear();
                if (result != null)
                {
                    //byteArrayOutputStream = new ByteArrayOutputStream();
                    target = "rhino";
                    receiver.userFocus = result;
                    sender.load("H:\\k6v\\main\\ok.wav");
                    System.out.println("start listening");
                }
                ready = true;
            }
        } catch (PorcupineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    protected void feedSTTcheta(short[] dataIn)
    {
        CheetahTranscript transcriptObj;
        try {
            transcriptObj = localcheetah.process(dataIn);
            transcript += transcriptObj.getTranscript();
            if (transcript.compareTo(LastTranscript) != 0)
            {
                System.out.println(transcript);
                LastTranscript = transcript;
            }

            if (transcriptObj.getIsEndpoint()) {
                flushSTT(localcheetah);
            }

        } catch (CheetahException e) {
            e.printStackTrace();
        }
    }

    protected void feedSTTrhino(short[] dataIn)
    {    
        try {
            boolean isFinalized = localrhino.process(dataIn);
            if(isFinalized){
                RhinoInference inference = localrhino.getInference();
                if(inference.getIsUnderstood()){
                    String intent = inference.getIntent();
                    Map<String, String> slots = inference.getSlots();
                    System.out.println("rhino has detexted intent :" + intent);
                    slots.forEach((k,v) -> System.out.println("slot : " + k + ", have value : " + v));

                    if (intent.equals("bonjour"))
                    {
                        String name = App.bot.getUserById(receiver.userFocus).getName();
                        sender.respond("bonjour " + name + ".");
                    }
                    else sender.respond("rhino a détécté l'intention " + intent + ".");
                    target = "porcupine";
                    receiver.userFocus = null;
                }
            }
        } catch (RhinoException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    protected void flushSTT(Cheetah localcheetah)
    {
        /*
        CheetahTranscript finalTranscriptObj;
        try {
            finalTranscriptObj = localcheetah.flush();
            transcript += finalTranscriptObj.getTranscript();
            System.out.println(transcript);
            LastTranscript = transcript;
            transcript = "";
        } catch (CheetahException e) {
            e.printStackTrace();
        } */
    }

    protected void ProcessData()
    {
        int framelength = localporc.getFrameLength();
        short[] framebuffer = new short[framelength];
        @Nullable byte[] currentByte = null;
        int bufferIndex = 0;
        short samplebuffer = 0;

        int j = 0;
        int TimeOutTimer = -600;

        System.out.println("Process data is alive");
        while (run) {
            if (target.equals("wav"))
            {
                if (userTimeOut > 500)
                {
                    receiver.userFocus = null;
                    target = "porcupine";
                    try {
                        AudioInputStream audioInputStream = new AudioInputStream(
                            new ByteArrayInputStream(byteArrayOutputStream.toByteArray()),
                            ReceiverModule.OUTPUT_FORMAT,
                            byteArrayOutputStream.size() / ReceiverModule.OUTPUT_FORMAT.getFrameSize());

                        AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, new File("./userVoice.wav"));

                        System.out.println("timed out, stop recording");;

                        // Clean up resources
                        byteArrayOutputStream.reset();
                        byteArrayOutputStream.close();
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
                if (currentByte == null)
                {
                    if (!fifoQueue.isEmpty()) {
                        currentByte = fifoQueue.poll();
                        TimeOutTimer = 0;
                        //System.out.println("processing new data");
                    }
                    else if (TimeOutTimer > 100)
                    {
                        System.out.println("TimeOuted, let's flush the data");
                        if (j != 0)
                        {
                            framebuffer[bufferIndex] = samplebuffer;
                            samplebuffer = 0;
                            j = 0;
                            bufferIndex++;
                        }
                        if (bufferIndex != 0)
                        {
                            while (bufferIndex < framelength) {
                                framebuffer[bufferIndex] = (short) 0;
                                bufferIndex++;
                            }
                        }
                        TimeOutTimer = -1;
                    }
                    else if (TimeOutTimer >= 0)
                    {
                        TimeOutTimer++;
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                        }
                    }
                    else if (TimeOutTimer == -500)
                    {
                        System.out.println("timeout 2 : going back to porcupine");
                        receiver.userFocus = null;
                        target = "porcupine";
                        sender.load("H:\\k6v\\main\\ok.wav");
                        TimeOutTimer--;
                    }
                    else if (TimeOutTimer > -500)
                    {
                        TimeOutTimer--;
                        while (bufferIndex < framelength) {
                            framebuffer[bufferIndex] = (short) 0;
                            bufferIndex++;
                        }
                    }
                }
                if (currentByte != null)
                {
                    short sample = formatConvert(currentByte[4*j], currentByte[4*j+1], currentByte[4*j+2], currentByte[4*j+3]);
                    j++;
                    samplebuffer += sample;

                    if (j%3 == 0)
                    {
                        framebuffer[bufferIndex] = samplebuffer;
                        bufferIndex++;
                        samplebuffer = 0;
                    }
                    if (4*j >= currentByte.length)
                    {
                        currentByte = null;
                        j = 0;
                    }
                }
                if (bufferIndex == framelength) {
                    //System.out.println("let's send some data");
                    feedSTT(framebuffer);
                    if (TimeOutTimer == -1) {
                        flushSTT(localcheetah);
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    bufferIndex = 0;
                }
            }
        }
        localporc.delete();
        localrhino.delete();
        System.out.println("ProcessData is now dead");
    }

    /**
     * @param accessKey String of the accessKey to PicoVoice
     * @param porcupinePath String to a path to builtin Keywords for porcupine
     * @param capacity 
     * @throws CheetahException
     * @throws PorcupineException 
     * @throws RhinoException 
     */
    public STTModule(String accessKey,
                     String porcupinePath, 
                     int capacity, 
                     String porcupineModelPath,
                     String rhinoKeyPath,
                     String rhinoModelPath) throws CheetahException, PorcupineException, RhinoException
    {
        run = true;
        ready = true;
        is_focused = false;

        fifoQueue = new ArrayBlockingQueue<byte[]>(capacity);
        target = "porcupine";
        transcript = "";
        LastTranscript = "";
        usersProba = new HashMap<>();
        //localcheetah = new Cheetah.Builder().setAccessKey(accessKey).setEnableAutomaticPunctuation(true).build();
        localporc = new Porcupine.Builder().setAccessKey(accessKey).setKeywordPath(porcupinePath).setModelPath(porcupineModelPath).build();
        localrhino = new Rhino.Builder().setAccessKey(accessKey).setContextPath(rhinoKeyPath).setModelPath(rhinoModelPath).build();
    }
}
